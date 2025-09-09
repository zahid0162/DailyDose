package com.zahid.dailydose.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zahid.dailydose.data.repository.AuthState
import com.zahid.dailydose.data.repository.AuthStateManager
import com.zahid.dailydose.data.repository.StartDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class MainUiState(
    val authState: AuthState = AuthState(),
    val startDestination: StartDestination = StartDestination.Onboarding,
    val isInitialized: Boolean = false
)

class MainViewModel : ViewModel(), KoinComponent {
    
    private val authStateManager: AuthStateManager by inject()
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    init {
        initializeAuthState()
    }
    
    private fun initializeAuthState() {
        viewModelScope.launch {
            authStateManager.getAuthState().collect { authState ->
                val startDestination = authStateManager.getStartDestination(authState)
                _uiState.value = _uiState.value.copy(
                    authState = authState,
                    startDestination = startDestination,
                    isInitialized = true
                )
            }
        }
    }
    
    fun refreshAuthState() {
        initializeAuthState()
    }
}
