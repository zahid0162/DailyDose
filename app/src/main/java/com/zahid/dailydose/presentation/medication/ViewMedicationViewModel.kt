package com.zahid.dailydose.presentation.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zahid.dailydose.domain.model.Medication
import com.zahid.dailydose.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class ViewMedicationUiState(
    val isLoading: Boolean = false,
    val medication: Medication? = null,
    val error: String? = null
)

class ViewMedicationViewModel : ViewModel(), KoinComponent {
    
    private val medicationRepository: MedicationRepository by inject()
    
    private val _uiState = MutableStateFlow(ViewMedicationUiState())
    val uiState: StateFlow<ViewMedicationUiState> = _uiState.asStateFlow()
    
    fun loadMedication(medicationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val medication = medicationRepository.getMedicationById(medicationId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    medication = medication,
                    error = if (medication == null) "Medication not found" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load medication"
                )
            }
        }
    }
}
