package com.safefleet.ai.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import com.safefleet.ai.mobile.ui.SafeFleetRoot
import com.safefleet.ai.mobile.ui.theme.SafeFleetTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SafeFleetTheme {
                SafeFleetRoot()
            }
        }
    }
}
