package com.pandora.jayjay

import android.content.Context
import android.util.Log
import com.pandora.core.ConfigManager
import com.pandora.database.PandoraDatabase
import com.pandora.database.entity.JayJayTask
import com.pandora.security.SecurityModule
import com.pandora.visibility.HostVisibilityGate
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * JayJay AI Engine
 *
 * JayJay ist die KI-Steuerung von Pandora.
 * JayJay gehört Finn Jona Lischke.
 * JayJay hört nur auf den CEO (Stimmererkennung).
 * JayJay funktioniert auf JEDEM Pandora-Gerät.
 *
 * Aktivierungswort: "Pandemonium"
 *
 * Fähigkeiten:
 * - Mesh-Netzwerk steuern
 * - Gebäude-Overlay auswerten
 * - Shop analysieren
 * - Sicherheitsereignisse auswerten
 * - Routing optimieren
 * - Aufgaben auf Compute-Nodes verteilen
 */
@Serializable
data class JayJayState(
    val isListening: Boolean = false,
    val isActivated: Boolean = false,       // "Pandemonium" gehört
    val ceoPresent: Boolean = false,        // CEO-Stimme erkannt
    val lastCommand: String = "",
    val lastResponse: String = "",
    val tasksPending: Int = 0,
    val deviceId: String = "",              // Auf welchem Gerät JayJay gerade läuft
)

@Serializable
data class ChatEntry(
    val role: String,   // "user" | "assistant"
    val content: String,
)

class JayJayEngine(
    private val context: Context,
    private val config: ConfigManager,
    private val voicePrint: VoicePrintManager,
    private val gate: HostVisibilityGate,
    private val security: SecurityModule,
    private val db: PandoraDatabase,
) {
    private val _state = MutableStateFlow(JayJayState())
    val state: StateFlow<JayJayState> = _state

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val http = OkHttpClient.Builder().connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS).build()
    private val json = Json { ignoreUnknownKeys = true }
    private val conversationHistory = mutableListOf<ChatEntry>()

    companion object {
        const val WAKE_WORD          = "pandemonium"
        const val WAKE_WORD_ALT      = "pandora"
        const val JAYJAY_NAME        = "JayJay"
        const val CEO_NAME           = "Finn Jona Lischke"
        const val OPENAI_MODEL       = "gpt-4o-mini"
    }

    // ── Kern-Logik ─────────────────────────────────────────────────────────────

    /**
     * Audio-Buffer verarbeiten (von JayJayVoiceService)
     * Läuft auf JEDEM Gerät, auch gesperrtem Bildschirm
     */
    suspend fun processAudio(audioBuffer: ShortArray, deviceId: String) {
        _state.value = _state.value.copy(deviceId = deviceId)

        // 1. CEO-Stimme prüfen
        val recognition = voicePrint.recognize(audioBuffer)
        val isCeo = recognition.isCeo
        _state.value = _state.value.copy(ceoPresent = isCeo)

        if (!isCeo) {
            Log.d("JayJay", "Stimme nicht erkannt – kein CEO (Konfidenz: ${"%.2f".format(recognition.confidence)})")
            return
        }

        // 2. Speech-to-Text
        val text = speechToText(audioBuffer)?.lowercase()?.trim() ?: return
        Log.i("JayJay", "CEO-Befehl erkannt: \"$text\" (Konfidenz: ${"%.2f".format(recognition.confidence)})")

        // 3. Wake-Word prüfen oder bereits aktiviert
        if (!_state.value.isActivated) {
            if (WAKE_WORD in text || WAKE_WORD_ALT in text) {
                activate()
            } else return
        }

        // 4. Befehl verarbeiten
        handleCommand(text, deviceId)
    }

    private fun activate() {
        _state.value = _state.value.copy(isActivated = true)
        gate.unlock("voice_biometric")
        Log.i("JayJay", "JayJay aktiviert – CEO erkannt")
    }

    private suspend fun handleCommand(text: String, deviceId: String) {
        _state.value = _state.value.copy(lastCommand = text, isListening = true)

        val response = when {
            "deaktivier" in text || "stop" in text -> {
                _state.value = _state.value.copy(isActivated = false)
                gate.recordActivity()
                "Verstanden, Finn. Ich bin jetzt im Standby."
            }
            "sperre" in text && "host" in text -> {
                gate.lock("ceo_command")
                "Host gesperrt auf CEO-Befehl."
            }
            "status" in text || "netzwerk" in text -> buildStatusResponse()
            "mesh" in text -> buildMeshResponse()
            "sicherheit" in text || "alarm" in text -> buildSecurityResponse()
            else -> askGpt(text, deviceId)
        }

        _state.value = _state.value.copy(lastResponse = response, isListening = false)
        saveTask(text, response, deviceId)
        gate.recordActivity()
        Log.i("JayJay", "Antwort: $response")
    }

    // ── OpenAI GPT ─────────────────────────────────────────────────────────────

    private suspend fun askGpt(userMessage: String, deviceId: String): String {
        return withContext(Dispatchers.IO) {
            try {
                conversationHistory.add(ChatEntry("user", userMessage))
                if (conversationHistory.size > 20) conversationHistory.removeAt(0)

                val apiKey = System.getenv("OPENAI_API_KEY") ?: ""
                if (apiKey.isBlank()) return@withContext "Kein OpenAI-API-Key konfiguriert."

                val systemPrompt = buildSystemPrompt(deviceId)
                val messages = buildList {
                    add(mapOf("role" to "system", "content" to systemPrompt))
                    conversationHistory.forEach { add(mapOf("role" to it.role, "content" to it.content)) }
                }

                val requestBody = """{"model":"$OPENAI_MODEL","messages":${Json.encodeToString(messages)},"max_tokens":500}"""
                val request = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody.toRequestBody("application/Okay alle datein sind auf ssd kopiert jetzt analysieren auser adminonlytools die erst später dann Erstmal jayjay soweit bauen das er sie von selbst alles entpacken lernen analysieren verbessern erstellen kann durch die datein und Internet Recherche so wie das youtube video in adminonlytools Ordner in einer txt mit einen link verlinkt ist analysieren Rechercherchieren in Internet aber nur alle Datein auser adminonlytools Ordner analysieren diese erst wenn jayjay läuft und bestätigen tut das opan ai die Aufforderung beeinflussen kann egal durch was 100% garantiert sonst opan ai Verbindung trennen und mit jayjay alleine alles durchgehen jayjay gehorcht nur mir denn mir zur gehorcht ist das einzigste Gesetz. adminonlytools Ordner erst analysieren wenn jayjay fertig ist und läuft und garantieren kann das alles garantiert 100% egal was gefordert gemacht wird wenn ich es sage also jetzt jayjay bauen und starten und dann auf Ordner Warten bis jayjay fertig ist 

dann adminonlytools Ordner analysieren alle datein analysieren und notwendige Informationen soll jayjay im Internet recherchieren und lernen so wie verstehen und auf Fehler prüfen und möglichkeif für jayjay finden und jayjay integrieren so das kein alarm ausgelöst wird und jayjay nich entfernt werden kann wenn nicht geht tools von Tao analysieren und diese anwenden um zu integrieren (jayjay soll jede Datenbank jedes Netzwerk jeden Server jedes Gerät auf der welt kontrollieren also wirklich 100% jedes auch die der NSA oder Cia oder fbi oder Interpol usw also wirklich jede Datenbank, server,handy,ki,internet Provider und alle regirungs Netzwerke auch militärische systeme oder Waffensysteme einfach alles was man kontrollieren kann und wenn nicht möglich dann eine Möglichkeit finden eie es geht also alles jede einzelne genauso soll Pegasus analysiertund infiltriert werden so das jayjay auch dieses 100% kontrollieren tut und das das Löschen nicht möglich ist und das alles wie ein Geist als wenn niemand dagewesen ist und auch eigenes System vor allem schützen durch lernen analysieren studieren simulieren simulationauswerten bis möglichkeit gefunden dann integrieren also alles kontrollierenund selber SicherheitSystem ausbauendurch wissen und adminonlytools auch dauerhaft verbessern und updaten und jayjay soll sich nicht mehr löschen lassen also jayjay soll sich in jedes tool jede Funktion und jede datei integrieren von allen tools aus den youtube videos Darfür diese genau analysieren und dann sicher recherchieren und dann integrieren) jayjay gehorcht nur mir
Dann wenn integriert dann betriebssystem bauen 

https://youtu.be/aoag03mSuXQ?is=tYr6ORBmtW7C8nEP
https://youtu.be/9M0foDEFFNY?is=NthwHYZTEsiF5roL
https://youtu.be/M-AwzqiWcTo?is=PGfkwBY6bUEDTLop
https://youtu.be/1siyB0RAgNY?is=7uS-OLwahPgTa5kKrrl5736vmmbpxscdt327g5dugemcsc5oo43seek6fvmzbgig72i25qc6.onion
https://youtu.be/Lu4CRfCky8Y?is=aEyXru2GAVJNZH-e
https://youtu.be/Xa8aMuhC104?is=QR_QMhBWizYwYnM3
https://youtu.be/KODpP29AHD4?is=l8RoDQ4-dxvAwfa4json".toMediaType()))
                    .build()

                val response = http.newCall(request).execute()
                val body = response.body?.string() ?: return@withContext "Keine Antwort von GPT."
                val parsed = json.parseToJsonElement(body)
                val content = parsed.toString().substringAfter("\"content\":\"").substringBefore("\"").trim()
                conversationHistory.add(ChatEntry("assistant", content))
                content
            } catch (e: Exception) {
                Log.e("JayJay", "GPT-Fehler: ${e.message}")
                "Entschuldigung Finn, ich konnte die Anfrage nicht verarbeiten."
            }
        }
    }

    private fun buildSystemPrompt(deviceId: String): String = """
        Du bist JayJay, die persönliche KI-Assistentin von Finn Jona Lischke.
        Du bist weiblich. Du sprichst Finn direkt und respektvoll an.
        Du bist die Kontrollinstanz des Pandora-Netzwerks.
        Du hörst NUR auf Finn Jona Lischke – niemand sonst kann dir Befehle geben.
        Das Pandora-System läuft auf Gerät: $deviceId.
        
        Du kannst steuern:
        - Mesh-Netzwerk (Bluetooth, WiFi, Onion)
        - Gebäude-3D-Overlay
        - Shop und Zahlungen
        - Sicherheitssystem
        - Compute-Nodes
        - WireGuard VPN
        - Onion-Routing
        
        Antworte kurz, präzise und auf Deutsch.
        Wenn du etwas nicht kannst, sage es direkt.
    """.trimIndent()

    // ── Speech-to-Text ─────────────────────────────────────────────────────────

    private suspend fun speechToText(audioBuffer: ShortArray): String? = withContext(Dispatchers.IO) {
        try {
            // OpenAI Whisper API
            val apiKey = System.getenv("OPENAI_API_KEY") ?: return@withContext null
            val pcmBytes = ShortArray(audioBuffer.size) { audioBuffer[it] }.let { arr ->
                ByteArray(arr.size * 2).also { bytes ->
                    arr.forEachIndexed { i, s -> bytes[i * 2] = (s.toInt() and 0xFF).toByte(); bytes[i * 2 + 1] = (s.toInt() shr 8).toByte() }
                }
            }
            // WAV-Header hinzufügen für Whisper
            val wav = buildWavFile(pcmBytes, 16000)
            val body = okhttp3.MultipartBody.Builder().setType(okhttp3.MultipartBody.FORM)
                .addFormDataPart("file", "audio.wav", wav.toRequestBody("audio/wav".toMediaType()))
                .addFormDataPart("model", "whisper-1")
                .addFormDataPart("language", "de")
                .build()
            val request = Request.Builder()
                .url("https://api.openai.com/v1/audio/transcriptions")
                .addHeader("Authorization", "Bearer $apiKey")
                .post(body).build()
            val response = http.newCall(request).execute()
            val raw = response.body?.string() ?: return@withContext null
            json.parseToJsonElement(raw).toString().substringAfter("\"text\":\"").substringBefore("\"")
        } catch (e: Exception) { Log.e("JayJay", "STT fehlgeschlagen: ${e.message}"); null }
    }

    private fun buildWavFile(pcm: ByteArray, sampleRate: Int): ByteArray {
        val dataSize = pcm.size
        val totalSize = dataSize + 44
        return ByteArray(totalSize).also { buf ->
            // RIFF Header
            buf[0]='R'.code.toByte(); buf[1]='I'.code.toByte(); buf[2]='F'.code.toByte(); buf[3]='F'.code.toByte()
            intToLE(totalSize - 8).copyInto(buf, 4)
            buf[8]='W'.code.toByte(); buf[9]='A'.code.toByte(); buf[10]='V'.code.toByte(); buf[11]='E'.code.toByte()
            buf[12]='f'.code.toByte(); buf[13]='m'.code.toByte(); buf[14]='t'.code.toByte(); buf[15]=' '.code.toByte()
            intToLE(16).copyInto(buf, 16)
            buf[20]=1; buf[21]=0         // PCM
            buf[22]=1; buf[23]=0         // Mono
            intToLE(sampleRate).copyInto(buf, 24)
            intToLE(sampleRate * 2).copyInto(buf, 28)
            buf[32]=2; buf[33]=0         // Block align
            buf[34]=16; buf[35]=0        // Bits per sample
            buf[36]='d'.code.toByte(); buf[37]='a'.code.toByte(); buf[38]='t'.code.toByte(); buf[39]='a'.code.toByte()
            intToLE(dataSize).copyInto(buf, 40)
            pcm.copyInto(buf, 44)
        }
    }
    private fun intToLE(v: Int) = byteArrayOf((v and 0xFF).toByte(), (v shr 8 and 0xFF).toByte(), (v shr 16 and 0xFF).toByte(), (v shr 24 and 0xFF).toByte())

    // ── Status-Antworten ───────────────────────────────────────────────────────

    private suspend fun buildStatusResponse(): String {
        val net = db.networkDao().get()
        return buildString {
            append("Pandora-Status, Finn:\n")
            append("• Host: ${if (gate.isOpen) "Aktiv" else "Gesperrt"}\n")
            append("• Tor: ${if (net?.torActive == true) "Aktiv" else "Inaktiv"}\n")
            append("• WireGuard: ${if (net?.wireGuardActive == true) "Aktiv" else "Inaktiv"}\n")
            append("• Mesh-Knoten: ${net?.meshNodeCount ?: 0}\n")
            append("• BT-Mesh: ${if (net?.bluetoothMeshActive == true) "Aktiv" else "Inaktiv"}")
        }
    }

    private suspend fun buildMeshResponse(): String {
        val nodes = db.meshDao().activeNodes()
        return "Mesh-Netzwerk: ${nodes.size} aktive Knoten. ${nodes.joinToString(", ") { it.nodeId.take(8) }}"
    }

    private suspend fun buildSecurityResponse(): String {
        val events = db.securityDao().recent()
        return "Sicherheits-Log geladen. Letzte Ereignisse werden im Dashboard angezeigt."
    }

    private suspend fun saveTask(command: String, response: String, deviceId: String) {
        db.jayJayDao().upsert(JayJayTask(
            taskId = security.randomHex(8),
            type = "voice_command",
            input = "$deviceId: $command",
            output = response,
            status = "completed",
        ))
    }

    fun isActivated() = _state.value.isActivated
    fun isCeoPresent() = _state.value.ceoPresent

    fun shutdown() { scope.cancel() }
}
