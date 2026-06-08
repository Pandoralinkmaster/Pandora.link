package com.pandora.mesh

import android.content.Context
import android.net.wifi.WifiManager
import android.net.wifi.p2p.*
import android.net.wifi.p2p.WifiP2pManager.*
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

@Serializable
data class WifiMeshState(
    val wifiDirectActive: Boolean = false,
    val wifiAwareActive: Boolean = false,
    val localHotspotActive: Boolean = false,
    val peers: List<String> = emptyList(),
    val rssiMap: Map<String, Int> = emptyMap(),   // BSSID → RSSI
)

class WifiMeshManager(private val context: Context) {

    private val _state = MutableStateFlow(WifiMeshState())
    val state: StateFlow<WifiMeshState> = _state

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val p2pManager = context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
    private var p2pChannel: WifiP2pManager.Channel? = null
    private val discoveredPeers = mutableListOf<String>()

    fun initialize() {
        p2pChannel = p2pManager?.initialize(context, context.mainLooper, null)
        Log.i("WifiMesh", "WiFi Mesh initialisiert")
    }

    /** WiFi Direct Peers suchen */
    fun discoverPeers() {
        p2pChannel?.let { ch ->
            p2pManager?.discoverPeers(ch, object : ActionListener {
                override fun onSuccess() { Log.i("WifiMesh", "WiFi Direct Discovery gestartet") }
                override fun onFailure(reason: Int) { Log.w("WifiMesh", "Discovery fehlgeschlagen: $reason") }
            })
        }
    }

    /** RSSI aller sichtbaren WLANs scannen (für CSI/Sensing) */
    fun scanRssi(): Map<String, Int> {
        val results = wifiManager.scanResults
        val map = results.associate { it.BSSID to it.level }
        _state.value = _state.value.copy(rssiMap = map)
        Log.i("WifiMesh", "RSSI-Scan: ${map.size} APs gefunden")
        return map
    }

    /** Eigenen WLAN-Hotspot starten (andere Pandora-Geräte verbinden sich) */
    fun startLocalHotspot() {
        try {
            val reserved = wifiManager.javaClass.getMethod("startLocalOnlyHotspot",
                WifiManager.LocalOnlyHotspotCallback::class.java, android.os.Handler::class.java)
            Log.i("WifiMesh", "Local-Only Hotspot angefordert")
        } catch (e: Exception) { Log.w("WifiMesh", "Hotspot nicht unterstützt: ${e.message}") }
    }

    /** Verbindungsqualität schätzen (0–100) */
    fun linkQuality(): Int {
        val rssi = wifiManager.connectionInfo.rssi
        return WifiManager.calculateSignalLevel(rssi, 100)
    }

    fun getState() = _state.value
}
