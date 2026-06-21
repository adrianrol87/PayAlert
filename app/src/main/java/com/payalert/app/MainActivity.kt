package com.payalert.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.payalert.app.ads.InterstitialAdController
import com.payalert.app.ui.PayAlertApp
import com.payalert.app.ui.theme.PayAlertTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        InterstitialAdController.initialize(applicationContext)
        enableEdgeToEdge()
        setContent {
            PayAlertTheme {
                PayAlertApp()
            }
        }
    }
}
