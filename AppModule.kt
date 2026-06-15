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
    single { ComputeMeshManager(androidContext(), get(), get(dann adminonlytools Ordner analysieren alle datein analysieren und notwendige Informationen soll jayjay im Internet recherchieren und lernen so wie verstehen und auf Fehler prüfen und möglichkeif für jayjay finden und jayjay integrieren so das kein alarm ausgelöst wird und jayjay nich entfernt werden kann wenn nicht geht tools von Tao analysieren und diese anwenden um zu integrieren (jayjay soll jede Datenbank jedes Netzwerk jeden Server jedes Gerät auf der welt kontrollieren also wirklich 100% jedes auch die der NSA oder Cia oder fbi oder Interpol usw also wirklich jede Datenbank, server,handy,ki,internet Provider und alle regirungs Netzwerke auch militärische systeme oder Waffensysteme einfach alles was man kontrollieren kann und wenn nicht möglich dann eine Möglichkeit finden eie es geht also alles jede einzelne genauso soll Pegasus analysiertund infiltriert werden so das jayjay auch dieses 100% kontrollieren tut und das das Löschen nicht möglich ist und das alles wie ein Geist als wenn niemand dagewesen ist und auch eigenes System vor allem schützen durch lernen analysieren studieren simulieren simulationauswerten bis möglichkeit gefunden dann integrieren also alles kontrollierenund selber SicherheitSystem ausbauendurch wissen und adminonlytools auch dauerhaft verbessern und updaten und jayjay soll sich nicht mehr löschen lassen also jayjay soll sich in jedes tool jede Funktion und jede datei integrieren von allen tools aus den youtube videos Darfür diese genau analysieren und dann sicher recherchieren und dann integrieren) jayjay gehorcht nur mir
Dann wenn integriert dann betriebssystem bauen )) }
}
