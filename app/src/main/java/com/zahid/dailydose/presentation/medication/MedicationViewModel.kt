package com.zahid.dailydose.presentation.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zahid.dailydose.data.repository.AuthStateManager
import com.zahid.dailydose.domain.model.Medication
import com.zahid.dailydose.domain.repository.MedicationRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class MedicationUiState(
    val isLoading: Boolean = false,
    val medications: List<Medication> = emptyList(),
    val error: String? = null
)

class MedicationViewModel(val supabaseClient: SupabaseClient) : ViewModel(), KoinComponent {
    
    private val medicationRepository: MedicationRepository by inject()

    
    private val _uiState = MutableStateFlow(MedicationUiState())
    val uiState: StateFlow<MedicationUiState> = _uiState.asStateFlow()
    
    init {
        loadMedications()
    }
    
    private fun loadMedications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id
                
                if (userId != null) {
                    val medications = medicationRepository.getMedicationsByUserId(userId)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        medications = medications
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "User not authenticated"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load medications"
                )
            }
        }
    }
    
    fun refreshMedications() {
        loadMedications()
    }
    
    fun deleteMedication(medicationId: String) {
        viewModelScope.launch {
            try {
                val result = medicationRepository.deleteMedication(medicationId)
                if (result.isSuccess) {
                    // Refresh the medications list after successful deletion
                    loadMedications()
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.exceptionOrNull()?.message ?: "Failed to delete medication"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete medication"
                )
            }
        }
    }
}
