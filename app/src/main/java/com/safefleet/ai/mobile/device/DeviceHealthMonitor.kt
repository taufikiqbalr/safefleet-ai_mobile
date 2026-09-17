package com.safefleet.ai.mobile.device

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class DeviceHealthSnapshot(
    val cameraPermissionGranted: Boolean,
    val locationPermissionGranted: Boolean,
    val networkConnected: Boolean,
    val batteryPercent: Int?,
)

@Singleton
class DeviceHealthMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun snapshot(): DeviceHealthSnapshot {
        val cameraGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED
        val locationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = activeNetwork?.let(connectivityManager::getNetworkCapabilities)
        val networkConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        val batteryManager = context.getSystemService(BatteryManager::class.java)
        val rawBattery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val batteryPercent = rawBattery.takeIf { it in 0..100 }

        return DeviceHealthSnapshot(
            cameraPermissionGranted = cameraGranted,
            locationPermissionGranted = locationGranted,
            networkConnected = networkConnected,
            batteryPercent = batteryPercent,
        )
    }
}
