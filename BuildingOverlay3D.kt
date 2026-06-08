package com.pandora.building

import android.util.Log
import com.pandora.database.PandoraDatabase
import com.pandora.database.entity.*
import com.pandora.security.SecurityModule
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class HeatmapPoint(val x: Double, val y: Double, val floor: Int, val value: Float, val type: String)

@Serializable
data class BuildingOverlayData(
    val buildingId: String,
    val signalHeatmap: List<HeatmapPoint> = emptyList(),
    val presenceHeatmap: List<HeatmapPoint> = emptyList(),
    val activeNodes: List<String> = emptyList(),
    val offlineNodes: List<String> = emptyList(),
    val motionEvents: List<HeatmapPoint> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis(),
)

class BuildingOverlay3D(
    private val db: PandoraDatabase,
    private val security: SecurityModule,
) {
    private val _state = MutableStateFlow<BuildingOverlayData?>(null)
    val state: StateFlow<BuildingOverlayData?> = _state

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val json = Json { ignoreUnknownKeys = true }

    fun startAutoRefresh(buildingId: String, intervalMs: Long = 10_000) {
        scope.launch {
            while (isActive) {
                refresh(buildingId)
                delay(intervalMs)
            }
        }
    }

    suspend fun refresh(buildingId: String): BuildingOverlayData = withContext(Dispatchers.IO) {
        val nodes = db.meshDao().activeNodes()
        val rooms = db.buildingDao().rooms(buildingId)
        val allNodes = nodes.map { it.nodeId }

        // Heatmap aus RSSI-Daten berechnen
        val signalHeatmap = nodes.map { node ->
            HeatmapPoint(
                x = node.posX, y = node.posY, floor = 0,
                value = ((node.rssi + 100).toFloat() / 70f).coerceIn(0f, 1f),
                type = "signal"
            )
        }
        val presenceHeatmap = rooms.map { room ->
            HeatmapPoint(
                x = room.posX + room.width / 2, y = room.posY + room.height / 2, floor = 0,
                value = 0f, type = "presence"
            )
        }
        val overlay = BuildingOverlayData(
            buildingId = buildingId,
            signalHeatmap = signalHeatmap,
            presenceHeatmap = presenceHeatmap,
            activeNodes = allNodes,
            updatedAt = System.currentTimeMillis(),
        )
        _state.value = overlay

        // Auf SSD/DB speichern
        db.buildingDao().upsertOverlay(OverlayData3D(
            buildingId = buildingId,
            overlayType = "full",
            dataJson = json.encodeToString(overlay),
        ))
        Log.d("Building3D", "Overlay aktualisiert: ${allNodes.size} Nodes, ${signalHeatmap.size} Heatmap-Punkte")
        overlay
    }

    /** Neues Gebäude anlegen */
    suspend fun createBuilding(name: String, address: String, floors: Int): Building {
        val b = Building(buildingId = security.randomHex(8), name = name, address = address, floors = floors)
        db.buildingDao().upsert(b)
        Log.i("Building3D", "Gebäude erstellt: $name ($floors Etagen)")
        return b
    }

    fun stop() { scope.cancel() }
}
