package com.pandora.api

import android.util.Log
import com.pandora.core.ConfigManager
import com.pandora.database.PandoraDatabase
import com.pandora.database.entity.*
import com.pandora.security.SecurityModule
import com.pandora.visibility.HostVisibilityGate
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

/**
 * Pandora Embedded Ktor API Server
 *
 * Läuft auf dem CEO-Host (Samsung Galaxy S24 Ultra) als eingebetteter HTTP-Server.
 * Alle anderen Pandora-Geräte verbinden sich zu diesem Server.
 *
 * Port: 8765 (konfigurierbar)
 *
 * Sicherheit:
 * - Alle Endpunkte prüfen das HostVisibilityGate
 * - Bearer-Token-Pflicht für alle nicht-öffentlichen Endpunkte
 * - Wenn CEO Host nicht aktiv → 503 für alle Endpunkte
 */
@Serializable data class ApiResponse<T>(val success: Boolean, val data: T? = null, val error: String? = null)
@Serializable data class RegisterRequest(val deviceId: String, val deviceName: String, val model: String = "")
@Serializable data class LoginRequest(val deviceId: String, val tokenOrPin: String)

class PandoraApiServer(
    private val config: ConfigManager,
    private val db: PandoraDatabase,
    private val security: SecurityModule,
    private val gate: HostVisibilityGate,
) {
    private var server: EmbeddedServer<*, *>? = null
    private val activeSockets = ConcurrentHashMap<String, DefaultWebSocketSession>()
    private val socketMutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    fun start(port: Int = config.config.apiPort) {
        if (server != null) return
        server = embeddedServer(Netty, port = port) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            install(WebSockets)
            install(CORS) {
                anyHost()
                allowMethod(HttpMethod.Get); allowMethod(HttpMethod.Post)
                allowMethod(HttpMethod.Put); allowMethod(HttpMethod.Delete)
                allowHeader(HttpHeaders.Authorization)
                allowHeader(HttpHeaders.ContentType)
            }
            routing { setupRoutes() }
        }.also { it.start(wait = false) }
        Log.i("ApiServer", "Pandora API Server gestartet auf Port $port")
    }

    fun stop() { server?.stop(500, 2000); server = null; Log.i("ApiServer", "Server gestoppt") }

    // ── Middleware ─────────────────────────────────────────────────────────────

    private fun ApplicationCall.checkGate(): Boolean {
        if (!gate.isOpen) { runBlocking { respond(HttpStatusCode.ServiceUnavailable, ApiResponse<Unit>(false, error = "host_offline")) }; return false }
        return true
    }

    private fun ApplicationCall.extractDeviceId(): String? {
        val auth = request.header(HttpHeaders.Authorization) ?: return null
        val token = auth.removePrefix("Bearer ").trim()
        return security.extractDeviceId(token)
    }

    private suspend fun ApplicationCall.requireAuth(): String? {
        val auth = request.header(HttpHeaders.Authorization)
        if (auth.isNullOrBlank()) { respond(HttpStatusCode.Unauthorized, ApiResponse<Unit>(false, error = "token_required")); return null }
        val token = auth.removePrefix("Bearer ").trim()
        if (!security.validateToken(token)) { respond(HttpStatusCode.Unauthorized, ApiResponse<Unit>(false, error = "invalid_token")); return null }
        return security.extractDeviceId(token)
    }

    // ── Routes ─────────────────────────────────────────────────────────────────

    private fun Routing.setupRoutes() {

        // ── Öffentlich ────────────────────────────────────────────────────────

        get("/pandora/status") {
            call.respond(ApiResponse(true, gate.clientStatus()))
        }

        post("/pandora/register") {
            if (!call.checkGate()) return@post
            val req = call.receive<RegisterRequest>()
            val existing = db.deviceDao().getById(req.deviceId)
            if (existing != null && existing.isAuthorized) {
                call.respond(ApiResponse(false, error = "already_registered"))
                return@post
            }
            val device = Device(
                deviceId = req.deviceId, name = req.deviceName, model = req.model,
                isAuthorized = false,
            )
            db.deviceDao().upsert(device)
            call.respond(ApiResponse(true, mapOf("message" to "Registrierung eingegangen – CEO-Freigabe ausstehend")))
        }

        post("/pandora/auth") {
            if (!call.checkGate()) return@post
            val req = call.receive<LoginRequest>()
            val device = db.deviceDao().getById(req.deviceId)
            if (device == null || !device.isAuthorized) {
                call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(false, error = "not_authorized"))
                return@post
            }
            val token = security.generateToken(req.deviceId, device.role)
            call.respond(ApiResponse(true, mapOf("token" to token, "role" to device.role)))
        }

        // ── Authentifiziert ───────────────────────────────────────────────────

        get("/pandora/products") {
            if (!call.checkGate()) return@get
            val deviceId = call.requireAuth() ?: return@get
            val device = db.deviceDao().getById(deviceId)
            val level = device?.customerLevel ?: 0
            val products = db.productDao().visibleTo(level)
            call.respond(ApiResponse(true, products))
        }

        post("/pandora/orders") {
            if (!call.checkGate()) return@post
            val deviceId = call.requireAuth() ?: return@post
            @Serializable data class OrderRequest(val productId: Int, val quantity: Int = 1)
            val req = call.receive<OrderRequest>()
            val product = db.productDao().getById(req.productId)
                ?: run { call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(false, error = "product_not_found")); return@post }
            val paymentId = security.randomHex(16)
            val payment = Payment(
                id = paymentId, productName = product.name,
                amountEur = product.price * req.quantity,
                amountBtc = product.btcPrice * req.quantity,
                btcAddress = "", deviceId = deviceId,
            )
            db.paymentDao().upsert(payment)
            call.respond(ApiResponse(true, mapOf("paymentId" to paymentId, "amount" to payment.amountEur)))
        }

        get("/pandora/orders") {
            if (!call.checkGate()) return@get
            val deviceId = call.requireAuth() ?: return@get
            call.respond(ApiResponse(true, emptyList<Payment>()))
        }

        // ── Mesh ──────────────────────────────────────────────────────────────

        post("/pandora/mesh/heartbeat") {
            if (!call.checkGate()) return@post
            val deviceId = call.requireAuth() ?: return@post
            @Serializable data class HeartbeatRequest(val nodeId: String, val rssi: Int = 0, val hopCount: Int = 0)
            val req = call.receive<HeartbeatRequest>()
            db.meshDao().setActive(req.nodeId, true, System.currentTimeMillis())
            db.deviceDao().updateLastSeen(deviceId, System.currentTimeMillis())
            call.respond(ApiResponse(true, mapOf("ack" to true)))
        }

        // ── Chat ──────────────────────────────────────────────────────────────

        post("/pandora/chat") {
            if (!call.checkGate()) return@post
            val deviceId = call.requireAuth() ?: return@post
            @Serializable data class ChatRequest(val content: String, val receiverId: String = "broadcast")
            val req = call.receive<ChatRequest>()
            db.chatDao().insert(ChatMessage(senderId = deviceId, receiverId = req.receiverId, content = req.content))
            broadcastToSockets("chat", """{"from":"$deviceId","msg":"${req.content}"}""")
            call.respond(ApiResponse(true, mapOf("sent" to true)))
        }

        // ── WebSocket ─────────────────────────────────────────────────────────

        webSocket("/pandora/ws") {
            val token = call.request.header("X-Token") ?: call.request.queryParameters["token"]
            if (token == null || !security.validateToken(token)) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "unauthorized"))
                return@webSocket
            }
            val deviceId = security.extractDeviceId(token) ?: "unknown"
            socketMutex.withLock { activeSockets[deviceId] = this }
            Log.i("ApiServer", "WebSocket verbunden: $deviceId (aktiv: ${activeSockets.size})")
            try {
                send(Frame.Text("""{"type":"connected","deviceId":"$deviceId"}"""))
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val msg = frame.readText()
                        broadcastToSockets("relay", msg, except = deviceId)
                    }
                }
            } finally {
                socketMutex.withLock { activeSockets.remove(deviceId) }
                Log.i("ApiServer", "WebSocket getrennt: $deviceId")
            }
        }

        // ── Compute Mesh ──────────────────────────────────────────────────────

        post("/pandora/compute/register") {
            if (!call.checkGate()) return@post
            val deviceId = call.requireAuth() ?: return@post
            @Serializable data class ComputeRequest(val cpuCores: Int, val ramMb: Int, val batteryPercent: Int)
            val req = call.receive<ComputeRequest>()
            db.computeDao().upsertNode(ComputeNode(
                nodeId = deviceId, deviceId = deviceId, status = "active",
                cpuCores = req.cpuCores, ramMb = req.ramMb, batteryPercent = req.batteryPercent,
            ))
            call.respond(ApiResponse(true, mapOf("nodeId" to deviceId, "status" to "active")))
        }

        // ── CEO Admin ─────────────────────────────────────────────────────────

        get("/pandora/admin/devices") {
            if (!call.checkGate()) return@get
            call.requireAuth() ?: return@get
            call.respond(ApiResponse(true, db.deviceDao().authorized()))
        }

        post("/pandora/admin/devices/{id}/authorize") {
            if (!call.checkGate()) return@post
            call.requireAuth() ?: return@post
            val id = call.parameters["id"] ?: return@post
            db.deviceDao().setAuthorized(id, true)
            call.respond(ApiResponse(true, mapOf("authorized" to true)))
        }
    }

    private suspend fun broadcastToSockets(type: String, payload: String, except: String? = null) {
        val msg = """{"type":"$type","payload":$payload}"""
        socketMutex.withLock {
            activeSockets.entries.filter { it.key != except }.forEach { (id, ws) ->
                try { ws.send(Frame.Text(msg)) } catch (_: Exception) { activeSockets.remove(id) }
            }
        }
    }

    fun getActiveConnections() = activeSockets.keys.toList()
    fun isRunning() = server != null
}
