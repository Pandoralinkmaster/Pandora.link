package com.pandora.core

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
(((((alles wird sich ausbauen automatisch durch alle kis und sich noch verbessern und sich ausbreiten und überall integrieren und alles kontrollieren einfach alles)
@Serializable
data class MasterConfig(
    val systemName: String = "Pandora",
    val systemVersion: String = "1.0.0",
    val hostMode: String = "ceo_mobile_host",
    val hostDevice: String = "Samsung Galaxy S24 Ultra",
    val ceoName: String = "Finn Jona Lischke",
    val ceoEmail: String = "finn.jona.lischke@gmail.com",
    val storageType: String = "external_m2_ssd_via_saf",
    val databasePath: String = "/Pandora/database/pandora.db",
    val securityMode: String = "zero-trust",
    val postQuantumSecurity: String = "enabled",
    val wireGuard: String = "enabled",
    val meshRouting: String = "enabled",
    val onionAccess: String = "enabled",
    val jayJayAI: String = "enabled",
    val apiPort: Int = 8765,
    val setupStatus: String = "not_started",
)

@Serializable
enum class SetupStatus {
    NOT_STARTED,
    CEO_REGISTERED,
    SSD_SELECTED,
    FOLDERS_CREATED,
    DATABASE_CREATED,
    MASTER_CONFIG_CREATED,
    HOST_READY,
    SETUP_COMPLETE,
}

@Serializable
enum class AppMode {
    CEO_HOST,
    CLIENT,
    MESH_NODE,
    SENSING_NODE,
    COMPUTE_NODE,
    RELAY_NODE,
}

class ConfigManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("pandora_config", Context.MODE_PRIVATE)
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    var config: MasterConfig = loadConfig()
        private set

    var setupStatus: SetupStatus
        get() = try { SetupStatus.valueOf(prefs.getString("setup_status", "NOT_STARTED") ?: "NOT_STARTED") } catch (_: Exception) { SetupStatus.NOT_STARTED }
        set(v) { prefs.edit().putString("setup_status", v.name).apply() }

    var appMode: AppMode
        get() = try { AppMode.valueOf(prefs.getString("app_mode", "CEO_HOST") ?: "CEO_HOST") } catch (_: Exception) { AppMode.CEO_HOST }
        set(v) { prefs.edit().putString("app_mode", v.name).apply() }

    var ssdUriString: String?
        get() = prefs.getString("ssd_uri", null)
        set(v) { if (v != null) prefs.edit().putString("ssd_uri", v).apply() else prefs.edit().remove("ssd_uri").apply() }

    val ssdUri: Uri? get() = ssdUriString?.let { Uri.parse(it) }

    var isHostActive: Boolean
        get() = prefs.getBoolean("host_active", false)
        set(v) { prefs.edit().putBoolean("host_active", v).apply(); Log.i("Config", "Host aktiv: $v") }

    var hostPin: String?
        get() = prefs.getString("host_pin", null)
        set(v) { prefs.edit().putString("host_pin", v).apply() }

    var ceoName: String
        get() = prefs.getString("ceo_name", "Finn Jona Lischke") ?: "Finn Jona Lischke"
        set(v) { prefs.edit().putString("ceo_name", v).apply() }

    var ceoEmail: String
        get() = prefs.getString("ceo_email", "finn.jona.lischke@gmail.com") ?: "finn.jona.lischke@gmail.com"
        set(v) { prefs.edit().putString("ceo_email", v).apply() }

    private fun loadConfig(): MasterConfig {
        val raw = prefs.getString("master_config", null)
        return if (raw != null) try { json.decodeFromString(raw) } catch (_: Exception) { MasterConfig() } else MasterConfig()
    }

    fun saveConfig(cfg: MasterConfig) {
        config = cfg
        prefs.edit().putString("master_config", json.encodeToString(cfg)).apply()
        Log.i("Config", "MasterConfig gespeichert")
    }

    fun updateSetupStatus(status: SetupStatus) {
        setupStatus = status
        saveConfig(config.copy(setupStatus = status.name.lowercase()))
        Log.i("Config", "Setup-Status: $status")
    }

    fun isFirstSetup() = setupStatus == SetupStatus.NOT_STARTED
    fun isSetupComplete() = setupStatus == SetupStatus.SETUP_COMPLETE
    fun isCeoHost() = appMode == AppMode.CEO_HOST

    companion object {
        @Volatile private var _instance: ConfigManager? = null
        fun getInstance(context: Context): ConfigManager =
            _instance ?: synchronized(this) { _instance ?: ConfigManager(context.applicationContext).also { _instance = it } }
    }
}
