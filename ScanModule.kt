package com.pandora.scan

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.wifi.WifiManager
import android.util.Log
import com.pandora.database.PandoraDatabase
import com.pandora.database.entity.CsiData
import com.pandora.database.entity.ScanSession
import com.pandora.security.SecurityModule
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.sqrt

/**
 * Pandora Scan Module – RSSI / BLE / Bewegung / CSI-Forschung
 *
 * Verfügbar in v1 (auf Samsung Galaxy S24 Ultra und anderen Android-Geräten):
 * - WLAN RSSI-Scan (alle sichtbaren APs)
 * - Bluetooth/BLE Signalstärke
 * - Accelerometer / Gyro (Bewegungserkennung)
 * - Kamera-basierter QR-Code-Scan
 *
 * Forschungsfunktionen (RuView-Kompatibilitäts-Layer):
 * - CSI-ähnliche Datenerfassung via RSSI-Varianz (Approximation)
 * - Keine echten CSI-Daten ohne Root/Kernel-Patch
 * - Datenspeicherung für spätere Analyse
 *
 * PandoraMeshSensingNetwork:
 * - Mehrere Geräte scannen gleichzeitig
 * - CEO-Host aggregiert alle Scan-Daten
 * - Triangulation über RSSI-Differenzen
 */
@Serializable
data class ScanResult(
    val sessionId: String,
    val rssiMap: Map<String, Int> = emptyMap(),
    val bleDevices: List<BleDevice> = emptyList(),
    val motion: MotionData = MotionData(),
    val presenceScore: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
)

@Serializable data class BleDevice(val address: String, val name: String, val rssi: Int)
@Serializable data class MotionData(val accelX: Float = 0f, val accelY: Float = 0f, val accelZ: Float = 0f, val magnitude: Float = 0f)

class ScanModule(
    private val context: Context,
    private val db: PandoraDatabase,
    private val security: SecurityModule,
) : SensorEventListener {

    private val _state = MutableStateFlow<ScanResult?>(null)
    val state: StateFlow<ScanResult?> = _state

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val json = Json { ignoreUnknownKeys = true }
    private var currentMotion = MotionData()
    private var isListening = false

    // ── Sensoren ───────────────────────────────────────────────────────────────

    fun startMotionTracking() {
        if (isListening) return
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        accel?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        gyro?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        isListening = true
        Log.i("Scan", "Bewegungssensoren gestartet")
    }

    fun stopMotionTracking() {
        sensorManager.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]; val y = event.values[1]; val z = event.values[2]
            val mag = sqrt(x * x + y * y + z * z)
            currentMotion = MotionData(x, y, z, mag)
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // ── RSSI-Scan ──────────────────────────────────────────────────────────────

    suspend fun fullScan(buildingId: String = ""): ScanResult = withContext(Dispatchers.IO) {
        val sessionId = security.randomHex(8)
        val rssiMap = scanWifiRssi()
        val presenceScore = estimatePresence(rssiMap)

        val result = ScanResult(
            sessionId = sessionId,
            rssiMap = rssiMap,
            motion = currentMotion,
            presenceScore = presenceScore,
        )
        _state.value = result

        // In DB speichern
        db.scanDao().upsert(ScanSession(
            sessionId = sessionId, deviceId = "local",
            buildingId = buildingId, scanType = "rssi",
            dataJson = json.encodeToString(result),
            endedAt = System.currentTimeMillis(),
        ))

        // CSI-ähnliche Rohdaten
        db.scanDao().insertCsi(CsiData(
            sessionId = sessionId, deviceId = "local",
            dataType = "rssi_variance",
            rawData = json.encodeToString(rssiMap),
        ))
        Log.i("Scan", "Scan abgeschlossen: ${rssiMap.size} APs, Presence=$presenceScore")
        result
    }

    private fun scanWifiRssi(): Map<String, Int> {
        wifiManager.startScan()
        return wifiManager.scanResults.associate { it.BSSID to it.level }
    }

    /**
     * Vereinfachte Anwesenheitsschätzung:
     * RSSI-Varianz ist höher wenn sich jemand im Raum bewegt (multipath-Fading)
     */
    private fun estimatePresence(rssiMap: Map<String, Int>): Float {
        if (rssiMap.isEmpty()) return 0f
        val values = rssiMap.values.map { it.toFloat() }
        val mean = values.average().toFloat()
        val variance = values.map { (it - mean) * (it - mean) }.average().toFloat()
        return minOf(variance / 100f, 1f)   // normalisiert auf 0–1
    }

    // ── RuView-Kompatibilitäts-Layer ───────────────────────────────────────────

    /**
     * PandoraRuViewCompatibilityLayer:
     * Exportiert Scan-Daten im RuView-kompatiblen Format für externe CSI-Forschungstools.
     * Echte CSI-Daten erfordern Root + speziellen Kernel-Treiber.
     */
    fun exportRuViewCompatible(session: ScanSession): Map<String, Any> {
        return mapOf(
            "format" to "pandora_ruview_v1",
            "sessionId" to session.sessionId,
            "type" to "rssi_approximated",
            "csiAvailable" to false,
            "note" to "Echte CSI-Daten erfordern Root. Dies ist eine RSSI-Näherung.",
            "data" to session.dataJson,
            "timestamp" to session.startedAt,
        )
    }

    fun destroy() { stopMotionTracking(); scope.cancel() }
}
