package com.zahid.dailydose.data.repository

import com.zahid.dailydose.domain.repository.AuthRepository
import com.zahid.dailydose.domain.repository.PatientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

data class AuthState(
    val isOnboardingCompleted: Boolean = false,
    val isAuthenticated: Boolean = false,
    val hasPatientProfile: Boolean = false,
    val userId: String? = null,
    val isLoading: Boolean = true
)

sealed class StartDestination {
    object Onboarding : StartDestination()
    object Login : StartDestination()
    object PatientOnboarding : StartDestination()
    object Home : StartDestination()
}

class AuthStateManager(
    private val authRepository: AuthRepository,
    private val onboardingRepository: OnboardingRepository,
    private val patientRepository: PatientRepository
) {
    
    fun getAuthState(): Flow<AuthState> = flow {
        emit(AuthState(isLoading = true))
        
        try {
            val isOnboardingCompleted = onboardingRepository.isOnboardingCompleted()
            
            // Try to get current user - this will check Supabase session if needed
            val currentUserResult = authRepository.getCurrentUser()
            val currentUser = currentUserResult.getOrNull()
            val isAuthenticated = currentUser != null
            val userId = currentUser?.id
            
            val hasPatientProfile = if (userId != null) {
                try {
                    patientRepository.hasPatientProfile(userId)
                } catch (e: Exception) {
                    false
                }
            } else {
                false
            }
            
            emit(
                AuthState(
                    isOnboardingCompleted = isOnboardingCompleted,
                    isAuthenticated = isAuthenticated,
                    hasPatientProfile = hasPatientProfile,
                    userId = userId,
                    isLoading = false
                )
            )
        } catch (e: Exception) {
            emit(
                AuthState(
                    isOnboardingCompleted = false,
                    isAuthenticated = false,
                    hasPatientProfile = false,
                    userId = null,
                    isLoading = false
                )
            )
        }
    }
    
    fun getStartDestination(authState: AuthState): StartDestination {
        return when {
            authState.isLoading -> StartDestination.Onboarding // Show loading while checking
            !authState.isOnboardingCompleted -> StartDestination.Onboarding
            !authState.isAuthenticated -> StartDestination.Login
            authState.isAuthenticated && !authState.hasPatientProfile -> StartDestination.PatientOnboarding
            authState.isAuthenticated && authState.hasPatientProfile -> StartDestination.Home
            else -> StartDestination.Login
        }
    }
}
