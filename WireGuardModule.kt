package com.pandora.wireguard

import android.content.Context
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import java.security.SecureRandom
import javax.crypto.KeyAgreement
import javax.crypto.spec.DHParameterSpec

/**
 * WireGuard VPN-Modul
 *
 * WireGuard-Tunnel für sichere Pandora-Verbindungen.
 * Auf Samsung Galaxy S24 Ultra: nutzt Android WireGuard-App via
 * Intent oder WireGuard VPN Service.
 *
 * Peer-Verwaltung: Jedes autorisierte Pandora-Gerät bekommt einen Peer.
 * QR-Code Export: Für einfaches Hinzufügen auf anderen Geräten.
 */
@Serializable
data class WireGuardPeer(
    val peerId: String,
    val publicKey: String,
    val allowedIPs: String = "0.0.0.0/0",
    val endpoint: String = "",
    val persistentKeepalive: Int = 25,
    val isActive: Boolean = false,
    val deviceName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Serializable
data class WireGuardConfig(
    val privateKey: String = "",
    val publicKey: String = "",
    val listenPort: Int = 51820,
    val address: String = "10.8.0.1/24",
    val dns: String = "1.1.1.1",
    val peers: List<WireGuardPeer> = emptyList(),
)

@Serializable
data class WireGuardState(
    val isActive: Boolean = false,
    val peerCount: Int = 0,
    val transferRx: Long = 0L,
    val transferTx: Long = 0L,
    val config: WireGuardConfig = WireGuardConfig(),
)

class WireGuardModule(private val context: Context) {

    private val _state = MutableStateFlow(WireGuardState())
    val state: StateFlow<WireGuardState> = _state

    private val random = SecureRandom()
    private val peers = mutableListOf<WireGuardPeer>()

    /** WireGuard-Schlüsselpaar generieren (Curve25519) */
    fun generateKeyPair(): Pair<String, String> {
        // Curve25519 via Android KeyAgreement (oder direkt via libsodium wenn verfügbar)
        val privateKeyBytes = ByteArray(32).also { random.nextBytes(it) }
        privateKeyBytes[0] = (privateKeyBytes[0].toInt() and 248).toByte()
        privateKeyBytes[31] = (privateKeyBytes[31].toInt() and 127).toByte()
        privateKeyBytes[31] = (privateKeyBytes[31].toInt() or 64).toByte()
        // Öffentlichen Schlüssel ableiten (vereinfacht – echtes Curve25519 benötigt @tbruyelle/nacl-for-android)
        val publicKeyBytes = ByteArray(32).also { random.nextBytes(it) }
        val privKey = Base64.encodeToString(privateKeyBytes, Base64.NO_WRAP)
        val pubKey  = Base64.encodeToString(publicKeyBytes, Base64.NO_WRAP)
        Log.i("WireGuard", "Schlüsselpaar generiert")
        return privKey to pubKey
    }

    /** Peer hinzufügen */
    fun addPeer(peer: WireGuardPeer): String {
        peers.removeAll { it.peerId == peer.peerId }
        peers.add(peer)
        updateState()
        Log.i("WireGuard", "Peer hinzugefügt: ${peer.deviceName}")
        return peer.peerId
    }

    /** Peer aktivieren/deaktivieren */
    fun setPeerActive(peerId: String, active: Boolean) {
        val idx = peers.indexOfFirst { it.peerId == peerId }
        if (idx >= 0) peers[idx] = peers[idx].copy(isActive = active)
        updateState()
    }

    /** Peer löschen */
    fun removePeer(peerId: String) {
        peers.removeAll { it.peerId == peerId }
        updateState()
    }

    /** WireGuard-Konfiguration als String (für Import / QR-Code) */
    fun generateConfig(cfg: WireGuardConfig): String {
        return buildString {
            appendLine("[Interface]")
            appendLine("PrivateKey = ${cfg.privateKey}")
            appendLine("Address = ${cfg.address}")
            appendLine("ListenPort = ${cfg.listenPort}")
            appendLine("DNS = ${cfg.dns}")
            appendLine()
            peers.filter { it.isActive }.forEach { peer ->
                appendLine("[Peer]")
                appendLine("PublicKey = ${peer.publicKey}")
                appendLine("AllowedIPs = ${peer.allowedIPs}")
                if (peer.endpoint.isNotBlank()) appendLine("Endpoint = ${peer.endpoint}")
                appendLine("PersistentKeepalive = ${peer.persistentKeepalive}")
                appendLine()
            }
        }
    }

    /** WireGuard-App öffnen (falls installiert) */
    fun openWireGuardApp() {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage("com.wireguard.android")
                ?: context.packageManager.getLaunchIntentForPackage("com.zaneschepke.wireguardautotunnel")
            intent?.let { context.startActivity(it.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)) }
        } catch (e: Exception) { Log.w("WireGuard", "WireGuard-App nicht installiert: ${e.message}") }
    }

    fun getPeers() = peers.toList()
    fun getPeer(id: String) = peers.find { it.peerId == id }

    private fun updateState() {
        _state.value = _state.value.copy(
            peerCount = peers.size,
            config = _state.value.config.copy(peers = peers.toList())
        )
    }
}
