package com.pandora.jayjay

import android.app.*
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.pandora.MainActivity
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

/**
 * JayJay Always-On Voice Service
 *
 * Läuft als Foreground Service – auch bei gesperrtem Bildschirm.
 * Auf JEDEM Pandora-Gerät (CEO-Handy, Kollegen-Handy, egal welches).
 *
 * Erkennt CEO-Stimme lokal → kein Netzwerk nötig für Erkennung.
 * Nur bei CEO-Befehl: Netzwerkzugriff für GPT-Anfrage.
 *
 * VAD (Voice Activity Detection):
 * - Mikrofon-Level dauerhaft überwacht (kein Full-Buffer-Recording)
 * - Nur bei Stimme oberhalb Schwellwert → MFCC-Analyse
 * - CPU-schonend: 99% der Zeit wird nur Lautstärke gemessen
 */
class JayJayVoiceService : Service() {

    private val engine: JayJayEngine by inject()
    private val voicePrint: VoicePrintManager by inject()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var recorder: AudioRecord? = null
    private var isRunning = false

    companion object {
        const val CHANNEL_ID    = "jayjay_voice"
        const val NOTIF_ID      = 42
        const val SAMPLE_RATE   = 16000   // 16kHz (Whisper-Standard)
        const val BUFFER_FRAMES = 4096
        const val VAD_THRESHOLD = 800     // RMS-Schwellwert für Spracherkennung
        const val CAPTURE_MS    = 3000    // 3 Sekunden Aufnahme nach VAD-Trigger
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.i("JayJay", "Voice Service gestartet – Gerät überwacht")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, buildNotification())
        startListening()
        return START_STICKY   // Automatisch neu starten wenn System tötet
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        recorder?.release()
        scope.cancel()
        Log.i("JayJay", "Voice Service beendet")
    }

    // ── Audio Loop ─────────────────────────────────────────────────────────────

    private fun startListening() {
        if (isRunning) return
        isRunning = true

        scope.launch {
            val bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(BUFFER_FRAMES * 2)

            recorder = try {
                AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize
                ).also { it.startRecording() }
            } catch (e: SecurityException) {
                Log.e("JayJay", "Mikrofon-Berechtigung fehlt: ${e.message}"); return@launch
            }

            Log.i("JayJay", "Mikrofon aktiv – warte auf CEO-Stimme...")
            val vadBuffer = ShortArray(BUFFER_FRAMES)

            while (isRunning) {
                val read = recorder?.read(vadBuffer, 0, BUFFER_FRAMES) ?: break
                if (read <= 0) continue

                // VAD: Sprachaktivität prüfen (günstig)
                val rms = calculateRms(vadBuffer, read)
                if (rms > VAD_THRESHOLD) {
                    Log.d("JayJay", "Sprache erkannt (RMS=$rms) – nehme auf...")
                    val fullCapture = captureFullAudio()
                    val deviceId = android.provider.Settings.Secure.getString(
                        contentResolver, android.provider.Settings.Secure.ANDROID_ID
                    ) ?: "unknown"
                    engine.processAudio(fullCapture, deviceId)
                }

                delay(50)   // 50ms VAD-Polling
            }
        }
    }

    /** 3 Sekunden vollständig aufnehmen nach VAD-Trigger */
    private fun captureFullAudio(): ShortArray {
        val totalSamples = SAMPLE_RATE * CAPTURE_MS / 1000
        val buffer = ShortArray(totalSamples)
        var offset = 0
        val chunk = ShortArray(BUFFER_FRAMES)
        while (offset < totalSamples) {
            val read = recorder?.read(chunk, 0, minOf(BUFFER_FRAMES, totalSamples - offset)) ?: break
            if (read > 0) { chunk.copyInto(buffer, offset, 0, read); offset += read }
        }
        return buffer
    }

    private fun calculateRms(buffer: ShortArray, length: Int): Int {
        var sum = 0L
        for (i in 0 until length) sum += buffer[i].toLong() * buffer[i]
        return Math.sqrt((sum / length).toDouble()).toInt()
    }

    // ── Notification ───────────────────────────────────────────────────────────

    private fun buildNotification(): Notification {
        val intent = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("JayJay hört zu")
            .setContentText("Sage \"Pandemonium\" um JayJay zu aktivieren")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .setContentIntent(intent)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "JayJay Voice",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "JayJay Voice-Erkennung läuft im Hintergrund" }
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }
}
