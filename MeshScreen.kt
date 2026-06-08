package com.pandora.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import com.pandora.mesh.BluetoothMeshManager
import com.pandora.mesh.WifiMeshManager
import com.pandora.onion.OnionModule
import com.pandora.wireguard.WireGuardModule
import org.koin.compose.koinInject

@Composable
fun MeshScreen(onBack: () -> Unit) {
    val btMesh: BluetoothMeshManager = koinInject()
    val wifiMesh: WifiMeshManager = koinInject()
    val onion: OnionModule = koinInject()
    val wg: WireGuardModule = koinInject()

    val btState by btMesh.state.collectAsState()
    val wgState by wg.state.collectAsState()
    val onionState by onion.state.collectAsState()

    val bg = Color(0xFF0A0A0F)
    val cyan = Color(0xFF00BCD4)

    Surface(Modifier.fillMaxSize(), color = bg) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                Spacer(Modifier.width(8.dp))
                Text("Mesh & Netzwerk", color = cyan, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
            Column(Modifier.fillMaxSize().padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {

                NetworkCard("🔵 Bluetooth Mesh", listOf(
                    "Status" to if (btState.advertising) "Aktiv (Advertising)" else "Inaktiv",
                    "Peers" to "${btState.peers.size} gefunden",
                    "Empfangen" to "${btState.messagesReceived} Nachrichten",
                ), cyan,
                    action = "Starten" to { btMesh.startAdvertising("pandora"); btMesh.startScanning() })

                Spacer(Modifier.height(12.dp))

                NetworkCard("📡 WiFi Mesh", listOf(
                    "WiFi Direct" to if (wgState.isActive) "Aktiv" else "Inaktiv",
                    "Verbindungsqualität" to "${wifiMesh.linkQuality()}%",
                ), Color(0xFF4CAF50),
                    action = "Scannen" to { wifiMesh.discoverPeers() })

                Spacer(Modifier.height(12.dp))

                NetworkCard("🧅 Onion / Tor", listOf(
                    "Status" to if (onionState.isActive) "Aktiv" else "Inaktiv",
                    "Orbot" to if (onion.isOrbotInstalled()) "Installiert ✓" else "Nicht installiert",
                    "Onion-Adresse" to (onionState.onionAddress.ifBlank { "–" }),
                ), Color(0xFF69F0AE),
                    action = "Verbinden" to { onion.startOrbot() })

                Spacer(Modifier.height(12.dp))

                NetworkCard("🔒 WireGuard VPN", listOf(
                    "Status" to if (wgState.isActive) "Aktiv" else "Inaktiv",
                    "Peers" to "${wgState.peerCount}",
                    "Port" to "${wgState.config.listenPort}",
                ), Color(0xFF40C4FF),
                    action = "Öffnen" to { wg.openWireGuardApp() })

                Spacer(Modifier.height(12.dp))

                // Pandora Onion Mesh
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF12121A)),
                    border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(0.4f)),
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text("🌐 Pandora Onion Mesh", color = Color(0xFF7C4DFF), fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Internes Tor-ähnliches Routing nur für Pandora-Daten", color = Color.Gray, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        listOf("Layered Encryption", "Hop-by-Hop Routing", "Store-and-Forward", "Replay-Schutz", "Route Rotation alle 10 Min").forEach {
                            Row(Modifier.padding(vertical = 1.dp)) {
                                Text("• ", color = Color(0xFF7C4DFF))
                                Text(it, color = Color.White.copy(0.8f), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkCard(title: String, stats: List<Pair<String, String>>, color: Color, action: Pair<String, () -> Unit>) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF12121A)),
        border = BorderStroke(1.dp, color.copy(0.3f)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = color, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))
            stats.forEach { (k, v) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Text(k, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text(v, color = Color.White, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = action.second, border = BorderStroke(1.dp, color),
                shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)) {
                Text(action.first, color = color, fontSize = 13.sp)
            }
        }
    }
}
