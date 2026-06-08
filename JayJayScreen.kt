package com.pandora.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pandora.jayjay.JayJayEngine
import com.pandora.jayjay.JayJayLearningEngine
import com.pandora.jayjay.VoicePrintManager
import org.koin.compose.koinInject

@Composable
fun JayJayScreen(onBack: () -> Unit) {
    val engine: JayJayEngine = koinInject()
    val learning: JayJayLearningEngine = koinInject()
    val voicePrint: VoicePrintManager = koinInject()

    val state by engine.state.collectAsState()
    val bg = Color(0xFF0A0A0F)
    val purple = Color(0xFFE040FB)
    val green = Color(0xFF00E676)

    var textInput by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val stats = remember { learning.getStats() }

    Surface(modifier = Modifier.fillMaxSize(), color = bg) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top Bar ────────────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                Spacer(Modifier.width(8.dp))
                Text("◈ JayJay", color = purple, fontSize = 22.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                Spacer(Modifier.weight(1f))
                // Voice-Status
                Surface(shape = CircleShape, color = if (state.ceoPresent) green.copy(0.2f) else Color(0xFF1A1A2E),
                    border = BorderStroke(1.dp, if (state.ceoPresent) green else Color.Gray)) {
                    Text(if (state.isListening) "🎤 Hört..." else if (state.isActivated) "● Aktiv" else "○ Standby",
                        color = if (state.isActivated) green else Color.Gray,
                        fontSize = 11.sp, modifier = Modifier.padding(8.dp, 4.dp))
                }
            }

            // ── Tabs ───────────────────────────────────────────────────────
            TabRow(selectedTabIndex = selectedTab, containerColor = Color(0xFF12121A), contentColor = purple) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) { Text("Chat", modifier = Modifier.padding(12.dp)) }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) { Text("Lernen", modifier = Modifier.padding(12.dp)) }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) { Text("Stimme", modifier = Modifier.padding(12.dp)) }
            }

            when (selectedTab) {

                // ── Chat ───────────────────────────────────────────────────
                0 -> {
                    LazyColumn(modifier = Modifier.weight(1f).padding(16.dp), reverseLayout = false) {
                        item {
                            ChatBubble("JayJay", "Hallo Finn! Ich bin JayJay, deine persönliche KI-Assistentin. Sage \"Pandemonium\" um mich zu aktivieren oder schreib mir hier.", purple)
                            Spacer(Modifier.height(8.dp))
                        }
                        items(chatMessages) { (role, msg) ->
                            ChatBubble(role, msg, if (role == "Du") Color(0xFF333355) else purple.copy(0.8f))
                            Spacer(Modifier.height(8.dp))
                        }
                        if (state.lastResponse.isNotBlank()) {
                            item {
                                ChatBubble("JayJay", state.lastResponse, purple.copy(0.8f))
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = textInput, onValueChange = { textInput = it },
                            placeholder = { Text("Schreibe JayJay...", color = Color.Gray) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = purple, unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = {
                            if (textInput.isNotBlank()) {
                                chatMessages = chatMessages + ("Du" to textInput)
                                textInput = ""
                            }
                        }, modifier = Modifier.size(48.dp).background(purple, CircleShape)) {
                            Icon(Icons.Default.Send, null, tint = Color.White)
                        }
                    }
                }

                // ── Lernen ─────────────────────────────────────────────────
                1 -> {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
                        Text("Wissens-Datenbank", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("JayJay lernt alles was du ihr auftragst – automatisch", color = Color.Gray, fontSize = 12.sp)
                        Spacer(Modifier.height(16.dp))

                        // Stats
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatCard("Wissen", "${stats["knowledgeEntries"]}", purple, Modifier.weight(1f))
                            StatCard("Befehle", "${stats["customCommands"]}", Color(0xFF00E676), Modifier.weight(1f))
                            StatCard("Themen", "${stats["interestTopics"]}", Color(0xFFFFD700), Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(16.dp))

                        // Lern-Beispiele
                        Text("Sprachbefehle zum Lernen:", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        listOf(
                            "\"JayJay, recherchiere über Bitcoin\"",
                            "\"JayJay, lerne: Das Pandora-Netz hat 5 Knoten\"",
                            "\"JayJay, wenn ich 'Status' sage, zeig mir das Dashboard\"",
                            "\"JayJay, suche nach neuen Infos über Mesh-Routing\"",
                        ).forEach { example ->
                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF1A1A2E),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text(example, color = purple, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                            }
                        }

                        @Suppress("UNCHECKED_CAST")
                        val topTopics = stats["topTopics"] as? List<String> ?: emptyList()
                        if (topTopics.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            Text("Meistgenutzte Themen:", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            topTopics.forEach { topic ->
                                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text("• ", color = purple)
                                    Text(topic, color = Color.White, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                // ── Stimme ─────────────────────────────────────────────────
                2 -> {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
                        Text("Stimm-Fingerabdruck", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("JayJay erkennt Finn's Stimme auf jedem Pandora-Gerät", color = Color.Gray, fontSize = 12.sp)
                        Spacer(Modifier.height(16.dp))

                        val hasPrint = voicePrint.hasPrint()
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)), shape = RoundedCornerShape(12.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (hasPrint) "✓" else "○", color = if (hasPrint) green else Color.Gray, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (hasPrint) "Voice-Print vorhanden" else "Kein Voice-Print", color = Color.White, fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("CEO: Finn Jona Lischke", color = Color.Gray, fontSize = 12.sp)
                                Text("Aktivierungswort: \"Pandemonium\"", color = Color.Gray, fontSize = 12.sp)
                                Text("Verteilt auf: alle Pandora-Geräte", color = Color.Gray, fontSize = 12.sp)
                                Text("Gespeichert: M.2 SSD (exFAT) verschlüsselt", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        if (!hasPrint) {
                            Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = purple),
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                                Icon(Icons.Default.Mic, null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Stimme einlernen (3 Samples)", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        Text("So funktioniert die Erkennung:", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        listOf(
                            "1. Mikrofon läuft dauerhaft im Hintergrund (VAD)",
                            "2. Nur bei Sprache oberhalb Schwellwert → Analyse",
                            "3. MFCC-Vektorvergleich mit deinem Voice-Print",
                            "4. Konfidenz > 82%? → CEO erkannt",
                            "5. Funktioniert auch bei gesperrtem Bildschirm",
                            "6. Voice-Print wird auf M.2 SSD (exFAT) gespeichert",
                            "7. Verteilung an alle Pandora-Geräte via Mesh",
                        ).forEach {
                            Text(it, color = Color.White.copy(0.8f), fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(sender: String, message: String, color: Color) {
    val isJayJay = sender == "JayJay"
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isJayJay) Arrangement.Start else Arrangement.End) {
        if (isJayJay) {
            Surface(shape = CircleShape, color = Color(0xFFE040FB).copy(0.2f), modifier = Modifier.size(32.dp)) {
                Text("J", color = Color(0xFFE040FB), fontWeight = FontWeight.Bold, modifier = Modifier.wrapContentSize(Alignment.Center))
            }
            Spacer(Modifier.width(8.dp))
        }
        Surface(shape = RoundedCornerShape(12.dp, 12.dp, if (isJayJay) 12.dp else 2.dp, if (isJayJay) 2.dp else 12.dp),
            color = if (isJayJay) Color(0xFF1A1A2E) else Color(0xFF2A2A4E), modifier = Modifier.widthIn(max = 280.dp)) {
            Text(message, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(10.dp, 8.dp))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color(0xFF12121A)),
        border = BorderStroke(1.dp, color.copy(0.3f)), shape = RoundedCornerShape(10.dp)) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = color, fontWeight = FontWeight.Black, fontSize = 22.sp)
            Text(label, color = Color.Gray, fontSize = 10.sp)
        }
    }
}
