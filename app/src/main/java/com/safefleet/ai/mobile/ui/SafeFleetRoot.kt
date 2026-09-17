package com.safefleet.ai.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
            FoundationHomeScreen(onOpenDiagnostics = { navController.navigate(DIAGNOSTICS_ROUTE) })
        }
        composable(DIAGNOSTICS_ROUTE) {
            DiagnosticsScreen(onBack = { navController.popBackStack() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoundationHomeScreen(
    onOpenDiagnostics: () -> Unit,
    viewModel: FoundationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { TopAppBar(title = { Text("SafeFleet AI Driver") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "M0 Android foundation",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "Edge-first safety application shell. Camera inference and local alarms are added in later mobile phases.",
                style = MaterialTheme.typography.bodyLarge,
            )
            StatusCard("Android application", "READY")
            StatusCard("Local persistence", "ROOM READY")
            StatusCard("Offline sync", "WORKMANAGER READY")
            StatusCard(
                "Backend health",
                when (state.backendReachable) {
                    true -> "REACHABLE"
                    false -> "UNREACHABLE"
                    null -> "CHECKING"
                },
            )
            Button(onClick = viewModel::refreshBackendHealth, modifier = Modifier.fillMaxWidth()) {
                Text("Check backend")
            }
            Button(onClick = onOpenDiagnostics, modifier = Modifier.fillMaxWidth()) {
                Text("Foundation diagnostics")
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
        topBar = { TopAppBar(title = { Text("Foundation diagnostics") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("API base URL", style = MaterialTheme.typography.labelLarge)
            Text(BuildConfig.API_BASE_URL)
            Text("CameraX: dependency ready")
            Text("Room outbox: schema v1")
            Text("WorkManager: worker boundary ready")
            Text("Android Keystore: credential vault ready")
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Back")
            }
        }
    }
}
