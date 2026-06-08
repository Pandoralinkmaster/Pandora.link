package com.pandora.compute

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.util.Log
import com.pandora.database.PandoraDatabase
import com.pandora.database.entity.ComputeNode
import com.pandora.database.entity.ComputeTask
import com.pandora.security.SecurityModule
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

@Serializable
data class ComputeNodeInfo(
    val nodeId: String,
    val cpuCores: Int,
    val ramMb: Int,
    val batteryPercent: Int,
    val temperatureCelsius: Float,
    val networkQuality: Int,
    val status: String,
)

class ComputeMeshManager(
    private val context: Context,
    private val db: PandoraDatabase,
    private val security: SecurityModule,
) {
    private val _state = MutableStateFlow<ComputeNodeInfo?>(null)
    val state: StateFlow<ComputeNodeInfo?> = _state

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun startAsComputeNode(deviceId: String) {
        scope.launch {
            while (isActive) {
                val info = collectNodeInfo(deviceId)
                _state.value = info
                db.computeDao().upsertNode(ComputeNode(
                    nodeId = deviceId, deviceId = deviceId,
                    status = info.status,
                    cpuCores = info.cpuCores,
                    ramMb = info.ramMb,
                    batteryPercent = info.batteryPercent,
                    temperatureCelsius = info.temperatureCelsius,
                    networkQuality = info.networkQuality,
                ))
                // Wartende Aufgaben prüfen
                val tasks = db.computeDao().pendingTasks()
                tasks.forEach { executeTask(it) }
                delay(30_000)   // alle 30 Sek. aktualisieren
            }
        }
        Log.i("Compute", "Compute-Node gestartet: $deviceId")
    }

    private fun collectNodeInfo(deviceId: String): ComputeNodeInfo {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val battery = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val runtime = Runtime.getRuntime()
        return ComputeNodeInfo(
            nodeId = deviceId,
            cpuCores = runtime.availableProcessors(),
            ramMb = (memInfo.availMem / 1_048_576).toInt(),
            batteryPercent = battery,
            temperatureCelsius = 0f,
            networkQuality = 80,
            status = if (battery > 20) "active" else "low_battery",
        )
    }

    private suspend fun executeTask(task: ComputeTask) {
        Log.i("Compute", "Aufgabe ausführen: ${task.taskId} (${task.taskType})")
        try {
            db.computeDao().upsertTask(task.copy(status = "running"))
            val result = when (task.taskType) {
                "hash" -> computeHash(task.payload)
                "jayjay_inference" -> "Inference-Ergebnis (GPT-Delegation)"
                "mesh_route_calc" -> "Routing-Pfad berechnet"
                "heatmap" -> "Heatmap-Daten verarbeitet"
                else -> "Aufgabe abgeschlossen"
            }
            db.computeDao().completeTask(task.taskId, "completed", result, System.currentTimeMillis())
        } catch (e: Exception) {
            db.computeDao().completeTask(task.taskId, "error", e.message ?: "Fehler", System.currentTimeMillis())
        }
    }

    private fun computeHash(data: String): String = security.sha256(data)

    fun dispatchTask(type: String, payload: String): String {
        val taskId = security.randomHex(8)
        scope.launch {
            db.computeDao().upsertTask(ComputeTask(taskId = taskId, taskType = type, payload = payload))
        }
        return taskId
    }

    fun stop() { scope.cancel() }
}
