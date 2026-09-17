package com.safefleet.ai.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safefleet.ai.mobile.data.network.DeviceContextResponse
import com.safefleet.ai.mobile.data.network.FoundationHealthRepository
import com.safefleet.ai.mobile.data.runtime.DeviceRuntimeRepository
import com.safefleet.ai.mobile.device.DeviceHealthMonitor
import com.safefleet.ai.mobile.device.DeviceHealthSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DriverHomeUiState(
    val installationId: String = "",
    val pairingPayload: String = "",
    val paired: Boolean = false,
    val backendReachable: Boolean? = null,
    val context: DeviceContextResponse? = null,
    val health: DeviceHealthSnapshot? = null,
    val loading: Boolean = true,
    val actionInProgress: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

@HiltViewModel
class DriverHomeViewModel @Inject constructor(
    private val runtimeRepository: DeviceRuntimeRepository,
    private val healthRepository: FoundationHealthRepository,
    private val deviceHealthMonitor: DeviceHealthMonitor,
) : ViewModel() {
    private val _state = MutableStateFlow(DriverHomeUiState())
    val state: StateFlow<DriverHomeUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun updatePairingPayload(value: String) {
        _state.value = _state.value.copy(pairingPayload = value, error = null)
    }

    fun refresh() {
        viewModelScope.launch {
            val current = _state.value
            _state.value = current.copy(
                loading = true,
                backendReachable = null,
                health = deviceHealthMonitor.snapshot(),
                error = null,
            )

            val installationId = runtimeRepository.installationId()
            val paired = runtimeRepository.isPaired()
            val backendReachable = healthRepository.isBackendReachable()
            val contextResult = if (paired && backendReachable) {
                runCatching { runtimeRepository.context() }
            } else {
                null
            }

            _state.value = _state.value.copy(
                installationId = installationId,
                paired = paired,
                backendReachable = backendReachable,
                context = contextResult?.getOrNull(),
                health = deviceHealthMonitor.snapshot(),
                loading = false,
                error = contextResult?.exceptionOrNull()?.userMessage(),
            )
        }
    }

    fun pair() {
        val payload = _state.value.pairingPayload
        viewModelScope.launch {
            runAction("Device paired successfully") {
                runtimeRepository.pair(payload)
            }
        }
    }

    fun unpair() {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionInProgress = true, error = null, message = null)
            runtimeRepository.unpair()
            _state.value = _state.value.copy(
                paired = false,
                context = null,
                pairingPayload = "",
                actionInProgress = false,
                message = "Device credential removed from this installation",
            )
        }
    }

    fun startTrip() {
        viewModelScope.launch {
            runAction("Trip started") {
                runtimeRepository.startTrip()
                runtimeRepository.context()
            }
        }
    }

    fun completeTrip(tripId: String) {
        viewModelScope.launch {
            runAction("Trip completed") {
                runtimeRepository.completeTrip(tripId)
                runtimeRepository.context()
            }
        }
    }

    private suspend fun runAction(
        successMessage: String,
        action: suspend () -> DeviceContextResponse,
    ) {
        _state.value = _state.value.copy(actionInProgress = true, error = null, message = null)
        runCatching { action() }
            .onSuccess { context ->
                _state.value = _state.value.copy(
                    paired = true,
                    context = context,
                    actionInProgress = false,
                    message = successMessage,
                    error = null,
                    health = deviceHealthMonitor.snapshot(),
                )
            }
            .onFailure { throwable ->
                _state.value = _state.value.copy(
                    actionInProgress = false,
                    error = throwable.userMessage(),
                    health = deviceHealthMonitor.snapshot(),
                )
            }
    }

    private fun Throwable.userMessage(): String =
        message?.takeIf { it.isNotBlank() } ?: "SafeFleet operation failed"
}
