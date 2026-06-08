package com.pandora.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pandora.core.ConfigManager
import com.pandora.visibility.HostVisibilityGate
import org.koin.compose.koinInject

@Composable
fun DashboardScreen(
    config: ConfigManager,
    onNavigateAdmin: () -> Unit,
    onNavigateJayJay: () -> Unit,
    onNavigateMesh: () -> Unit,
    onNavigateWallet: () -> Unit,
    onNavigateShop: () -> Unit,
) {
    val gate: HostVisibilityGate = koinInject()
    val gateState by gate.state.collectAsState()
    val bg = Color(0xFF0A0A0F)
    val accent = Color(0xFFE040FB)
    val green = Color(0xFF00E676)
    val red = Color(0xFFFF1744)

    Surface(modifier = Modifier.fillMaxSize(), color = bg) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {

            // ── Header ────────────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("◈ PANDORA", color = accent, fontSize = 22.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                    Text("Willkommen, ${config.ceoName.split(" ").first()}", color = Color.Gray, fontSize = 13.sp)
                }
                // Host-Status-Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (gateState.isOpen) green.copy(0.15f) else red.copy(0.15f),
                    border = BorderStroke(1.dp, if (gateState.isOpen) green else red),
                ) {
                    Text(
                        if (gateState.isOpen) "● HOST AKTIV" else "○ HOST GESPERRT",
                        color = if (gateState.isOpen) green else red,
                        fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Modus-Badge ───────────────────────────────────────────────
            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF1A1A2E)) {
                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("👑", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("CEO Host · Samsung Galaxy S24 Ultra · M.2 SSD (exFAT)", color = Color.Gray, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Host-Steuerung ────────────────────────────────────────────
            if (!gateState.isOpen) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("🔒 Host entsperren", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Sage \"Pandemonium\" oder gib deinen CEO-PIN ein", color = Color.Gray, fontSize = 12.sp)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { gate.unlock("biometric") },
                            colors = ButtonDefaults.buttonColors(containerColor = accent),
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                            Text("Entsperren", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Modul-Grid ────────────────────────────────────────────────
            Text("Module", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(520.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item { ModuleCard("JayJay KI", "Sprachsteuerung", Icons.Default.Psychology, Color(0xFFE040FB), onNavigateJayJay) }
                item { ModuleCard("Admin", "Geräte & Rollen", Icons.Default.AdminPanelSettings, Color(0xFFFF6D00), onNavigateAdmin) }
                item { ModuleCard("Mesh", "BT · WiFi · Onion", Icons.Default.Hub, Color(0xFF00BCD4), onNavigateMesh) }
                item { ModuleCard("₿ Wallet", "Bitcoin HW", Icons.Default.AccountBalanceWallet, Color(0xFFFFD700), onNavigateWallet) }
                item { ModuleCard("Shop", "Produkte & Kauf", Icons.Default.ShoppingCart, Color(0xFF4CAF50), onNavigateShop) }
                item { ModuleCard("Scan", "RSSI · BLE · CSI", Icons.Default.Radar, Color(0xFF26C6DA)) {} }
                item { ModuleCard("3D Gebäude", "Overlay & Heatmap", Icons.Default.Apartment, Color(0xFF7C4DFF)) {} }
                item { ModuleCard("Compute", "Mesh Computing", Icons.Default.Memory, Color(0xFFFF5252)) {} }
                item { ModuleCard("Onion", "Tor-Zugang", Icons.Default.Security, Color(0xFF69F0AE)) {} }
                item { ModuleCard("WireGuard", "VPN", Icons.Default.VpnLock, Color(0xFF40C4FF)) {} }
            }
        }
    }
}

@Composable
private fun ModuleCard(title: String, subtitle: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12121A)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().height(90.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(subtitle, color = Color.Gray, fontSize = 10.sp)
            }
        }
    }
}
