package com.safefleet.ai.mobile.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.safefleet.ai.mobile.BuildConfig

private const val HOME_ROUTE = "home"
private const val DIAGNOSTICS_ROUTE = "diagnostics"

@Composable
fun SafeFleetRoot(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = HOME_ROUTE) {
        composable(HOME_ROUTE) {
            DriverHomeScreen(onOpenDiagnostics = { navController.navigate(DIAGNOSTICS_ROUTE) })
        }
        composable(DIAGNOSTICS_ROUTE) {
            DiagnosticsScreen(onBack = { navController.popBackStack() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverHomeScreen(
    onOpenDiagnostics: () -> Unit,
    viewModel: DriverHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("SafeFleet AI Driver") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "M1 Device & Trip",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "Device credentials are kept in Android Keystore-backed storage. Driver and vehicle identity come from the authenticated backend context.",
                style = MaterialTheme.typography.bodyLarge,
            )

            StatusCard(
                "Backend",
                when (state.backendReachable) {
                    true -> "REACHABLE"
                    false -> "UNREACHABLE"
                    null -> "CHECKING"
                },
            )
            StatusCard("Network", if (state.health?.networkConnected == true) "CONNECTED" else "OFFLINE")
            StatusCard("Camera permission", if (state.health?.cameraPermissionGranted == true) "GRANTED" else "REQUIRED")
            StatusCard("Location permission", if (state.health?.locationPermissionGranted == true) "GRANTED" else "REQUIRED")
            StatusCard("Battery", state.health?.batteryPercent?.let { "$it%" } ?: "UNKNOWN")

            if (state.health?.cameraPermissionGranted != true || state.health?.locationPermissionGranted != true) {
                OutlinedButton(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Grant camera & location permissions")
                }
            }

            if (!state.paired) {
                Text("Installation ID", style = MaterialTheme.typography.titleMedium)
                SelectionContainer {
                    Text(state.installationId.ifBlank { "Loading installation identity..." })
                }
                Text(
                    "Register this installation ID as the backend deviceUid, rotate its device credential, then paste the returned JSON below.",
                )
                OutlinedTextField(
                    value = state.pairingPayload,
                    onValueChange = viewModel::updatePairingPayload,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Pairing credential JSON") },
                    placeholder = { Text("{\"deviceId\":\"...\",\"deviceKey\":\"...\"}") },
                    minLines = 4,
                    maxLines = 8,
                )
                Button(
                    onClick = viewModel::pair,
                    enabled = !state.actionInProgress && state.pairingPayload.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (state.actionInProgress) "Pairing..." else "Pair device")
                }
            } else {
                val context = state.context
                StatusCard("Pairing", "PAIRED")
                StatusCard("Device", context?.device?.deviceUid ?: "CONTEXT UNAVAILABLE")
                StatusCard(
                    "Driver",
                    context?.driver?.let { "${it.fullName} (${it.employeeCode})" } ?: "NOT BOUND",
                )
                StatusCard(
                    "Vehicle",
                    context?.vehicle?.let {
                        listOfNotNull(it.plateNumber, it.make, it.model).joinToString(" · ")
                    } ?: "NOT BOUND",
                )
                StatusCard("Fleet", context?.fleet?.name ?: "NOT ASSIGNED")
                StatusCard("Binding source", context?.bindingSource ?: "UNKNOWN")
                StatusCard(
                    "Trip",
                    context?.activeTrip?.let { "${it.status} · ${it.id}" }
                        ?: if (context?.canStartTrip == true) "READY TO START" else "NOT READY",
                )

                if (!context?.requiredActions.isNullOrEmpty()) {
                    Text(
                        text = "Required setup: ${context?.requiredActions?.joinToString()}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                if (context?.activeTrip == null) {
                    Button(
                        onClick = viewModel::startTrip,
                        enabled = context?.canStartTrip == true && !state.actionInProgress,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (state.actionInProgress) "Working..." else "Start trip")
                    }
                } else {
                    Button(
                        onClick = { viewModel.completeTrip(context.activeTrip.id) },
                        enabled = !state.actionInProgress,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (state.actionInProgress) "Working..." else "Complete trip")
                    }
                }

                OutlinedButton(
                    onClick = viewModel::unpair,
                    enabled = !state.actionInProgress,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Remove device pairing")
                }
            }

            state.message?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = viewModel::refresh,
                enabled = !state.actionInProgress,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (state.loading) "Refreshing..." else "Refresh device context")
            }
            OutlinedButton(onClick = onOpenDiagnostics, modifier = Modifier.fillMaxWidth()) {
                Text("Diagnostics")
            }
        }
    }
}

@Composable
private fun StatusCard(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(PaddingValues(16.dp)),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiagnosticsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("M1 diagnostics") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("API base URL", style = MaterialTheme.typography.labelLarge)
            SelectionContainer { Text(BuildConfig.API_BASE_URL) }
            Text("App version: ${BuildConfig.VERSION_NAME}")
            Text("Credential storage: Android Keystore AES/GCM")
            Text("Device auth: X-SafeFleet-Device-Id + X-SafeFleet-Device-Key")
            Text("Trip retry IDs: persistent DataStore state")
            Text("Room outbox: reserved for M4 telemetry/event sync")
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Back")
            }
        }
    }
}
