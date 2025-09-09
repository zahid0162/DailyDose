package com.zahid.dailydose.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class SimpleMainUiState(
    val appState: AppState = AppState(),
    val destination: AppDestination = AppDestination.Onboarding,
    val isReady: Boolean = false
)

class SimpleMainViewModel : ViewModel(), KoinComponent {
    
    private val authManager: SimpleAuthManager by inject()
    
    private val _uiState = MutableStateFlow(SimpleMainUiState())
    val uiState: StateFlow<SimpleMainUiState> = _uiState.asStateFlow()
    
    init {
        loadAppState()
    }
    
    private fun loadAppState() {
        viewModelScope.launch {
            authManager.getAppState().collect { appState ->
                val destination = authManager.getDestination(appState)
                _uiState.value = _uiState.value.copy(
                    appState = appState,
                    destination = destination,
                    isReady = true
                )
            }
        }
    }
    
    fun refresh() {
        loadAppState()
    }
}
