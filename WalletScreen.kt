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

@Composable
fun WalletScreen(onBack: () -> Unit) {
    val bg = Color(0xFF0A0A0F); val gold = Color(0xFFFFD700)
    Surface(Modifier.fillMaxSize(), color = bg) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                Spacer(Modifier.width(8.dp))
                Text("₿ Bitcoin Wallet", color = gold, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
            Column(Modifier.fillMaxSize().padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF12121A)),
                    border = BorderStroke(1.dp, gold.copy(0.4f)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Hardware Wallet", color = gold, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        listOf(
                            "Typ" to "BIP39/BIP44 + Ledger Nano S/X",
                            "Netzwerk" to "Bitcoin Mainnet",
                            "Sicherheit" to "Android Keystore (Hardware-backed)",
                            "Signing" to "Private Key verlässt Ledger NIE",
                            "Broadcast" to "via Mempool.space über Tor",
                            "Gespeichert" to "M.2 SSD (exFAT) AES-256-GCM",
                        ).forEach { (k, v) ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                                Text(k, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Text(v, color = Color.White, fontSize = 12.sp)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = gold),
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                            Text("Nächste Adresse ableiten", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShopScreen(onBack: () -> Unit) {
    val bg = Color(0xFF0A0A0F); val green = Color(0xFF4CAF50)
    Surface(Modifier.fillMaxSize(), color = bg) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                Spacer(Modifier.width(8.dp))
                Text("Pandora Shop", color = green, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingCart, null, tint = green, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Shop aktiv – Produkte im Admin anlegen", color = Color.Gray)
                }
            }
        }
    }
}
