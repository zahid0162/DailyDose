package com.zahid.dailydose.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zahid.dailydose.data.service.NotificationService
import com.zahid.dailydose.domain.model.LoginRequest
import com.zahid.dailydose.domain.repository.AuthRepository
import com.zahid.dailydose.domain.repository.MedicationRepository
import com.zahid.dailydose.domain.repository.PatientRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccessful: Boolean = false
)

sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    object LoginClicked : LoginEvent()
    object NavigateToRegister : LoginEvent()
    object ClearError : LoginEvent()
}

sealed class LoginEffect {
    object NavigateToHome : LoginEffect()
    object NavigateToPatientOnboarding : LoginEffect()
    object NavigateToRegister : LoginEffect()
    data class ShowError(val message: String) : LoginEffect()
}

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val patientRepository: PatientRepository,
    val supabaseClient: SupabaseClient
) : ViewModel(), KoinComponent {

    private val medicationRepository: MedicationRepository by inject()
    private val notificationService: NotificationService by inject()
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    private val _effects = Channel<LoginEffect>()
    val effects = _effects.receiveAsFlow()
    
    fun handleEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                _uiState.value = _uiState.value.copy(
                    email = event.email,
                    errorMessage = null
                )
            }
            is LoginEvent.PasswordChanged -> {
                _uiState.value = _uiState.value.copy(
                    password = event.password,
                    errorMessage = null
                )
            }
            is LoginEvent.LoginClicked -> {
                login()
            }
            is LoginEvent.NavigateToRegister -> {
                viewModelScope.launch {
                    _effects.send(LoginEffect.NavigateToRegister)
                }
            }
            is LoginEvent.ClearError -> {
                _uiState.value = _uiState.value.copy(errorMessage = null)
            }
        }
    }
    
    private fun login() {
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
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _uiState.value = currentState.copy(errorMessage = "Please enter a valid email")
            return
        }
        
        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            val result = authRepository.login(
                LoginRequest(
                    email = currentState.email,
                    password = currentState.password
                )
            )
            
            result.fold(
                onSuccess = { authResponse ->
                    loadMedications()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccessful = true
                    )
                    
                    // Check if user has completed patient profile
                    val currentUser = authRepository.getCurrentUser().getOrNull()
                    if (currentUser != null) {
                        val hasProfile = patientRepository.hasPatientProfile(currentUser.id)
                        if (hasProfile) {
                            _effects.send(LoginEffect.NavigateToHome)
                        } else {
                            _effects.send(LoginEffect.NavigateToPatientOnboarding)
                        }
                    } else {
                        _effects.send(LoginEffect.NavigateToPatientOnboarding)
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message?.split("\n")?.get(0) ?: "Login failed"
                    )
                }
            )
        }
    }

    private fun loadMedications() {
        viewModelScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id

                if (userId != null) {
                    val medications = medicationRepository.getMedicationsByUserId(userId)
                    medications.forEach {
                        notificationService.scheduleMedicationReminders(it)
                    }
                }
            } catch (e: Exception) { }
        }
    }
}
