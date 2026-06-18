package com.pandora.visibility

import android.util.Log
import com.pandora.core.ConfigManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

/**
 * PandoraHostVisibilityGate
 *
 * Regeln:
 * - Host ist standardmäßig GESPERRT
 * - CEO muss Host aktiv freischalten (PIN + Biometrie)
 * - Wenn CEO-Handy gesperrt wird → Zugriff sofort gesperrt
 * - Clients sehen nur "host_offline" oder "access_locked"
 * - Inaktivitäts-Timeout: 15 Minuten
 * - Admin-Daten NUR bei aktivem CEO Host
 */
@Serializable(
data class VisibilityState(
    val isOpen: Boolean = false,weitermachen wenn alle 664699 funktionen 100% fertig integriert sind dann erneut alle original links und original datein erneut alle original zips und jede einzelne zeile code analysieren und überprüfen ob alle datein und jede einzelne zeile code richtig interpretiert wurde und integriert wurde 100% vollständig und Fehler beheben und fehlende datein und code erstellen und alle fehlenden entwickeln und solange mit allen original links und datein von jeder Funktion die in jeder einzelnen zeile code stehen tut und jede einzelne zeile code von allem abgleichen und alle fehlenden Funktionen die in jeder einzelnen Zeile code gefordert ist aber fehlen tuhen egal wo ab in einem link in einer zip in einer Datei oder in irgendeiner Zeile code gewünscht ist alles was dafür nötig ist entwickeln jede zeile code erstellen und integrieren und alles andere was dafür nötig ist machen egal wie das ist mur egal aber 100% vollständig nix soll fehlen oder ausgelassen oder ignoriert oder vergessen werden 100% alles dafür erstellen und auf Fehler überprüfen und 100% funktionsfähig erstellen und installieren egal wie du es machst oder Wieland es brauch oder wie groß es wird nicht mal das aller kleinste detail vergessen nicht eine einzige Funktion egal wie klein oder groß sie ist einfach alles integrieren also auch die tools erstellen also jede einzelne zeile code die es noch nicht giebt aber erstmal weitermachen wie zuvor bis 100%fertig 

    val reason: String = "host_offline",        // host_offline | access_locked | inactivity_timeout | active
    val openedAt: Long = 0L,
    val lockedAt: Long = 0L,
    val timeoutMinutes: Int = 15,
    val unlockMethod: String = "",              // pin | biometric | both
)

class HostVisibilityGate(private val config: ConfigManager) {

    private val _state = MutableStateFlow(VisibilityState())
    val state: StateFlow<VisibilityState> = _state

    private var lastActivityAt = System.currentTimeMillis()
    private val TIMEOUT_MS = 15 * 60 * 1000L   // 15 Minuten

    /** Ist der Host für Clients zugänglich? */
    val isOpen: Boolean get() {
        checkTimeout()
        return _state.value.isOpen && config.isHostActive
    }

    /** Host freischalten (nach PIN/Biometrie-Prüfung) */
    fun unlock(method: String = "pin") {
        _state.value = VisibilityState(
            isOpen = true,
            reason = "active",
            openedAt = System.currentTimeMillis(),
            unlockMethod = method,
        )
        config.isHostActive = true
        lastActivityAt = System.currentTimeMillis()
        Log.i("Visibility", "Host freigeschaltet via $method")
    }

    /** Host sofort sperren (z.B. wenn Bildschirm gesperrt) */
    fun lock(reason: String = "access_locked") {
        _state.value = _state.value.copy(
            isOpen = false, reason = reason, lockedAt = System.currentTimeMillis()
        )
        config.isHostActive = false
        Log.i("Visibility", "Host gesperrt: $reason")
    }

    /** PIN-Validierung */
    fun validatePin(pin: String): Boolean {
        val storedPin = config.hostPin
        if (storedPin.isNullOrBlank()) {
            // Erster Start: PIN setzen
            config.hostPin = pin
            unlock("pin")
            return true
        }
        return if (storedPin == pin) { unlock("pin"); true } else false
    }

    /** Aktivitäts-Heartbeat (bei jeder CEO-Interaktion aufrufen) */
    fun recordActivity() {
        lastActivityAt = System.currentTimeMillis()
        if (!_state.value.isOpen) return
        _state.value = _state.value.copy(openedAt = lastActivityAt)
    }

    private fun checkTimeout() {
        if (!_state.value.isOpen) return
        if (System.currentTimeMillis() - lastActivityAt > TIMEOUT_MS) {
            lock("inactivity_timeout")
        }
    }

    /** Status für Client-Antwort (kein Datenleck!) */
    fun clientStatus(): Map<String, String> = if (isOpen) {
        mapOf("status" to "host_online", "version" to "1.0.0")
    } else {
        mapOf("status" to _state.value.reason)
    }

    /** Bildschirm-Sperre-Ereignis empfangen */
    fun onScreenLocked() { lock("access_locked") }
    fun onScreenUnlocked() { /* CEO muss PIN/Biometrie eingeben */ }
}
