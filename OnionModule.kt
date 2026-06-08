package com.pandora.onion

import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import java.net.InetSocketAddress
import java.net.Proxy

/**
 * Onion / Tor Access Module
 *
 * Nutzt Orbot (offizieller Tor-Client für Android) via SOCKS5-Proxy
 * Orbot muss separat installiert sein: org.torproject.android
 *
 * Alle sensitiven Verbindungen (Bitcoin Broadcast, JayJay API, Mesh-Sync)
 * können über Tor geleitet werden.
 *
 * Tor SOCKS5 Port: 9050 (Orbot Standard)
 */
@Serializable
data class OnionState(
    val isActive: Boolean = false,
    val onionAddress: String = "",
    val torVersion: String = "",
    val circuitCount: Int = 0,
    val bytesRead: Long = 0L,
    val bytesWritten: Long = 0L,
    val lastChecked: Long = 0L,
)

class OnionModule(private val context: Context) {

    private val _state = MutableStateFlow(OnionState())
    val state: StateFlow<OnionState> = _state

    companion object {
        const val ORBOT_PACKAGE  = "org.torproject.android"
        const val TOR_SOCKS_PORT = 9050
        const val TOR_HTTP_PORT  = 8118
        const val TOR_CONTROL_PORT = 9051
        const val CHECK_URL      = "https://check.torproject.org/api/ip"
    }

    /** Orbot starten (Start-Intent senden) */
    fun startOrbot() {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                `package` = ORBOT_PACKAGE
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Log.i("Onion", "Orbot gestartet")
        } catch (e: Exception) { Log.e("Onion", "Orbot nicht installiert: ${e.message}") }
    }

    /** Tor-Verbindung testen */
    suspend fun checkTorConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val client = torClient()
            val request = okhttp3.Request.Builder().url(CHECK_URL).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            val isTor = body.contains("\"IsTor\":true", ignoreCase = true)
            _state.value = _state.value.copy(
                isActive = isTor,
                lastChecked = System.currentTimeMillis()
            )
            Log.i("Onion", "Tor aktiv: $isTor")
            isTor
        } catch (e: Exception) {
            _state.value = _state.value.copy(isActive = false)
            Log.w("Onion", "Tor nicht erreichbar: ${e.message}")
            false
        }
    }

    /** OkHttpClient der über Tor-SOCKS5 geht */
    fun torClient(
        connectTimeoutSec: Int = 30,
        readTimeoutSec: Int = 60,
    ): OkHttpClient {
        val proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", TOR_SOCKS_PORT))
        return OkHttpClient.Builder()
            .proxy(proxy)
            .connectTimeout(connectTimeoutSec.toLong(), java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(readTimeoutSec.toLong(), java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    /** Einfache HTTP-GET Anfrage über Tor */
    suspend fun getViaTor(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val response = torClient().newCall(okhttp3.Request.Builder().url(url).build()).execute()
            response.body?.string()
        } catch (e: Exception) { Log.e("Onion", "Tor-Request fehlgeschlagen: ${e.message}"); null }
    }

    fun isOrbotInstalled(): Boolean = try {
        context.packageManager.getPackageInfo(ORBOT_PACKAGE, 0); true
    } catch (_: Exception) { false }

    fun getStatus() = _state.value
}
