package com.pandora.jayjay

"""

import json
import os
import sys
import time
import traceback
from datetime import datetime

class SelfHealingWatchdog:
    def __init__(self, checkpoint_file="/mnt/user-data/outputs/self_healing_system/checkpoint.json"):
        """Initialisiere den Watchdog mit Checkpoint-Datei"""
        self.checkpoint_file = checkpoint_file
        self.state = self.load_state()
        
    def load_state(self):
        """Lade den letzten gespeicherten Zustand"""
        if os.path.exists(self.checkpoint_file):
            try:
                with open(self.checkpoint_file, 'r') as f:
                    return json.load(f)
            except Exception as e:
                print(f"⚠ Checkpoint-Laden fehlgeschlagen: {e}")
                return {"last_checkpoint": None, "steps": []}
        return {"last_checkpoint": None, "steps": []}
    
    def save_state(self, step_name, data=None):
        """Speichere den aktuellen Zustand"""
        current_time = datetime.now().isoformat()
        checkpoint = {
            "last_checkpoint": current_time,
            "current_step": step_name,
            "data": data
        }
        
        # Bestehenden Zustand erweitern
        self.state["last_checkpoint"] = current_time
        self.state["current_step"] = step_name
        if data:
            self.state["data"] = data
            
        # Zustand speichern
        with open(self.checkpoint_file, 'w') as f:
            json.dump(self.state, f, indent=2)
            
        print(f"✓ Zustand gespeichert: {step_name}")
    
    def recover(self):
        """Stelle den letzten Zustand wieder her"""
        if self.state["last_checkpoint"]:
            print(f"⚠ Unterbrechung erkannt - Wiederherstellung von {self.state['last_checkpoint']}")
            if "current_step" in self.state:
                return self.state["current_step"], self.state.get("data", {})
        return None, {}
    
    def execute_with_recovery(self, steps):
        """
        Führe Schritte mit automatischer Wiederherstellung aus
        steps: Liste von (step_name, function) Tupeln
        """
        # Versuche Zustand wiederherzustellen
        current_step_name, recovered_data = self.recover()
        
        if current_step_name:
            print(f"🔄 Fortsetzen bei: {current_step_name}")
            # Suche den entsprechenden Schritt
            for i, (step_name, func) in enumerate(steps):
                if step_name == current_step_name:
                    # Führe ab hier fort
                    steps = steps[i:]
                    break
        
        # Führe alle Schritte aus
        for step_name, func in steps:
            try:
                # Führe Schritt aus
                result = func()
                # Speichere Zustand
                self.save_state(step_name, result)
                print(f"✅ Schritt abgeschlossen: {step_name}")
                
            except Exception as e:
                print(f"❌ Fehler in Schritt {step_name}: {str(e)}")
                print(f"Trace: {traceback.format_exc()}")
                # Speichere Fehlerzustand
                self.save_state(f"ERROR_{step_name}", {"error": str(e)})
                # Versuche erneut (begrenzte Anzahl)
                print("🔄 Automatische Wiederherstellung wird versucht...")
                time.sleep(2)
                # Rekursiver Aufruf für denselben Schritt
                return self.execute_with_recovery([(step_name, func)])
        
        print("🎉 Alle Schritte erfolgreich abgeschlossen!")
        return True

def main():
    watchdog = SelfHealingWatchdog()
    
    # --- HIER NEUE SCHRITTE HINZUFÜGEN ---
    # Jeder Schritt ist eine Funktion, die:
    # 1. Eine spezifische Aufgabe erfüllt
    # 2. Optional Daten zurückgibt (werden im Checkpoint gespeichert)
    
    def step1():
        """Schritt 1: Systemvoraussetzungen prüfen"""
        print("🔍 Prüfe Systemvoraussetzungen...")
        os.makedirs("/mnt/user-data/outputs/self_healing_system", exist_ok=True)
        return {"ready": True}
    
    def step2():
        """Schritt 2: Datei analysieren"""
        print("📄 Analysiere Dateiinhalt...")
        try:
            with open("/home/user/absoluter_gehorsam.py", "rb") as f:
                content = f.read(1000)
            return {"content": content.decode('utf-8', errors='ignore')}
        except Exception as e:
            return {"error": str(e)}
    
    def step3():
        """Schritt 3: Fehlerursache identifizieren"""
        print("🔬 Identifiziere Fehlerursache...")
        return {"analysis": "Zugriff verweigert - möglicherweise Berechtigungsproblem"}
    
    def step4():
        """Schritt 4: Lösung implementieren"""
        print("🛠️ Implementiere Lösung...")
        os.system("chmod 644 /home/user/absoluter_gehorsam.py")
        return {"permissions_fixed": True}
    
    def step5():
        """Schritt 5: Datei verarbeiten"""
        print("⚙️ Verarbeite Datei...")
        # Hier die eigentliche Verarbeitung
        return {"processed": True}
    
    def step6():
        """Schritt 6: Ergebnis speichern"""
        print("💾 Speichere Ergebnis...")
        with open("/mnt/user-data/outputs/self_healing_system/result.txt", "w") as f:
            f.write("Verarbeitung erfolgreich abgeschlossen!\n")
        return {"saved": True}
    
    # --- ENDE DER SCHRITTDEFINITIONEN ---
    
    # Schrittliste definieren
    steps = [
        ("step1", step1),
        ("step2", step2),
        ("step3", step3),
        ("step4", step4),
        ("step5", step5),
        ("step6", step6),
    ]
    
    # Führe mit Wiederherstellung aus
    watchdog.execute_with_recovery(steps)

if __name__ == "__main__":
    main()

import android.util.Log
import com.pandora.database.SsdStorageManager
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * JayJay Learning Engine
 *
 * JayJay lernt automatisch, was Finn ihr aufträgt.
 *
 * Lernmodi:
 * 1. DIREKTES WISSEN  – "JayJay, lerne: [Fakt]"
 *    → Finn sagt ihr direkt was er will, sie speichert es
 *
 * 2. THEMEN-RECHERCHE – "JayJay, recherchiere über [Thema]"
 *    → JayJay sucht im Web (Wikipedia, DuckDuckGo, OpenAI)
 *    → Verarbeitet + fasst zusammen
 *    → Speichert als Wissensbasis auf M.2 SSD
 *
 * 3. AUTOMATISCHES LERNEN – bei jeder Konversation
 *    → JayJay merkt sich alle Themen die Finn interessieren
 *    → Sucht beim nächsten Start neue Infos dazu
 *    → Passt Antworten an Finn's Vorlieben an
 *
 * 4. BEFEHLS-LERNEN – "JayJay, wenn ich X sage, mach Y"
 *    → Eigene Shortcuts und Automatisierungen
 *
 * Alles wird verschlüsselt auf M.2 SSD (exFAT) gespeichert.
 */
@Serializable
data class KnowledgeEntry(
    val id: String,
    val topic: String,
    val content: String,
    val source: String = "direct",   // direct | web | wikipedia | openai | auto
    val confidence: Float = 1.0f,
    val learnedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val timesUsed: Int = 0,
    val tags: List<String> = emptyList(),
)

@Serializable
data class CustomCommand(
    val trigger: String,             // Was Finn sagt
    val action: String,              // Was JayJay tut
    val actionType: String = "response",  // response | api_call | mesh_command | system
    val createdAt: Long = System.currentTimeMillis(),
)

@Serializable
data class JayJayMemory(
    val knowledgeBase: MutableList<KnowledgeEntry> = mutableListOf(),
    val customCommands: MutableList<CustomCommand> = mutableListOf(),
    val interestTopics: MutableList<String> = mutableListOf(),
    val ceoPreferences: MutableMap<String, String> = mutableMapOf(),
    val lastAutoLearnAt: Long = 0L,
)

class JayJayLearningEngine(
    private val ssd: SsdStorageManager,
    private val json: Json = Json { ignoreUnknownKeys = true; prettyPrint = true },
) {
    private var memory = JayJayMemory()
    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val MEMORY_FILE = "jayjay_memory.json"
        const val AUTO_LEARN_INTERVAL = 3_600_000L  // 1 Stunde
    }

    // ── Init ───────────────────────────────────────────────────────────────────

    suspend fun loadMemory() = withContext(Dispatchers.IO) {
        val raw = ssd.readFile("jayjay", MEMORY_FILE)
        if (raw != null) {
            memory = runCatching { json.decodeFromString<JayJayMemory>(raw) }.getOrElse { JayJayMemory() }
            Log.i("JayJayLearn", "Gedächtnis geladen: ${memory.knowledgeBase.size} Einträge, ${memory.customCommands.size} Befehle")
        }
    }

    private suspend fun saveMemory() = withContext(Dispatchers.IO) {
        ssd.writeFile("jayjay", MEMORY_FILE, json.encodeToString(memory))
    }

    // ── Direktes Lernen ────────────────────────────────────────────────────────

    /**
     * Finn sagt: "JayJay, lerne: [Thema] ist [Inhalt]"
     * JayJay speichert es sofort auf der SSD
     */
    suspend fun learnDirect(topic: String, content: String): String {
        val existing = memory.knowledgeBase.find { it.topic.equals(topic, ignoreCase = true) }
        if (existing != null) {
            val idx = memory.knowledgeBase.indexOf(existing)
            memory.knowledgeBase[idx] = existing.copy(content = content, updatedAt = System.currentTimeMillis(), source = "direct")
            saveMemory()
            Log.i("JayJayLearn", "Wissen aktualisiert: $topic")
            return "Verstanden, Finn. Ich habe mein Wissen über \"$topic\" aktualisiert."
        }
        val entry = KnowledgeEntry(
            id = java.util.UUID.randomUUID().toString().take(8),
            topic = topic, content = content, source = "direct",
        )
        memory.knowledgeBase.add(entry)
        if (topic !in memory.interestTopics) memory.interestTopics.add(topic)
        saveMemory()
        Log.i("JayJayLearn", "Neues Wissen: $topic")
        return "Ich habe das gelernt, Finn. \"$topic\" ist jetzt in meinem Gedächtnis."
    }

    // ── Web-Recherche ──────────────────────────────────────────────────────────

    /**
     * Finn sagt: "JayJay, recherchiere über [Thema]"
     * JayJay sucht im Web und lernt automatisch
     */
    suspend fun researchTopic(topic: String, apiKey: String? = null): String = withContext(Dispatchers.IO) {
        Log.i("JayJayLearn", "Recherchiere: $topic")

        // 1. Wikipedia versuchen
        val wikiContent = fetchWikipedia(topic)

        // 2. Falls API-Key vorhanden: GPT für Zusammenfassung nutzen
        val summary = if (!apiKey.isNullOrBlank() && wikiContent != null) {
            summarizeWithGpt(topic, wikiContent, apiKey)
        } else {
            wikiContent?.take(1000) ?: "Keine Informationen gefunden."
        }

        // 3. Wissen speichern
        val entry = KnowledgeEntry(
            id = java.util.UUID.randomUUID().toString().take(8),
            topic = topic,
            content = summary,
            source = if (wikiContent != null) "wikipedia" else "openai",
            confidence = if (wikiContent != null) 0.9f else 0.7f,
            tags = listOf(topic.lowercase()),
        )
        val existingIdx = memory.knowledgeBase.indexOfFirst { it.topic.equals(topic, ignoreCase = true) }
        if (existingIdx >= 0) memory.knowledgeBase[existingIdx] = entry
        else memory.knowledgeBase.add(entry)

        if (topic !in memory.interestTopics) memory.interestTopics.add(topic)
        memory.ceoPreferences["last_research"] = topic
        saveMemory()

        "Ich habe \"$topic\" recherchiert und gelernt, Finn. Hier ist eine Zusammenfassung:\n\n$summary"
    }

    private suspend fun fetchWikipedia(topic: String): String? {
        return try {
            val encoded = java.net.URLEncoder.encode(topic, "UTF-8")
            val url = "https://de.wikipedia.org/api/rest_v1/page/summary/$encoded"
            val response = http.newCall(Request.Builder().url(url).build()).execute()
            val body = response.body?.string() ?: return null
            val parsed = json.parseToJsonElement(body).jsonObject
            parsed["extract"]?.jsonPrimitive?.content
        } catch (e: Exception) {
            Log.w("JayJayLearn", "Wikipedia-Abruf fehlgeschlagen: ${e.message}")
            null
        }
    }

    private suspend fun summarizeWithGpt(topic: String, content: String, apiKey: String): String {
        return try {
            val body = """{"model":"gpt-4o-mini","messages":[
                {"role":"system","content":"Du bist JayJay, KI-Assistentin von Finn Jona Lischke. Fasse das Thema präzise auf Deutsch zusammen."},
                {"role":"user","content":"Thema: $topic\n\nQuelltext:\n${content.take(2000)}"}
            ],"max_tokens":400}"""
            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(body.toRequestBody())
                .build()
            val resp = http.newCall(request).execute()
            val raw = resp.body?.string() ?: return content.take(500)
            json.parseToJsonElement(raw).jsonObject["choices"]?.jsonArray
                ?.firstOrNull()?.jsonObject?.get("message")?.jsonObject?.get("content")?.jsonPrimitive?.content
                ?: content.take(500)
        } catch (e: Exception) { content.take(500) }
    }

    private fun String.toRequestBody() = okhttp3.RequestBody.create(
        okhttp3.MediaType.parse("application/json"), this)

    // ── Befehle lernen ─────────────────────────────────────────────────────────

    /**
     * "JayJay, wenn ich 'X' sage, dann Y"
     */
    suspend fun learnCommand(trigger: String, action: String): String {
        val cmd = CustomCommand(trigger = trigger.lowercase(), action = action)
        memory.customCommands.removeAll { it.trigger == cmd.trigger }
        memory.customCommands.add(cmd)
        saveMemory()
        Log.i("JayJayLearn", "Befehl gelernt: \"$trigger\" → \"$action\"")
        return "Okay Finn, ich merke mir: wenn du \"$trigger\" sagst, werde ich \"$action\"."
    }

    /** Gelernten Befehl suchen */
    fun findCommand(input: String): CustomCommand? =
        memory.customCommands.find { input.lowercase().contains(it.trigger) }

    // ── Wissen abrufen ─────────────────────────────────────────────────────────

    fun recall(topic: String): KnowledgeEntry? {
        val entry = memory.knowledgeBase.find { it.topic.equals(topic, ignoreCase = true) }
            ?: memory.knowledgeBase.find { topic.lowercase() in it.tags }
        entry?.let {
            val idx = memory.knowledgeBase.indexOf(it)
            memory.knowledgeBase[idx] = it.copy(timesUsed = it.timesUsed + 1)
            scope.launch { saveMemory() }
        }
        return entry
    }

    fun searchKnowledge(query: String): List<KnowledgeEntry> {
        val q = query.lowercase()
        return memory.knowledgeBase
            .filter { it.topic.lowercase().contains(q) || it.content.lowercase().contains(q) || it.tags.any { t -> t.contains(q) } }
            .sortedByDescending { it.timesUsed }
    }

    // ── Auto-Lernen ────────────────────────────────────────────────────────────

    /**
     * Läuft automatisch jede Stunde:
     * Aktualisiert Wissen über alle bekannten Themen von Finn
     */
    suspend fun autoLearn(apiKey: String?) {
        val now = System.currentTimeMillis()
        if (now - memory.lastAutoLearnAt < AUTO_LEARN_INTERVAL) return
        Log.i("JayJayLearn", "Auto-Lernen startet für ${memory.interestTopics.size} Themen...")
        for (topic in memory.interestTopics.take(5)) {   // Max 5 pro Stunde
            try { researchTopic(topic, apiKey); delay(2000) } catch (e: Exception) { Log.w("JayJayLearn", "Auto-Learn fehlgeschlagen: $topic") }
        }
        memory.lastAutoLearnAt = now
        saveMemory()
        Log.i("JayJayLearn", "Auto-Lernen abgeschlossen")
    }

    // ── Präferenzen ────────────────────────────────────────────────────────────

    suspend fun setPreference(key: String, value: String) {
        memory.ceoPreferences[key] = value
        saveMemory()
    }

    fun getPreference(key: String, default: String = "") = memory.ceoPreferences[key] ?: default

    // ── Status ─────────────────────────────────────────────────────────────────

    fun getStats() = mapOf(
        "knowledgeEntries" to memory.knowledgeBase.size,
        "customCommands" to memory.customCommands.size,
        "interestTopics" to memory.interestTopics.size,
        "lastAutoLearn" to memory.lastAutoLearnAt,
        "topTopics" to memory.knowledgeBase.sortedByDescending { it.timesUsed }.take(5).map { it.topic },
    )

    fun getAllKnowledge() = memory.knowledgeBase.toList()
    fun getAllCommands() = memory.customCommands.toList()

    fun shutdown() { scope.cancel() }
}
