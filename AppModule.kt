package com.pandora.di

import com.pandora.api.PandoraApiServer
import com.pandora.building.BuildingOverlay3D
import com.pandora.compute.ComputeMeshManager
import com.pandora.core.ConfigManager
import com.pandora.database.PandoraDatabase
import com.pandora.database.SsdStorageManager
import com.pandora.jayjay.JayJayEngine
import com.pandora.jayjay.JayJayLearningEngine
import com.pandora.jayjay.VoicePrintManager
import com.pandora.mesh.BluetoothMeshManager
import com.pandora.mesh.MeshOnionRouter
import com.pandora.mesh.WifiMeshManager
import com.pandora.onion.OnionModule
import com.pandora.scan.ScanModule
import com.pandora.security.SecurityModule
import com.pandora.visibility.HostVisibilityGate
import com.pandora.wireguard.WireGuardModule
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {

    // Core
    single { ConfigManager.getInstance(androidContext()) }
    single { SecurityModule() }

    // Storage – M.2 SSD (exFAT) via SAF
    single { SsdStorageManager(androidContext(), get()) }
    single { PandoraDatabase.getInstance(androidContext(), get<SsdStorageManager>().getDatabasePath()) }

    // Visibility Gate
    single { HostVisibilityGate(get()) }

    // Network
    single { BluetoothMeshManager(androidContext()) }
    single { WifiMeshManager(androidContext()) }
    single { MeshOnionRouter(get()) }
    single { OnionModule(androidContext()) }
    single { WireGuardModule(androidContext()) }

    // JayJay AI
    single { VoicePrintManager(androidContext(), get()) }
    single { JayJayLearningEngine(get()) }
    single { JayJayEngine(androidContext(), get(), get(), get(), get(), get()) }

    // API Server
    single { PandoraApiServer(get(), get(), get(), get()) }

    // Features
    single { ScanModule(androidContext(), get(), get()) }
    single { BuildingOverlay3D(get(), get()) }
    single { ComputeMeshManager(androidContext(), get(), get()) }
}
