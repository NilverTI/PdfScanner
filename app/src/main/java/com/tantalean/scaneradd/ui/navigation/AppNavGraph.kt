package com.tantalean.scaneradd.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.tantalean.scaneradd.ads.AdsManager
import com.tantalean.scaneradd.ui.screens.camera.CameraScreen
import com.tantalean.scaneradd.ui.screens.documents.DocumentsScreen
import com.tantalean.scaneradd.ui.screens.home.HomeScreen
import com.tantalean.scaneradd.ui.screens.preview.PreviewScreen
import com.tantalean.scaneradd.vm.MainViewModel

@Composable
fun AppNavGraph(
    vm: MainViewModel,
    adsManager: AdsManager,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                vm = vm,
                adsManager = adsManager,
                onGoCamera = { navController.navigate(Routes.CAMERA) },
                onGoDocs = { navController.navigate(Routes.DOCS) }
            )
        }

        composable(Routes.CAMERA) {
            CameraScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onFinishBatch = { navController.navigate(Routes.PREVIEW) }
            )
        }

        composable(Routes.PREVIEW) {
            PreviewScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onSaved = {
                    // luego de guardar, vamos a docs
                    navController.navigate(Routes.DOCS) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                }
            )
        }

        composable(Routes.DOCS) {
            DocumentsScreen(
                vm = vm,
                onBack = { navController.popBackStack() }
            )
        }
    }
}