package com.zahid.dailydose.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zahid.dailydose.data.repository.AuthStateManager
import com.zahid.dailydose.data.service.DoseCalculationService
import com.zahid.dailydose.domain.model.Dose
import com.zahid.dailydose.domain.model.EnrichedDose
import com.zahid.dailydose.domain.model.Medication
import com.zahid.dailydose.domain.repository.DoseRepository
import com.zahid.dailydose.domain.repository.MedicationRepository
import com.zahid.dailydose.domain.repository.PatientRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Date

data class HomeUiState(
    val isLoading: Boolean = false,
    val medications: List<Medication> = emptyList(),
    val todaysMedications: List<Medication> = emptyList(),
    val todaysDoses: List<EnrichedDose> = emptyList(),
    val takenDosesCount: Int = 0,
    val pendingDosesCount: Int = 0,
    val error: String? = null,
    val userId: String? = null,
    val userName: String? = null
)

class HomeViewModel(val supabaseClient: SupabaseClient) : ViewModel(), KoinComponent {

    private val medicationRepository: MedicationRepository by inject()
    private val doseRepository: DoseRepository by inject()
    private val doseCalculationService = DoseCalculationService()

    private val patientRepository: PatientRepository by inject()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserMedications()
    }

    private fun loadUserMedications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id

                if (userId != null) {
                    loadProfile(userId)
                    val todaysMedications = medicationRepository.getTodaysMedications(userId)

                    // Calculate today's doses based on medication schedules
                    val calculatedDoses =
                        doseCalculationService.calculateTodaysDoses(todaysMedications, userId)

                    // Get taken doses from database (only doses that were actually taken)
                    val takenDoses = doseRepository.getTodaysDoses(userId)

                    // Determine status for each calculated dose based on logs and time
                    val dosesWithStatus = calculateDoseStatuses(calculatedDoses, takenDoses)

                    // Enrich doses with medication information
                    val enrichedDoses =
                        enrichDosesWithMedicationInfo(dosesWithStatus, todaysMedications)

                    // Calculate statistics
                    val takenCount =
                        enrichedDoses.count { it.status == com.zahid.dailydose.domain.model.DoseStatus.TAKEN }
                    val pendingCount = enrichedDoses.count {
                        it.status == com.zahid.dailydose.domain.model.DoseStatus.UPCOMING ||
                                it.status == com.zahid.dailydose.domain.model.DoseStatus.DUE
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        todaysMedications = todaysMedications,
                        todaysDoses = enrichedDoses,
                        takenDosesCount = takenCount,
                        pendingDosesCount = pendingCount,
                        userId = userId
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

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            val userName = patientRepository.getPatient(userId).fold(
                onSuccess = { patient ->
                    patient.personalInfo.fullName
                },
                onFailure = { e ->
                    ""
                }
            )
            _uiState.update {
                it.copy(userName = userName)
            }
        }
    }

    fun refreshMedications() {
        loadUserMedications()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun markDoseAsTaken(index: Int) {
        viewModelScope.launch {
            try {
                val doseToMark =
                    if (index < _uiState.value.todaysDoses.size) _uiState.value.todaysDoses[index] else null
                if (doseToMark != null) {
                    // Create a new dose log entry for this taken dose
                    val newDoseLog = Dose(
                        medicationId = doseToMark.medicationId,
                        userId = doseToMark.userId,
                        doseTime = doseToMark.doseTime,
                        scheduledTime = doseToMark.scheduledTime,
                        status = com.zahid.dailydose.domain.model.DoseStatus.TAKEN,
                        takenAt = Date()
                    )
                    val result = doseRepository.createDoseLog(newDoseLog)
                    if (result.isSuccess) {
                        loadUserMedications()
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = result.exceptionOrNull()?.message
                                ?: "Failed to mark dose as taken"
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to mark dose as taken"
                )
            }
        }
    }

    private fun calculateDoseStatuses(
        calculatedDoses: List<Dose>,
        takenDoses: List<Dose>
    ): List<Dose> {
        val now = Date()

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
                val timeDifference = calculatedDose.doseTime.time - now.time
                val status = when {
                    timeDifference > 30 * 60 * 1000 -> com.zahid.dailydose.domain.model.DoseStatus.UPCOMING // More than 30 minutes in future
                    timeDifference > -30 * 60 * 1000 -> com.zahid.dailydose.domain.model.DoseStatus.DUE // Within 30 minutes (past or future)
                    else -> com.zahid.dailydose.domain.model.DoseStatus.MISSED // More than 30 minutes past
                }

                calculatedDose.copy(status = status)
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
}
