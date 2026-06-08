package com.pandora.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.koin.java.KoinJavaComponent.getKoin

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON") return

        val config = getKoin().get<ConfigManager>()
        if (config.isCeoHost() && config.isSetupComplete()) {
            Log.i("BootReceiver", "Boot erkannt – starte Pandora Host Service")
            context.startForegroundService(Intent(context, PandoraHostService::class.java))
        } else {
            Log.i("BootReceiver", "Boot erkannt – kein CEO Host oder Setup unvollständig, übersprungen")
        }
    }
}
