package com.zahid.dailydose.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zahid.dailydose.domain.model.Patient
import com.zahid.dailydose.domain.model.User
import com.zahid.dailydose.domain.repository.AuthRepository
import com.zahid.dailydose.domain.repository.PatientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class SettingsUiState(
    val user: User? = null,
    val patient: Patient? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditingEmail: Boolean = false,
    val newEmail: String = "",
    val showDeleteAccountDialog: Boolean = false,
    val isUpdatingEmail: Boolean = false
)

class SettingsViewModel() : ViewModel(), KoinComponent {

    private val authRepository: AuthRepository by inject()
    private val patientRepository: PatientRepository  by inject()
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val userResult = authRepository.getCurrentUser()
                if (userResult.isSuccess) {
                    val user = userResult.getOrNull()
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        newEmail = user?.email ?: "",
                        isLoading = false
                    )
                    
                    // Load patient data if user exists
                    user?.let { loadPatientData(it.id) }
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to load user data",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Unknown error occurred",
                    isLoading = false
                )
            }
        }
    }

    private fun loadPatientData(userId: String) {
        viewModelScope.launch {
            try {
                val patientResult = patientRepository.getPatient(userId)
                if (patientResult.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        patient = patientResult.getOrNull(),

                    )
                }
            } catch (e: Exception) {
                // Patient data is optional, don't show error for this
            }
        }
    }

    fun startEditingEmail() {
        _uiState.value = _uiState.value.copy(
            isEditingEmail = true,
            newEmail = _uiState.value.user?.email ?: ""
        )
    }

    fun cancelEditingEmail() {
        _uiState.value = _uiState.value.copy(
            isEditingEmail = false,
            newEmail = _uiState.value.user?.email ?: ""
        )
    }

    fun updateEmail(newEmail: String) {
        _uiState.value = _uiState.value.copy(newEmail = newEmail)
    }

    fun saveEmail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdatingEmail = true)
            
            try {
                // Note: This would require implementing email update in AuthRepository
                // For now, we'll just simulate the update
                val currentUser = _uiState.value.user
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(
                        email = _uiState.value.newEmail,
                        updatedAt = System.currentTimeMillis()
                    )
                    _uiState.value = _uiState.value.copy(
                        user = updatedUser,
                        isEditingEmail = false,
                        isUpdatingEmail = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update email",
                    isUpdatingEmail = false
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to logout"
                )
            }
        }
    }

    fun showDeleteAccountDialog() {
        _uiState.value = _uiState.value.copy(showDeleteAccountDialog = true)
    }

    fun hideDeleteAccountDialog() {
        _uiState.value = _uiState.value.copy(showDeleteAccountDialog = false)
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                // Note: This would require implementing account deletion in AuthRepository
                // For now, we'll just logout
                authRepository.logout()
                _uiState.value = _uiState.value.copy(showDeleteAccountDialog = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete account",
                    showDeleteAccountDialog = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
