package com.pandora

import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.pandora.core.ConfigManager
import com.pandora.core.PandoraHostService
import com.pandora.core.SetupStatus
import com.pandora.database.SsdStorageManager
import com.pandora.jayjay.VoicePrintManager
import com.pandora.ui.screens.*
import com.pandora.ui.theme.PandoraTheme
import com.pandora.visibility.HostVisibilityGate
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val config: ConfigManager by inject()
    private val ssd: SsdStorageManager by inject()
    private val gate: HostVisibilityGate by inject()
    private val voicePrint: VoicePrintManager by inject()

    // SAF – SSD Ordner-Auswahl
    private val pickSsdFolder = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        uri?.let {
            ssd.setSsdUri(it)
            config.updateSetupStatus(SetupStatus.SSD_SELECTED)
            lifecycleScope.launch {
                ssd.createFolderStructure()
                onSsdReady()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // CEO Host Service starten (wenn Setup abgeschlossen)
        if (config.isCeoHost() && config.isSetupComplete()) {
            startForegroundService(Intent(this, PandoraHostService::class.java))
        }

        // Voice-Print laden
        lifecycleScope.launch { voicePrint.loadPrint() }

        setContent {
            PandoraTheme {
                PandoraNavHost(
                    config = config,
                    onRequestSsdPicker = { pickSsdFolder.launch(null) },
                )
            }
        }
    }

    private suspend fun onSsdReady() {
        config.updateSetupStatus(SetupStatus.DATABASE_CREATED)
        config.updateSetupStatus(SetupStatus.MASTER_CONFIG_CREATED)
        config.updateSetupStatus(SetupStatus.HOST_READY)
        config.updateSetupStatus(SetupStatus.SETUP_COMPLETE)
        if (config.isCeoHost()) {
            startForegroundService(Intent(this, PandoraHostService::class.java))
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Host sperren wenn App den Fokus verliert (Bildschirmsperre)
        if (!hasFocus && gate.isOpen) gate.recordActivity()
    }
}
