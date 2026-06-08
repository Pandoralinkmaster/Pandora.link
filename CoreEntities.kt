package com.pandora.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "ceo_profile")
data class CeoProfile(
    @PrimaryKey val id: String = "ceo",
    val name: String,
    val email: String,
    val pinHash: String = "",
    val biometricEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "devices", indices = [Index("deviceId", unique = true)])
data class Device(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deviceId: String,
    val name: String,
    val model: String = "",
    val platform: String = "android",
    val role: String = "client",
    val isAuthorized: Boolean = false,
    val isBlocked: Boolean = false,
    val customerLevel: Int = 0,
    val tokenHash: String = "",
    val publicKey: String = "",
    val lastSeen: Long = System.currentTimeMillis(),
    val registeredAt: Long = System.currentTimeMillis(),
    val meshEnabled: Boolean = false,
    val sensingEnabled: Boolean = false,
    val computeEnabled: Boolean = false,
    val relayEnabled: Boolean = false,
)

@Entity(tableName = "roles")
data class Role(
    @PrimaryKey val name: String,
    val permissions: List<String> = emptyList(),
    val description: String = "",
)

@Entity(tableName = "consent_records")
data class ConsentRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deviceId: String,
    val consentType: String,
    val granted: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long = -1L,
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String = "",
    val price: Double,
    val currency: String = "EUR",
    val category: String = "Allgemein",
    val imageUri: String = "",
    val visibilityLevel: Int = 0,
    val stock: Int = -1,
    val isActive: Boolean = true,
    val btcPrice: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey val id: String,
    val orderId: String = "",
    val productName: String,
    val amountEur: Double,
    val amountBtc: Double,
    val btcAddress: String,
    val status: String = "pending",
    val txHash: String = "",
    val deviceId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val confirmedAt: Long = 0L,
)

@Entity(tableName = "receipts")
data class Receipt(
    @PrimaryKey val receiptId: String,
    val paymentId: String,
    val productName: String,
    val amount: Double,
    val currency: String = "EUR",
    val btcTxHash: String = "",
    val deviceId: String = "",
    val status: String = "issued",
    val sha256Hash: String = "",
    val timestamp: Long = System.currentTimeMillis(),
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderId: String,
    val receiverId: String = "broadcast",
    val content: String,
    val encryptedContent: String = "",
    val isEncrypted: Boolean = false,
    val messageType: String = "text",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
)

@Entity(tableName = "scan_sessions")
data class ScanSession(
    @PrimaryKey val sessionId: String,
    val deviceId: String,
    val buildingId: String = "",
    val scanType: String = "rssi",
    val dataJson: String = "{}",
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long = 0L,
)

@Entity(tableName = "buildings")
data class Building(
    @PrimaryKey val buildingId: String,
    val name: String,
    val address: String = "",
    val floors: Int = 1,
    val modelJson: String = "{}",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "floors")
data class Floor(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val buildingId: String,
    val floorNumber: Int,
    val name: String = "",
    val layoutJson: String = "{}",
)

@Entity(tableName = "rooms")
data class Room(
    @PrimaryKey val roomId: String,
    val floorId: Int,
    val buildingId: String,
    val name: String,
    val type: String = "room",
    val posX: Double = 0.0,
    val posY: Double = 0.0,
    val width: Double = 0.0,
    val height: Double = 0.0,
)

@Entity(tableName = "zones")
data class Zone(
    @PrimaryKey val zoneId: String,
    val buildingId: String,
    val name: String,
    val type: String = "zone",
    val meshNodeIds: List<String> = emptyList(),
)

@Entity(tableName = "mesh_nodes")
data class MeshNode(
    @PrimaryKey val nodeId: String,
    val deviceId: String,
    val nodeType: String = "relay",
    val isActive: Boolean = false,
    val lastHeartbeat: Long = 0L,
    val rssi: Int = 0,
    val hopCount: Int = 0,
    val buildingId: String = "",
    val roomId: String = "",
    val posX: Double = 0.0,
    val posY: Double = 0.0,
)

@Entity(tableName = "mesh_routes")
data class MeshRoute(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sourceId: String,
    val targetId: String,
    val path: List<String> = emptyList(),
    val hopCount: Int = 0,
    val quality: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "mesh_messages")
data class MeshMessage(
    @PrimaryKey val messageId: String,
    val senderId: String,
    val targetId: String,
    val payload: String,
    val encrypted: Boolean = true,
    val ttl: Int = 7,
    val sentAt: Long = System.currentTimeMillis(),
    val delivered: Boolean = false,
)

@Entity(tableName = "security_events")
data class SecurityEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventType: String,
    val severity: String = "info",
    val description: String,
    val deviceId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val resolved: Boolean = false,
)

@Entity(tableName = "log_entries")
data class LogEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val level: String = "info",
    val tag: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
)

@Entity(tableName = "network_status")
data class NetworkStatus(
    @PrimaryKey val id: String = "current",
    val torActive: Boolean = false,
    val onionAddress: String = "",
    val wireGuardActive: Boolean = false,
    val bluetoothMeshActive: Boolean = false,
    val wifiMeshActive: Boolean = false,
    val meshNodeCount: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "compute_nodes")
data class ComputeNode(
    @PrimaryKey val nodeId: String,
    val deviceId: String,
    val status: String = "pending",
    val cpuCores: Int = 0,
    val ramMb: Int = 0,
    val batteryPercent: Int = 0,
    val temperatureCelsius: Float = 0f,
    val networkQuality: Int = 0,
    val lastUpdate: Long = System.currentTimeMillis(),
)

@Entity(tableName = "compute_tasks")
data class ComputeTask(
    @PrimaryKey val taskId: String,
    val nodeId: String = "",
    val taskType: String,
    val payload: String = "{}",
    val result: String = "",
    val status: String = "pending",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long = 0L,
)

@Entity(tableName = "jayjay_tasks")
data class JayJayTask(
    @PrimaryKey val taskId: String,
    val type: String,
    val input: String,
    val output: String = "",
    val status: String = "pending",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "csi_data")
data class CsiData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: String,
    val deviceId: String,
    val dataType: String = "rssi",
    val rawData: String = "{}",
    val processedData: String = "{}",
    val timestamp: Long = System.currentTimeMillis(),
)

@Entity(tableName = "overlay_data_3d")
data class OverlayData3D(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val buildingId: String,
    val overlayType: String,
    val dataJson: String = "{}",
    val timestamp: Long = System.currentTimeMillis(),
)
