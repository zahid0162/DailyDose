package com.zahid.dailydose.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zahid.dailydose.domain.model.RegisterRequest
import com.zahid.dailydose.domain.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistrationSuccessful: Boolean = false
)

sealed class RegisterEvent {
    data class EmailChanged(val email: String) : RegisterEvent()
    data class PasswordChanged(val password: String) : RegisterEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : RegisterEvent()
    object RegisterClicked : RegisterEvent()
    object NavigateToLogin : RegisterEvent()
    object ClearError : RegisterEvent()
}

sealed class RegisterEffect {
    object NavigateToLogin : RegisterEffect()
    object NavigateToPatientOnboarding : RegisterEffect()
    data class ShowError(val message: String) : RegisterEffect()
}

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
    
    private val _effects = Channel<RegisterEffect>()
    val effects = _effects.receiveAsFlow()
    
    fun handleEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.EmailChanged -> {
                _uiState.value = _uiState.value.copy(
                    email = event.email,
                    errorMessage = null
                )
            }
            is RegisterEvent.PasswordChanged -> {
                _uiState.value = _uiState.value.copy(
                    password = event.password,
                    errorMessage = null
                )
            }
            is RegisterEvent.ConfirmPasswordChanged -> {
                _uiState.value = _uiState.value.copy(
                    confirmPassword = event.confirmPassword,
                    errorMessage = null
                )
            }
            is RegisterEvent.RegisterClicked -> {
                register()
            }
            is RegisterEvent.NavigateToLogin -> {
                viewModelScope.launch {
                    _effects.send(RegisterEffect.NavigateToLogin)
                }
            }
            is RegisterEvent.ClearError -> {
                _uiState.value = _uiState.value.copy(errorMessage = null)
            }
        }
    }
    
    private fun register() {
        val currentState = _uiState.value
        
        // Validation
        if (currentState.email.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "Email is required")
            return
        }
        
        if (currentState.password.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "Password is required")
            return
        }
        
        if (currentState.confirmPassword.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "Please confirm your password")
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _uiState.value = currentState.copy(errorMessage = "Please enter a valid email")
            return
        }
        
        if (currentState.password.length < 6) {
            _uiState.value = currentState.copy(errorMessage = "Password must be at least 6 characters")
            return
        }
        
        if (currentState.password != currentState.confirmPassword) {
            _uiState.value = currentState.copy(errorMessage = "Passwords do not match")
            return
        }
        
        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            val result = authRepository.register(
                RegisterRequest(
                    email = currentState.email,
                    password = currentState.password
                )
            )
            
            result.fold(
                onSuccess = { authResponse ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRegistrationSuccessful = true
                    )
                    _effects.send(RegisterEffect.NavigateToPatientOnboarding)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Registration failed"
                    )
                }
            )
        }
    }
}
