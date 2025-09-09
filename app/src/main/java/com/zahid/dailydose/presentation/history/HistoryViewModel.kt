package com.zahid.dailydose.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zahid.dailydose.data.service.DoseCalculationService
import com.zahid.dailydose.domain.model.Dose
import com.zahid.dailydose.domain.model.EnrichedDose
import com.zahid.dailydose.domain.model.Medication
import com.zahid.dailydose.domain.repository.DoseRepository
import com.zahid.dailydose.domain.repository.MedicationRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.*

data class HistoryUiState(
    val isLoading: Boolean = false,
    val selectedDate: Date = Date(),
    val selectedDateString: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val medications: List<Medication> = emptyList(),
    val doses: List<EnrichedDose> = emptyList(),
    val takenDosesCount: Int = 0,
    val missedDosesCount: Int = 0,
    val pendingDosesCount: Int = 0,
    val error: String? = null,
    val userId: String? = null
)

class HistoryViewModel(val supabaseClient: SupabaseClient) : ViewModel(), KoinComponent {

    private val medicationRepository: MedicationRepository by inject()
    private val doseRepository: DoseRepository by inject()
    private val doseCalculationService = DoseCalculationService()

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
            if (userId != null) {
                _uiState.value = _uiState.value.copy(userId = userId)
                loadDosesForSelectedDate()
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "User not authenticated"
                )
            }
        }
    }

    fun selectDate(date: Date) {
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            selectedDateString = dateString
        )
        loadDosesForSelectedDate()
    }

    fun navigateToPreviousDay() {
        val calendar = Calendar.getInstance()
        calendar.time = _uiState.value.selectedDate
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        selectDate(calendar.time)
    }

    fun navigateToNextDay() {
        val calendar = Calendar.getInstance()
        calendar.time = _uiState.value.selectedDate
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        selectDate(calendar.time)
    }

    private fun loadDosesForSelectedDate() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val userId = _uiState.value.userId
                if (userId != null) {
                    val selectedDate = _uiState.value.selectedDate
                    
                    // Get medications for the selected date
                    val medications = medicationRepository.getMedicationsForDate(userId, selectedDate)
                    
                    // Calculate doses for the selected date
                    val calculatedDoses = doseCalculationService.calculateDosesForDate(medications, userId, selectedDate)
                    
                    // Get taken doses from database for the selected date
                    val takenDoses = getDosesForDate(userId, selectedDate)
                    
                    // Determine status for each calculated dose
                    val dosesWithStatus = calculateDoseStatuses(calculatedDoses, takenDoses, selectedDate)
                    
                    // Enrich doses with medication information
                    val enrichedDoses = enrichDosesWithMedicationInfo(dosesWithStatus, medications)
                    
                    // Calculate statistics
                    val takenCount = enrichedDoses.count { it.status == com.zahid.dailydose.domain.model.DoseStatus.TAKEN }
                    val missedCount = enrichedDoses.count { it.status == com.zahid.dailydose.domain.model.DoseStatus.MISSED }
                    val pendingCount = enrichedDoses.count { 
                        it.status == com.zahid.dailydose.domain.model.DoseStatus.UPCOMING ||
                        it.status == com.zahid.dailydose.domain.model.DoseStatus.DUE
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        medications = medications,
                        doses = enrichedDoses,
                        takenDosesCount = takenCount,
                        missedDosesCount = missedCount,
                        pendingDosesCount = pendingCount
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
                    error = e.message ?: "Failed to load doses"
                )
            }
        }
    }

    private suspend fun getDosesForDate(userId: String, date: Date): List<Dose> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = calendar.timeInMillis
            
            // Use the existing dose repository method but with custom date range
            // We'll need to extend the repository for this
            doseRepository.getDosesForDateRange(userId, startOfDay, endOfDay)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun calculateDoseStatuses(
        calculatedDoses: List<Dose>,
        takenDoses: List<Dose>,
        selectedDate: Date
    ): List<Dose> {
        val now = Date()
        val isToday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now) == 
                     SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate)

        return calculatedDoses.map { calculatedDose ->
            // Check if this dose was taken (exists in logs)
            val takenDose = takenDoses.find { taken ->
                taken.medicationId == calculatedDose.medicationId &&
                taken.scheduledTime == calculatedDose.scheduledTime
            }

            if (takenDose != null) {
                // Dose was taken - use the taken dose data
                takenDose
            } else {
                // Dose was not taken - determine status based on time
                if (isToday) {
                    // For today, use the same logic as HomeViewModel
                    val timeDifference = calculatedDose.doseTime.time - now.time
                    val status = when {
                        timeDifference > 30 * 60 * 1000 -> com.zahid.dailydose.domain.model.DoseStatus.UPCOMING
                        timeDifference > -30 * 60 * 1000 -> com.zahid.dailydose.domain.model.DoseStatus.DUE
                        else -> com.zahid.dailydose.domain.model.DoseStatus.MISSED
                    }
                    calculatedDose.copy(status = status)
                } else {
                    // For past dates, if not taken, it's missed
                    calculatedDose.copy(status = com.zahid.dailydose.domain.model.DoseStatus.MISSED)
                }
            }
        }
    }

    private fun enrichDosesWithMedicationInfo(
        doses: List<Dose>,
        medications: List<Medication>
    ): List<EnrichedDose> {
        return doses.map { dose ->
            val medication = medications.find { it.id == dose.medicationId }
            EnrichedDose(
                id = dose.id,
                medicationId = dose.medicationId,
                userId = dose.userId,
                doseTime = dose.doseTime,
                scheduledTime = dose.scheduledTime,
                status = dose.status,
                takenAt = dose.takenAt,
                notes = dose.notes,
                createdAt = dose.createdAt,
                updatedAt = dose.updatedAt,
                medicationName = medication?.name ?: "Unknown Medication",
                medicationStrength = medication?.strength ?: "",
                medicationForm = medication?.form?.name ?: "",
                medicationDosage = medication?.dosage ?: "",
                mealTiming = medication?.mealTiming?.name
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun refreshDoses() {
        loadDosesForSelectedDate()
    }
}
