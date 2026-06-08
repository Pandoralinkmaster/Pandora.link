package com.pandora.ui.screens

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pandora.core.AppMode
import com.pandora.core.ConfigManager
import com.pandora.core.SetupStatus

@Composable
fun PandoraNavHost(
    config: ConfigManager,
    onRequestSsdPicker: () -> Unit,
) {
    val nav = rememberNavController()
    val startDest = when {
        config.isFirstSetup()    -> "setup"
        !config.isSetupComplete() -> "setup"
        else                     -> "dashboard"
    }

    NavHost(navController = nav, startDestination = startDest) {
        composable("setup") {
            SetupScreen(
                config = config,
                onRequestSsdPicker = onRequestSsdPicker,
                onSetupComplete = { nav.navigate("dashboard") { popUpTo("setup") { inclusive = true } } }
            )
        }
        composable("dashboard") {
            DashboardScreen(
                config = config,
                onNavigateAdmin   = { nav.navigate("admin") },
                onNavigateJayJay  = { nav.navigate("jayjay") },
                onNavigateMesh    = { nav.navigate("mesh") },
                onNavigateWallet  = { nav.navigate("wallet") },
                onNavigateShop    = { nav.navigate("shop") },
            )
        }
        composable("admin")  { AdminScreen(onBack = { nav.popBackStack() }) }
        composable("jayjay") { JayJayScreen(onBack = { nav.popBackStack() }) }
        composable("mesh")   { MeshScreen(onBack = { nav.popBackStack() }) }
        composable("wallet") { WalletScreen(onBack = { nav.popBackStack() }) }
        composable("shop")   { ShopScreen(onBack = { nav.popBackStack() }) }
    }
}
