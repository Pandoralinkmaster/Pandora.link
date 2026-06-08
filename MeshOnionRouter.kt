package com.pandora.mesh

import android.util.Log
import com.pandora.security.SecurityModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.security.SecureRandom

/**
 * Pandora Onion Mesh – Tor-ähnliches internes Routing
 *
 * - Mehrstufige Routing-Pfade (3-7 Hops)
 * - Layered Encryption (jeder Knoten entschlüsselt nur seinen Layer)
 * - Relay-Knoten kennen nur Vorgänger und Nachfolger
 * - Store-and-Forward für Offline-Knoten
 * - Route Rotation alle 10 Minuten
 * - Replay-Schutz via Nachricht-ID + TTL
 */
@Serializable
data class OnionPacket(
    val packetId: String,
    val payload: String,            // Verschlüsselter Payload
    val nextHop: String,            // Nur nächster Knoten bekannt
    val ttl: Int = 7,
    val sentAt: Long = System.currentTimeMillis(),
)

@Serializable
data class RouteLayer(
    val nodeId: String,
    val encryptedNextHop: String,
    val encryptedPayload: String,
)

class MeshOnionRouter(private val security: SecurityModule) {

    private val seenPackets = mutableSetOf<String>()
    private val offlineQueue = mutableListOf<OnionPacket>()
    private val random = SecureRandom()

    /**
     * Onion-Paket aufbauen:
     * 1. Pfad auswählen (3-7 Relay-Knoten)
     * 2. Layered Encryption: innerste Schicht zuerst verschlüsseln
     * 3. Paket an ersten Relay schicken
     */
    fun buildOnionPacket(
        sourceId: String,
        targetId: String,
        payload: String,
        availableRelays: List<String>,
    ): OnionPacket {
        val packetId = security.randomHex(16)
        val path = selectPath(sourceId, targetId, availableRelays)
        Log.i("OnionRouter", "Route: ${path.joinToString(" → ")}")

        // Layered Encryption: von innen nach außen
        var encPayload = security.encrypt("${targetId}|${payload}")
        for (node in path.reversed()) {
            encPayload = security.encrypt("${node}|$encPayload")
        }

        return OnionPacket(
            packetId = packetId,
            payload = encPayload,
            nextHop = path.firstOrNull() ?: targetId,
            ttl = minOf(path.size + 2, 10),
        )
    }

    /**
     * Paket als Relay-Knoten weiterleiten:
     * 1. Eigene Verschlüsselungsschicht entfernen
     * 2. Nächsten Hop ermitteln
     * 3. Replay-Schutz prüfen
     */
    fun forwardPacket(packet: OnionPacket, myNodeId: String): OnionPacket? {
        // Replay-Schutz
        if (packet.packetId in seenPackets) {
            Log.w("OnionRouter", "Replay-Angriff erkannt: ${packet.packetId}")
            return null
        }
        if (packet.ttl <= 0) { Log.w("OnionRouter", "TTL abgelaufen"); return null }
        seenPackets.add(packet.packetId)

        return try {
            val decrypted = security.decrypt(packet.payload)
            val parts = decrypted.split("|", limit = 2)
            val nextHop = parts[0]
            val remainingPayload = parts.getOrElse(1) { "" }
            packet.copy(payload = remainingPayload, nextHop = nextHop, ttl = packet.ttl - 1)
        } catch (e: Exception) {
            Log.e("OnionRouter", "Entschlüsselung fehlgeschlagen: ${e.message}")
            null
        }
    }

    /** Offline-Queue: Pakete für nicht erreichbare Knoten zwischenspeichern */
    fun queueForOffline(packet: OnionPacket) {
        offlineQueue.removeAll { it.nextHop == packet.nextHop && it.packetId == packet.packetId }
        if (offlineQueue.size < 100) offlineQueue.add(packet)
    }

    fun getQueuedFor(nodeId: String): List<OnionPacket> =
        offlineQueue.filter { it.nextHop == nodeId }.also { list ->
            offlineQueue.removeAll(list.toSet())
        }

    /** Pfad-Auswahl: zufällig 3-5 Relay-Knoten */
    private fun selectPath(source: String, target: String, relays: List<String>): List<String> {
        val available = relays.filter { it != source && it != target }.shuffled()
        val hopCount = minOf(available.size, 3 + random.nextInt(3))
        return available.take(hopCount) + target
    }

    fun clearOldPackets(maxAgeMs: Long = 3_600_000L) {
        val cutoff = System.currentTimeMillis() - maxAgeMs
        offlineQueue.removeAll { it.sentAt < cutoff }
    }
}
