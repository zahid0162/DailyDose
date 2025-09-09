package com.zahid.dailydose.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zahid.dailydose.domain.repository.AuthRepository
import com.zahid.dailydose.domain.repository.PatientRepository
import com.zahid.dailydose.data.repository.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class SplashUiState(
    val isLoading: Boolean = true,
    val destination: SplashDestination = SplashDestination.Loading
)

sealed class SplashDestination {
    object Loading : SplashDestination()
    object Onboarding : SplashDestination()
    object Login : SplashDestination()
    object PatientProfile : SplashDestination()
    object Home : SplashDestination()
}

class SplashViewModel(
    private val authRepository: AuthRepository,
    private val onboardingRepository: OnboardingRepository,
    private val patientRepository: PatientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkAppState()
    }

    private fun checkAppState() {
        viewModelScope.launch {
            try {
                // Add a small delay to show splash screen
                delay(2000) // 2 seconds splash screen
                
                // Check onboarding status
                val isOnboardingComplete = onboardingRepository.isOnboardingCompleted()
                if (!isOnboardingComplete) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        destination = SplashDestination.Onboarding
                    )
                    return@launch
                }
                
                // Check authentication status
                val currentUserResult = authRepository.getCurrentUser()
                val currentUser = currentUserResult.getOrNull()
                val isLoggedIn = currentUser != null
                
                if (!isLoggedIn) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        destination = SplashDestination.Login
                    )
                    return@launch
                }
                
                // Check patient profile status
                val userId = currentUser.id
                val hasPatientProfile = try {
                    patientRepository.hasPatientProfile(userId)
                } catch (e: Exception) {
                    false
                }
                
                val destination = if (hasPatientProfile) {
                    SplashDestination.Home
                } else {
                    SplashDestination.PatientProfile
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    destination = destination
                )
                
            } catch (e: Exception) {
                // On any error, go to login
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    destination = SplashDestination.Login
                )
            }
        }
    }
}
