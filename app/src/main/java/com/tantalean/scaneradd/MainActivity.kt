package com.tantalean.scaneradd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tantalean.scaneradd.ads.AdsManager
import com.tantalean.scaneradd.ui.navigation.AppNavGraph
import com.tantalean.scaneradd.ui.theme.ScanerADDTheme

class MainActivity : ComponentActivity() {

    private lateinit var adsManager: AdsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adsManager = AdsManager(this)
        adsManager.init()

        setContent {
            ScanerADDTheme {
                val vm: com.tantalean.scaneradd.vm.MainViewModel = viewModel()
                AppNavGraph(vm = vm, adsManager = adsManager)
            }
        }
    }
}