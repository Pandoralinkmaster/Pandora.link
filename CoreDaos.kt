package com.pandora.database.dao

import androidx.room.*
import com.pandora.database.entity.*
import kotlinx.coroutines.flow.Flow

@Dao interface CeoDao {
    @Query("SELECT * FROM ceo_profile WHERE id='ceo' LIMIT 1") suspend fun get(): CeoProfile?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(ceo: CeoProfile)
    @Query("DELETE FROM ceo_profile") suspend fun clear()
}

@Dao interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY lastSeen DESC") fun all(): Flow<List<Device>>
    @Query("SELECT * FROM devices WHERE deviceId=:id LIMIT 1") suspend fun getById(id: String): Device?
    @Query("SELECT * FROM devices WHERE isAuthorized=1 AND isBlocked=0") suspend fun authorized(): List<Device>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(device: Device)
    @Query("UPDATE devices SET isAuthorized=:auth WHERE deviceId=:id") suspend fun setAuthorized(id: String, auth: Boolean)
    @Query("UPDATE devices SET isBlocked=:blocked WHERE deviceId=:id") suspend fun setBlocked(id: String, blocked: Boolean)
    @Query("UPDATE devices SET role=:role, customerLevel=:level WHERE deviceId=:id") suspend fun setRole(id: String, role: String, level: Int)
    @Query("UPDATE devices SET lastSeen=:ts WHERE deviceId=:id") suspend fun updateLastSeen(id: String, ts: Long)
}

@Dao interface ProductDao {
    @Query("SELECT * FROM products WHERE isActive=1 ORDER BY name") fun all(): Flow<List<Product>>
    @Query("SELECT * FROM products WHERE id=:id LIMIT 1") suspend fun getById(id: Int): Product?
    @Query("SELECT * FROM products WHERE visibilityLevel<=:level AND isActive=1") suspend fun visibleTo(level: Int): List<Product>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(p: Product): Long
    @Delete suspend fun delete(p: Product)
    @Query("UPDATE products SET isActive=0 WHERE id=:id") suspend fun deactivate(id: Int)
}

@Dao interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY createdAt DESC") fun all(): Flow<List<Payment>>
    @Query("SELECT * FROM payments WHERE id=:id LIMIT 1") suspend fun getById(id: String): Payment?
    @Query("SELECT * FROM payments WHERE status='pending'") suspend fun pending(): List<Payment>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(p: Payment)
    @Query("UPDATE payments SET status=:status, txHash=:tx, confirmedAt=:ts WHERE id=:id") suspend fun confirm(id: String, status: String, tx: String, ts: Long)
}

@Dao interface ReceiptDao {
    @Query("SELECT * FROM receipts ORDER BY timestamp DESC") fun all(): Flow<List<Receipt>>
    @Query("SELECT * FROM receipts WHERE paymentId=:paymentId LIMIT 1") suspend fun getByPayment(paymentId: String): Receipt?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(r: Receipt)
}

@Dao interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT 100") fun recent(): Flow<List<ChatMessage>>
    @Query("SELECT * FROM chat_messages WHERE senderId=:from OR receiverId=:from ORDER BY timestamp") suspend fun thread(from: String): List<ChatMessage>
    @Insert suspend fun insert(m: ChatMessage): Long
    @Query("UPDATE chat_messages SET isRead=1 WHERE receiverId=:id") suspend fun markRead(id: String)
}

@Dao interface MeshDao {
    @Query("SELECT * FROM mesh_nodes") fun allNodes(): Flow<List<MeshNode>>
    @Query("SELECT * FROM mesh_nodes WHERE isActive=1") suspend fun activeNodes(): List<MeshNode>
    @Query("SELECT * FROM mesh_routes WHERE sourceId=:src") suspend fun routesFrom(src: String): List<MeshRoute>
    @Query("SELECT * FROM mesh_messages WHERE delivered=0 AND ttl>0") suspend fun undelivered(): List<MeshMessage>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertNode(n: MeshNode)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertRoute(r: MeshRoute)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertMessage(m: MeshMessage)
    @Query("UPDATE mesh_nodes SET isActive=:active, lastHeartbeat=:ts WHERE nodeId=:id") suspend fun setActive(id: String, active: Boolean, ts: Long)
    @Query("UPDATE mesh_messages SET delivered=1 WHERE messageId=:id") suspend fun markDelivered(id: String)
    @Query("UPDATE mesh_messages SET ttl=ttl-1 WHERE messageId=:id") suspend fun decrementTtl(id: String)
}

@Dao interface ScanDao {
    @Query("SELECT * FROM scan_sessions ORDER BY startedAt DESC") fun all(): Flow<List<ScanSession>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(s: ScanSession)
    @Query("SELECT * FROM csi_data WHERE sessionId=:sid ORDER BY timestamp") suspend fun csiForSession(sid: String): List<CsiData>
    @Insert suspend fun insertCsi(d: CsiData)
}

@Dao interface BuildingDao {
    @Query("SELECT * FROM buildings") fun all(): Flow<List<Building>>
    @Query("SELECT * FROM buildings WHERE buildingId=:id LIMIT 1") suspend fun getById(id: String): Building?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(b: Building)
    @Query("SELECT * FROM floors WHERE buildingId=:id ORDER BY floorNumber") suspend fun floors(id: String): List<Floor>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertFloor(f: Floor)
    @Query("SELECT * FROM rooms WHERE buildingId=:id") suspend fun rooms(id: String): List<Room>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertRoom(r: Room)
    @Query("SELECT * FROM overlay_data_3d WHERE buildingId=:id ORDER BY timestamp DESC LIMIT 1") suspend fun latestOverlay(id: String): OverlayData3D?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertOverlay(o: OverlayData3D)
}

@Dao interface SecurityDao {
    @Query("SELECT * FROM security_events ORDER BY timestamp DESC LIMIT 50") fun recent(): Flow<List<SecurityEvent>>
    @Insert suspend fun insert(e: SecurityEvent)
    @Query("UPDATE security_events SET resolved=1 WHERE id=:id") suspend fun resolve(id: Int)
}

@Dao interface ComputeDao {
    @Query("SELECT * FROM compute_nodes") fun all(): Flow<List<ComputeNode>>
    @Query("SELECT * FROM compute_nodes WHERE status='active'") suspend fun active(): List<ComputeNode>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertNode(n: ComputeNode)
    @Query("UPDATE compute_nodes SET status=:status WHERE nodeId=:id") suspend fun setStatus(id: String, status: String)
    @Query("SELECT * FROM compute_tasks WHERE status='pending' ORDER BY createdAt LIMIT 10") suspend fun pendingTasks(): List<ComputeTask>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertTask(t: ComputeTask)
    @Query("UPDATE compute_tasks SET status=:status, result=:result, completedAt=:ts WHERE taskId=:id") suspend fun completeTask(id: String, status: String, result: String, ts: Long)
}

@Dao interface JayJayDao {
    @Query("SELECT * FROM jayjay_tasks ORDER BY createdAt DESC LIMIT 20") fun recent(): Flow<List<JayJayTask>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(t: JayJayTask)
    @Query("UPDATE jayjay_tasks SET output=:output, status=:status WHERE taskId=:id") suspend fun complete(id: String, output: String, status: String)
}

@Dao interface NetworkDao {
    @Query("SELECT * FROM network_status WHERE id='current' LIMIT 1") suspend fun get(): NetworkStatus?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(n: NetworkStatus)
}
