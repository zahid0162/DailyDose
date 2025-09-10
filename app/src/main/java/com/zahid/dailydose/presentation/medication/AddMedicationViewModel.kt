package com.zahid.dailydose.presentation.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zahid.dailydose.data.repository.AuthStateManager
import com.zahid.dailydose.data.service.NotificationService
import com.zahid.dailydose.domain.model.*
import com.zahid.dailydose.domain.repository.MedicationRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.*

data class AddMedicationUiState(
    val currentStep: Int = 1,
    val medicationName: String = "",
    val medicationNameError: String? = null,
    val form: MedicationForm? = null,
    val strength: String = "",
    val strengthError: String? = null,
    val category: MedicationCategory? = null,
    val startDate: String = "",
    val endDate: String = "",
    val isOngoing: Boolean = true,
    val timesPerDay: Int = 1,
    val specificTimes: List<String> = listOf("08:00"),
    val mealTiming: MealTiming? = null,
    val remindersEnabled: Boolean = true,
    val reminderType: ReminderType = ReminderType.DEFAULT,
    val prescribedBy: String = "",
    val prescriptionFileUrl: String? = null,
    val refillCount: Int? = null,
    val notes: String = "",
    val canProceed: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false,
    val editingMedicationId: String? = null
)

class AddMedicationViewModel(val supabaseClient: SupabaseClient) : ViewModel(), KoinComponent {
    
    private val medicationRepository: MedicationRepository by inject()
    private val notificationService: NotificationService by inject()
    
    private val _uiState = MutableStateFlow(AddMedicationUiState())
    val uiState: StateFlow<AddMedicationUiState> = _uiState.asStateFlow()
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    init {
        updateCanProceed()
    }
    
    fun updateMedicationName(name: String) {
        _uiState.value = _uiState.value.copy(
            medicationName = name,
            medicationNameError = if (name.isBlank()) "Medication name is required" else null
        )
        updateCanProceed()
    }
    
    fun updateForm(form: MedicationForm) {
        _uiState.value = _uiState.value.copy(form = form)
        updateCanProceed()
    }
    
    fun updateStrength(strength: String) {
        _uiState.value = _uiState.value.copy(
            strength = strength,
            strengthError = if (strength.isBlank()) "Strength is required" else null
        )
        updateCanProceed()
    }
    
    fun updateCategory(category: MedicationCategory?) {
        _uiState.value = _uiState.value.copy(category = category)
    }
    
    fun updateStartDate(date: String) {
        _uiState.value = _uiState.value.copy(startDate = date)
        updateCanProceed()
    }
    
    fun updateEndDate(date: String) {
        _uiState.value = _uiState.value.copy(endDate = date)
    }
    
    fun updateIsOngoing(isOngoing: Boolean) {
        _uiState.value = _uiState.value.copy(
            isOngoing = isOngoing,
            endDate = if (isOngoing) "" else _uiState.value.endDate
        )
    }
    
    fun updateTimesPerDay(times: Int) {
        _uiState.value = _uiState.value.copy(timesPerDay = times)
        
        // Update specific times based on times per day
        val currentTimes = _uiState.value.specificTimes
        val newTimes = when {
            times > currentTimes.size -> {
                currentTimes + (1..times - currentTimes.size).map { 
                    when (it) {
                        1 -> "08:00"
                        2 -> "14:00"
                        3 -> "20:00"
                        else -> "12:00"
                    }
                }
            }
            times < currentTimes.size -> {
                currentTimes.take(times)
            }
            else -> currentTimes
        }
        
        _uiState.value = _uiState.value.copy(specificTimes = newTimes)
    }
    
    fun updateSpecificTime(index: Int, time: String) {
        val newTimes = _uiState.value.specificTimes.toMutableList()
        if (index < newTimes.size) {
            newTimes[index] = time
            _uiState.value = _uiState.value.copy(specificTimes = newTimes)
        }
    }
    
    fun addSpecificTime() {
        val newTimes = _uiState.value.specificTimes.toMutableList()
        newTimes.add("12:00")
        _uiState.value = _uiState.value.copy(specificTimes = newTimes)
    }
    
    fun removeSpecificTime(index: Int) {
        val newTimes = _uiState.value.specificTimes.toMutableList()
        if (index < newTimes.size) {
            newTimes.removeAt(index)
            _uiState.value = _uiState.value.copy(specificTimes = newTimes)
        }
    }
    
    fun updateMealTiming(timing: MealTiming?) {
        _uiState.value = _uiState.value.copy(mealTiming = timing)
    }
    
    fun updateRemindersEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(remindersEnabled = enabled)
    }
    
    fun updateReminderType(type: ReminderType) {
        _uiState.value = _uiState.value.copy(reminderType = type)
    }
    
    fun updatePrescribedBy(doctor: String) {
        _uiState.value = _uiState.value.copy(prescribedBy = doctor)
    }
    
    fun updateRefillCount(count: Int?) {
        _uiState.value = _uiState.value.copy(refillCount = count)
    }
    
    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }
    
    fun updatePrescriptionFileUrl(url: String?) {
        _uiState.value = _uiState.value.copy(prescriptionFileUrl = url)
    }
    
    fun showError(error: String) {
        _uiState.value = _uiState.value.copy(error = error)
    }
    
    fun nextStep() {
        if (_uiState.value.currentStep < 5) {
            _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep + 1)
            updateCanProceed()
        }
    }
    
    fun previousStep() {
        if (_uiState.value.currentStep > 1) {
            _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep - 1)
            updateCanProceed()
        }
    }
    
    private fun updateCanProceed() {
        val state = _uiState.value
        val canProceed = when (state.currentStep) {
            1 -> state.medicationName.isNotBlank() && 
                  state.form != null && 
                  state.strength.isNotBlank()
            2 -> state.startDate.isNotBlank() && 
                  (state.isOngoing || state.endDate.isNotBlank())
            3 -> true // Reminders step is always valid
            4 -> true // Doctor step is always valid
            5 -> true // Notes step is always valid
            else -> false
        }
        _uiState.value = _uiState.value.copy(canProceed = canProceed)
    }
    
    fun loadMedicationForEdit(medicationId: String) {
        viewModelScope.launch {
            try {
                val medication = medicationRepository.getMedicationById(medicationId)
                if (medication != null) {
                    _uiState.value = _uiState.value.copy(
                        isEditMode = true,
                        editingMedicationId = medicationId,
                        medicationName = medication.name,
                        form = medication.form,
                        strength = medication.strength,
                        category = medication.category,
                        startDate = dateFormat.format(medication.startDate),
                        endDate = medication.endDate?.let { dateFormat.format(it) } ?: "",
                        isOngoing = medication.isOngoing,
                        timesPerDay = medication.timesPerDay,
                        specificTimes = medication.specificTimes,
                        mealTiming = medication.mealTiming,
                        remindersEnabled = medication.remindersEnabled,
                        reminderType = medication.reminderType,
                        prescribedBy = medication.prescribedBy ?: "",
                        prescriptionFileUrl = medication.prescriptionFileUrl,
                        refillCount = medication.refillCount,
                        notes = medication.notes ?: ""
                    )
                    updateCanProceed()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load medication for editing"
                )
            }
        }
    }
    
    fun saveMedication() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id
                
                if (userId == null) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = "User not authenticated"
                    )
                    return@launch
                }
                
                val state = _uiState.value
                
                // Parse dates
                val startDate = try {
                    dateFormat.parse(state.startDate) ?: Date()
                } catch (e: Exception) {
                    Date()
                }
                
                val endDate = if (state.isOngoing) {
                    null
                } else {
                    try {
                        dateFormat.parse(state.endDate)
                    } catch (e: Exception) {
                        null
                    }
                }
                
                val medication = Medication(
                    id = state.editingMedicationId,
                    userId = userId,
                    name = state.medicationName,
                    form = state.form!!,
                    strength = state.strength,
                    dosage = state.strength, // Using strength as dosage for now
                    startDate = startDate,
                    endDate = endDate,
                    isOngoing = state.isOngoing,
                    timesPerDay = state.timesPerDay,
                    specificTimes = state.specificTimes,
                    mealTiming = state.mealTiming,
                    remindersEnabled = state.remindersEnabled,
                    reminderType = state.reminderType,
                    prescribedBy = state.prescribedBy.ifBlank { null },
                    prescriptionFileUrl = state.prescriptionFileUrl,
                    notes = state.notes.ifBlank { null },
                    refillCount = state.refillCount,
                    category = state.category
                )
                
                val result = if (state.isEditMode) {
                    medicationRepository.updateMedication(medication)
                } else {
                    medicationRepository.addMedication(medication)
                }
                
                if (result.isSuccess) {
                    if (medication.remindersEnabled) {
                        try {
                            result.getOrNull()?.let {
                                notificationService.scheduleMedicationReminders(it)
                            }

                        } catch (e: Exception) {
                            println("Failed to schedule notifications: ${e.message}")
                        }
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        isSaved = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to save medication"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }
}
