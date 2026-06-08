package com.pandora.database

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.pandora.core.ConfigManager
import com.pandora.core.SetupStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SsdStorageManager – Zugriff auf M.2 SSD (exFAT) via Android Storage Access Framework
 *
 * Samsung Galaxy S24 Ultra: M.2 SSD über USB-C OTG-Adapter (NVME → USB-C)
 * Formatierung: exFAT (nativ vom Nutzer, Samsung S24 Ultra unterstützt exFAT vollständig)
 * Zugriff: ACTION_OPEN_DOCUMENT_TREE → URI-basiert → persistente Berechtigung
 *
 * exFAT-Hinweise:
 * - SAF/DocumentFile funktioniert einwandfrei mit exFAT
 * - Keine Unix-Permissions (exFAT unterstützt diese nicht) – kein Problem, SAF regelt das
 * - Große Dateien (>4GB) problemlos möglich (exFAT-Vorteil gegenüber FAT32)
 * - SQLite (Room) NICHT direkt auf exFAT via SAF (kein echtes Dateipfad-Locking)
 *   → Room DB liegt in app-internem Speicher (getFilesDir())
 *   → SSD wird genutzt für: Config, Receipts, Logs, Backups, JayJay-Daten, Wallet usw.
 *   → Automatisches DB-Backup auf SSD alle 15 Minuten
 *
 * Pandora-Ordnerstruktur auf SSD:
 * /Pandora/
 * ├── master_config.json
 * ├── database/
 * ├── receipts/
 * ├── products/
 * ├── logs/
 * ├── backups/
 * ├── consent/
 * ├── onion/
 * ├── scan/
 * ├── buildings/
 * ├── csi/
 * ├── chat/
 * ├── security/
 * ├── registry/
 * ├── network/
 * ├── mesh/
 * └── jayjay/
 */
class SsdStorageManager(
    private val context: Context,
    private val config: ConfigManager,
) {
    companion object {
        val PANDORA_DIRS = listOf(
            "database", "receipts", "products", "logs", "backups",
            "consent", "onion", "scan", "buildings", "csi", "chat",
            "security", "registry", "network", "mesh", "jayjay",
            "wallet", "compute", "wireguard",
        )
    }

    private var rootDoc: DocumentFile? = null

    /** SSD-URI speichern und persistente Berechtigung anfordern */
    fun setSsdUri(uri: Uri) {
        context.contentResolver.takePersistableUriPermission(uri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
            android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        config.ssdUriString = uri.toString()
        rootDoc = DocumentFile.fromTreeUri(context, uri)
        Log.i("SSD", "SSD URI gesetzt: $uri")
    }

    fun isAvailable(): Boolean {
        val uri = config.ssdUri ?: return false
        return try {
            val doc = DocumentFile.fromTreeUri(context, uri)
            doc?.isDirectory == true && doc.canWrite()
        } catch (_: Exception) { false }
    }

    private fun getRootDoc(): DocumentFile? {
        if (rootDoc != null) return rootDoc
        val uri = config.ssdUri ?: return null
        return DocumentFile.fromTreeUri(context, uri).also { rootDoc = it }
    }

    /** /Pandora/ Ordner holen oder erstellen */
    private fun getPandoraRoot(): DocumentFile? {
        val root = getRootDoc() ?: return null
        return root.findFile("Pandora") ?: root.createDirectory("Pandora")
    }

    /** Komplette Ordnerstruktur erstellen */
    suspend fun createFolderStructure(): Boolean = withContext(Dispatchers.IO) {
        try {
            val pandora = getPandoraRoot() ?: run {
                Log.e("SSD", "Kann /Pandora/ nicht erstellen"); return@withContext false
            }
            var created = 0
            for (dir in PANDORA_DIRS) {
                if (pandora.findFile(dir) == null) {
                    pandora.createDirectory(dir)?.let { created++ }
                }
            }
            Log.i("SSD", "Ordnerstruktur erstellt: $created neue Ordner")
            config.updateSetupStatus(SetupStatus.FOLDERS_CREATED)
            true
        } catch (e: Exception) {
            Log.e("SSD", "Ordnerstruktur fehlgeschlagen: ${e.message}"); false
        }
    }

    /** master_config.json auf SSD speichern */
    suspend fun saveMasterConfig(json: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val pandora = getPandoraRoot() ?: return@withContext false
            val file = pandora.findFile("master_config.json")
                ?: pandora.createFile("application/json", "master_config.json")
                ?: return@withContext false
            context.contentResolver.openOutputStream(file.uri, "wt")?.use {
                it.write(json.toByteArray())
            }
            Log.i("SSD", "master_config.json gespeichert")
            true
        } catch (e: Exception) { Log.e("SSD", "Config-Speichern fehlgeschlagen: ${e.message}"); false }
    }

    /** Datei in einem Pandora-Unterordner lesen */
    fun readFile(subDir: String, filename: String): String? {
        return try {
            val pandora = getPandoraRoot() ?: return null
            val dir = pandora.findFile(subDir) ?: return null
            val file = dir.findFile(filename) ?: return null
            context.contentResolver.openInputStream(file.uri)?.use { it.bufferedReader().readText() }
        } catch (e: Exception) { Log.w("SSD", "Lesen fehlgeschlagen: $subDir/$filename: ${e.message}"); null }
    }

    /** Datei in einem Pandora-Unterordner schreiben */
    fun writeFile(subDir: String, filename: String, content: String): Boolean {
        return try {
            val pandora = getPandoraRoot() ?: return false
            val dir = pandora.findFile(subDir) ?: pandora.createDirectory(subDir) ?: return false
            val file = dir.findFile(filename) ?: dir.createFile("text/plain", filename) ?: return false
            context.contentResolver.openOutputStream(file.uri, "wt")?.use { it.write(content.toByteArray()) }
            true
        } catch (e: Exception) { Log.e("SSD", "Schreiben fehlgeschlagen: $subDir/$filename: ${e.message}"); false }
    }

    /**
     * Datenbankpfad für Room SQLite.
     *
     * exFAT-Kompatibilität: SQLite benötigt Datei-Locking (fcntl), das exFAT
     * über SAF/ContentResolver NICHT garantiert. Deshalb liegt die Datenbank
     * im internen App-Speicher (getFilesDir()), der ext4 nutzt.
     * Automatische Backups auf SSD alle 15 Minuten via [backupDatabaseToSsd].
     */
    fun getDatabasePath(): String {
        val dbDir = java.io.File(context.filesDir, "pandora_db").also { it.mkdirs() }
        return java.io.File(dbDir, "pandora.db").absolutePath
    }

    /** SQLite DB auf SSD sichern (exFAT safe – reiner Byte-Copy) */
    suspend fun backupDatabaseToSsd(dbPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val dbFile = java.io.File(dbPath)
            if (!dbFile.exists()) return@withContext false
            val bytes = dbFile.readBytes()
            val pandora = getPandoraRoot() ?: return@withContext false
            val backupsDir = pandora.findFile("backups") ?: pandora.createDirectory("backups") ?: return@withContext false
            val ts = java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.US).format(java.util.Date())
            val backupFile = backupsDir.createFile("application/octet-stream", "pandora_$ts.db") ?: return@withContext false
            context.contentResolver.openOutputStream(backupFile.uri)?.use { it.write(bytes) }
            Log.i("SSD", "DB-Backup erstellt: pandora_$ts.db (${bytes.size / 1024}KB)")
            true
        } catch (e: Exception) { Log.e("SSD", "DB-Backup fehlgeschlagen: ${e.message}"); false }
    }

    /** Letztes DB-Backup von SSD wiederherstellen */
    suspend fun restoreLatestBackup(targetDbPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val pandora = getPandoraRoot() ?: return@withContext false
            val backupsDir = pandora.findFile("backups") ?: return@withContext false
            val latest = backupsDir.listFiles()
                .filter { it.name?.endsWith(".db") == true }
                .maxByOrNull { it.lastModified() } ?: return@withContext false
            val bytes = context.contentResolver.openInputStream(latest.uri)?.use { it.readBytes() } ?: return@withContext false
            java.io.File(targetDbPath).writeBytes(bytes)
            Log.i("SSD", "DB wiederhergestellt von: ${latest.name}")
            true
        } catch (e: Exception) { Log.e("SSD", "Wiederherstellung fehlgeschlagen: ${e.message}"); false }
    }

    fun getSsdInfo(): Map<String, Any> {
        val uri = config.ssdUri
        val available = isAvailable()
        return mapOf(
            "available" to available,
            "uri" to (uri?.toString() ?: "nicht gesetzt"),
            "pandoraRootExists" to (getPandoraRoot() != null),
            "freeSpace" to getFreeSpaceGb(),
        )
    }

    private fun getFreeSpaceGb(): String {
        return try {
            val uri = config.ssdUri ?: return "unbekannt"
            val stat = context.contentResolver.query(uri, arrayOf(
                android.provider.DocumentsContract.Root.COLUMN_AVAILABLE_BYTES
            ), null, null, null)
            stat?.use {
                if (it.moveToFirst()) {
                    val bytes = it.getLong(0)
                    "%.1f GB".format(bytes / 1_073_741_824.0)
                } else "unbekannt"
            } ?: "unbekannt"
        } catch (_: Exception) { "unbekannt" }
    }
}
