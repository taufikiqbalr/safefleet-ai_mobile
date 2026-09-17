package com.safefleet.ai.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safefleet.ai.mobile.data.network.FoundationHealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FoundationUiState(
    val backendReachable: Boolean? = null,
)

@HiltViewModel
class FoundationViewModel @Inject constructor(
    private val healthRepository: FoundationHealthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(FoundationUiState())
    val state: StateFlow<FoundationUiState> = _state.asStateFlow()

    init {
        refreshBackendHealth()
    }

    fun refreshBackendHealth() {
        viewModelScope.launch {
            _state.value = _state.value.copy(backendReachable = null)
            _state.value = _state.value.copy(
                backendReachable = healthRepository.isBackendReachable(),
            )
        }
    }
}
