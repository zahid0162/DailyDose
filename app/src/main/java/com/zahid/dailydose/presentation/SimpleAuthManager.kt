package com.zahid.dailydose.presentation

import android.content.Context
import android.content.SharedPreferences
import com.zahid.dailydose.data.repository.OnboardingRepository
import com.zahid.dailydose.domain.repository.AuthRepository
import com.zahid.dailydose.domain.repository.PatientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay

data class AppState(
    val isOnboardingComplete: Boolean = false,
    val isLoggedIn: Boolean = false,
    val hasPatientProfile: Boolean = false,
    val userId: String? = null,
    val isLoading: Boolean = false
)

sealed class AppDestination {
    object Onboarding : AppDestination()
    object Login : AppDestination()
    object PatientProfile : AppDestination()
    object Home : AppDestination()
}

class SimpleAuthManager(
    private val context: Context,
    private val authRepository: AuthRepository,
    private val onboardingRepository: OnboardingRepository,
    private val patientRepository: PatientRepository
) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("app_state", Context.MODE_PRIVATE)
    
    suspend fun getAppState(): Flow<AppState> = flow {
        // First emit a loading state
        emit(
            AppState(
                isOnboardingComplete = false,
                isLoggedIn = false,
                hasPatientProfile = false,
                userId = null,
                isLoading = true
            )
        )
        
        try {
            // Add a small delay to allow authentication state to be properly initialized
            delay(100)
            
            val isOnboardingComplete = onboardingRepository.isOnboardingCompleted()
            
            // Try to get current user with proper error handling
            val currentUserResult = authRepository.getCurrentUser()
            val currentUser = currentUserResult.getOrNull()
            val isLoggedIn = currentUser != null
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
                AppState(
                    isOnboardingComplete = isOnboardingComplete,
                    isLoggedIn = isLoggedIn,
                    hasPatientProfile = hasPatientProfile,
                    userId = userId,
                    isLoading = false
                )
            )
        } catch (e: Exception) {
            // If there's any error, emit a safe default state
            emit(
                AppState(
                    isOnboardingComplete = false,
                    isLoggedIn = false,
                    hasPatientProfile = false,
                    userId = null,
                    isLoading = false
                )
            )
        }
    }
    
    fun getDestination(appState: AppState): AppDestination {
        return when {
            appState.isLoading -> AppDestination.Login // Show login screen while loading
            !appState.isOnboardingComplete -> AppDestination.Onboarding
            !appState.isLoggedIn -> AppDestination.Login
            appState.isLoggedIn && !appState.hasPatientProfile -> AppDestination.PatientProfile
            appState.isLoggedIn && appState.hasPatientProfile -> AppDestination.Home
            else -> AppDestination.Login
        }
    }
}
