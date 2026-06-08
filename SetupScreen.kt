package com.pandora.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pandora.core.AppMode
import com.pandora.core.ConfigManager
import com.pandora.core.SetupStatus

@Composable
fun SetupScreen(
    config: ConfigManager,
    onRequestSsdPicker: () -> Unit,
    onSetupComplete: () -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    var selectedMode by remember { mutableStateOf(AppMode.CEO_HOST) }
    var ceoName by remember { mutableStateOf("Finn Jona Lischke") }
    var ceoPin by remember { mutableStateOf("") }
    var pinConfirm by remember { mutableStateOf("") }

    val bg = Color(0xFF0A0A0F)
    val accent = Color(0xFFE040FB)
    val gold = Color(0xFFFFD700)

    Surface(modifier = Modifier.fillMaxSize(), color = bg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(40.dp))

            Text("◈ PANDORA", color = accent, fontSize = 32.sp, fontWeight = FontWeight.Black, letterSpacing = 6.sp)
            Text("v1.0.0", color = Color.Gray, fontSize = 12.sp)
            Spacer(Modifier.height(32.dp))

            when (step) {
                // ── Schritt 0: Modus wählen ────────────────────────────────
                0 -> {
                    Text("Gerätemodus wählen", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(24.dp))
                    AppMode.values().forEach { mode ->
                        val isSelected = selectedMode == mode
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { selectedMode = mode },
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) accent.copy(alpha = 0.2f) else Color(0xFF1A1A2E)),
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) accent else Color.Gray.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(modeIcon(mode), fontSize = 24.sp)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(modeLabel(mode), color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Text(modeDesc(mode), color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    PandoraButton("Weiter →", accent) {
                        config.appMode = selectedMode
                        step = if (selectedMode == AppMode.CEO_HOST) 1 else 3
                    }
                }

                // ── Schritt 1: CEO registrieren ────────────────────────────
                1 -> {
                    Text("CEO registrieren", color = gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Samsung Galaxy S24 Ultra = Root of Trust", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(24.dp))
                    OutlinedTextField(value = ceoName, onValueChange = { ceoName = it }, label = { Text("CEO Name", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(), colors = pandoraTextFieldColors())
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = ceoPin, onValueChange = { ceoPin = it }, label = { Text("CEO PIN (min. 6 Stellen)", color = Color.Gray) },
                        visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), colors = pandoraTextFieldColors())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = pinConfirm, onValueChange = { pinConfirm = it }, label = { Text("PIN bestätigen", color = Color.Gray) },
                        visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), colors = pandoraTextFieldColors())
                    Spacer(Modifier.height(24.dp))
                    PandoraButton("CEO registrieren", gold) {
                        if (ceoPin.length >= 6 && ceoPin == pinConfirm) {
                            config.ceoName = ceoName
                            config.hostPin = ceoPin
                            config.updateSetupStatus(SetupStatus.CEO_REGISTERED)
                            step = 2
                        }
                    }
                }

                // ── Schritt 2: M.2 SSD (exFAT) ────────────────────────────
                2 -> {
                    Text("M.2 SSD freigeben", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("📋 Anleitung", color = accent, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            listOf(
                                "1. M.2 SSD via USB-C OTG-Adapter anschließen",
                                "2. Formatierung: exFAT ✓ (bereits korrekt)",
                                "3. Unten auf 'SSD auswählen' tippen",
                                "4. Im Datei-Browser den SSD-Ordner auswählen",
                                "5. Berechtigung erteilen",
                            ).forEach { Text(it, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, modifier = Modifier.padding(vertical = 2.dp)) }
                            Spacer(Modifier.height(8.dp))
                            Text("ℹ exFAT: SQLite läuft im internen Speicher, alle anderen Daten auf SSD", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    PandoraButton("📂 SSD auswählen (exFAT)", accent) { onRequestSsdPicker() }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { onSetupComplete() }, modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color.Gray)) {
                        Text("Ohne SSD fortfahren (Demo-Modus)", color = Color.Gray)
                    }
                }

                // ── Schritt 3: Client-Setup ────────────────────────────────
                3 -> {
                    Text("Client einrichten", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Text("Verbinde dich mit dem Pandora CEO-Host-Netzwerk", color = Color.Gray, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(24.dp))
                    PandoraButton("Fertig", accent) { onSetupComplete() }
                }
            }
        }
    }
}

@Composable
private fun PandoraButton(text: String, color: Color, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun pandoraTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFFE040FB), unfocusedBorderColor = Color.Gray,
    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
)

private fun modeIcon(mode: AppMode) = when(mode) {
    AppMode.CEO_HOST     -> "👑"
    AppMode.CLIENT       -> "📱"
    AppMode.MESH_NODE    -> "🔗"
    AppMode.SENSING_NODE -> "📡"
    AppMode.COMPUTE_NODE -> "⚙️"
    AppMode.RELAY_NODE   -> "↔️"
}
private fun modeLabel(mode: AppMode) = when(mode) {
    AppMode.CEO_HOST     -> "CEO Host Mode"
    AppMode.CLIENT       -> "Client Mode"
    AppMode.MESH_NODE    -> "Mesh-Knoten"
    AppMode.SENSING_NODE -> "Sensing-Knoten"
    AppMode.COMPUTE_NODE -> "Compute-Knoten"
    AppMode.RELAY_NODE   -> "Relay-Knoten"
}
private fun modeDesc(mode: AppMode) = when(mode) {
    AppMode.CEO_HOST     -> "Samsung Galaxy S24 Ultra · Root of Trust · JayJay"
    AppMode.CLIENT       -> "Shop, Checkout, Chat, QR-Scan"
    AppMode.MESH_NODE    -> "Netzwerk-Weiterleitungsknoten"
    AppMode.SENSING_NODE -> "RSSI, BLE, Bewegungs-Scanning"
    AppMode.COMPUTE_NODE -> "Rechenleistung für JayJay-Aufgaben"
    AppMode.RELAY_NODE   -> "Onion-Mesh Relay"
}
