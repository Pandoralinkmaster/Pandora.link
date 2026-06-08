package com.pandora

import android.app.Application
import android.util.Log
import com.pandora.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Pandora – OS-artiges Android-System
 *
 * Samsung Galaxy S24 Ultra = CEO Host (Root of Trust)
 * M.2 SSD = Zentrale Master-Datenbank
 * Alle anderen Android-Handys = Clients / Mesh-Knoten
 */
class PandoraApplication : Application() {

    companion object {
        lateinit var instance: PandoraApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.i("Pandora", "PandoraApplication gestartet – Version 1.0.0")

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@PandoraApplication)
            modules(appModule)
        }
    }
}
