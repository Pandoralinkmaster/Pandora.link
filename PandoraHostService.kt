package com.pandora.core

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.pandora.MainActivity
import com.pandora.api.PandoraApiServer
import com.pandora.database.PandoraDatabase
import com.pandora.database.SsdStorageManager
import com.pandora.jayjay.JayJayVoiceService
import com.pandora.mesh.BluetoothMeshManager
import com.pandora.mesh.WifiMeshManager
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

/**
 * Pandora Host Service – Kern-Dienst des CEO Hosts
 *
 * Läuft dauerhaft als Foreground Service auf dem Samsung Galaxy S24 Ultra.
 * Startet automatisch nach Boot (via BootReceiver).
 *
 * Verantwortlichkeiten:
 * - Ktor API-Server starten/stoppen
 * - DB-Backup auf exFAT SSD alle 15 Minuten
 * - Mesh-Netzwerke aktiv halten
 * - JayJay Voice Service koordinieren
 */
class PandoraHostService : Service() {

    private val config: ConfigManager by inject()
    private val apiServer: PandoraApiServer by inject()
    private val ssd: SsdStorageManager by inject()
    private val db: PandoraDatabase by inject()
    private val btMesh: BluetoothMeshManager by inject()
    private val wifiMesh: WifiMeshManager by inject()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val CHANNEL_ID = "pandora_host"
        const val NOTIF_ID   = 1
        const val ACTION_STOP = "com.pandora.STOP_HOST"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.i("HostService", "Pandora Host Service gestartet")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) { stopSelf(); return START_NOT_STICKY }

        startForeground(NOTIF_ID, buildNotification())

        if (config.isCeoHost() && config.isSetupComplete()) {
            launchHostTasks()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        apiServer.stop()
        btMesh.stopAll()
        scope.cancel()
        Log.i("HostService", "Pandora Host Service beendet")
    }

    private fun launchHostTasks() {
        // API-Server starten
        scope.launch {
            try {
                apiServer.start()
                Log.i("HostService", "API-Server gestartet")
            } catch (e: Exception) { Log.e("HostService", "API-Server Fehler: ${e.message}") }
        }

        // Bluetooth Mesh starten
        scope.launch {
            btMesh.initialize()
            btMesh.startAdvertising("pandora-ceo")
            btMesh.startScanning()
        }

        // WiFi Mesh starten
        scope.launch {
            wifiMesh.initialize()
            wifiMesh.discoverPeers()
        }

        // DB-Backup alle 15 Minuten (exFAT SSD)
        scope.launch {
            while (isActive) {
                delay(900_000L)   // 15 Minuten
                if (ssd.isAvailable()) {
                    val dbPath = ssd.getDatabasePath()
                    val ok = ssd.backupDatabaseToSsd(dbPath)
                    Log.i("HostService", "DB-Backup: ${if (ok) "OK" else "fehlgeschlagen"}")
                }
            }
        }

        // JayJay Voice Service starten
        startService(Intent(this, JayJayVoiceService::class.java))

        // Notification aktualisieren
        updateNotification("Host aktiv · ${apiServer.getActiveConnections().size} Verbindungen")
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm?.notify(NOTIF_ID, buildNotification(text))
    }

    private fun buildNotification(text: String = "Pandora CEO Host läuft"): Notification {
        val stopIntent = PendingIntent.getService(this, 0,
            Intent(this, PandoraHostService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE)
        val openIntent = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pandora")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_share)
            .setOngoing(true)
            .setContentIntent(openIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stoppen", stopIntent)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Pandora Host",
            NotificationManager.IMPORTANCE_LOW).apply {
            description = "Pandora CEO Host Dienst"
        }
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }
}
