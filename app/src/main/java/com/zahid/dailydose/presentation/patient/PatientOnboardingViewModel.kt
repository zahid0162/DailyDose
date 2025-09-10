package com.zahid.dailydose.presentation.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zahid.dailydose.domain.model.*
import com.zahid.dailydose.domain.repository.AuthRepository
import com.zahid.dailydose.domain.repository.PatientRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*



sealed class PatientOnboardingEvent {
    // Navigation
    object NextStep : PatientOnboardingEvent()
    object PreviousStep : PatientOnboardingEvent()
    object CompleteOnboarding : PatientOnboardingEvent()

    // Step 1: Personal Info
    data class FullNameChanged(val name: String) : PatientOnboardingEvent()
    data class DateOfBirthChanged(val date: Date) : PatientOnboardingEvent()
    data class GenderChanged(val gender: Gender) : PatientOnboardingEvent()
    data class ContactNumberChanged(val number: String) : PatientOnboardingEvent()

    // Step 2: Medical Info
    data class MedicalConditionAdded(val condition: String) : PatientOnboardingEvent()
    data class MedicalConditionRemoved(val condition: String) : PatientOnboardingEvent()
    data class AllergyAdded(val allergy: String) : PatientOnboardingEvent()
    data class AllergyRemoved(val allergy: String) : PatientOnboardingEvent()
    data class PrimaryDoctorNameChanged(val name: String) : PatientOnboardingEvent()
    data class PrimaryDoctorContactChanged(val contact: String) : PatientOnboardingEvent()

    // Step 3: Emergency Info
    data class EmergencyContactNameChanged(val name: String) : PatientOnboardingEvent()
    data class EmergencyContactPhoneChanged(val phone: String) : PatientOnboardingEvent()
    data class BloodGroupChanged(val bloodGroup: BloodGroup) : PatientOnboardingEvent()

    object ClearError : PatientOnboardingEvent()
    data class LoadPatientToEdit(val id:String) : PatientOnboardingEvent()
    object SetEditMode: PatientOnboardingEvent()
}

sealed class PatientOnboardingEffect {
    object NavigateToHome : PatientOnboardingEffect()
    data class ShowError(val message: String) : PatientOnboardingEffect()
}

class PatientOnboardingViewModel(
    private val authRepository: AuthRepository,
    private val patientRepository: PatientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientOnboardingUiState())
    val uiState: StateFlow<PatientOnboardingUiState> = _uiState.asStateFlow()

    private val _effects = Channel<PatientOnboardingEffect>()
    val effects = _effects.receiveAsFlow()

    fun handleEvent(event: PatientOnboardingEvent) {
        when (event) {
            is PatientOnboardingEvent.NextStep -> {
                if (validateCurrentStep()) {
                    _uiState.value = _uiState.value.copy(
                        currentStep = (_uiState.value.currentStep + 1).coerceAtMost(_uiState.value.totalSteps),
                        errorMessage = null
                    )
                }
            }

            is PatientOnboardingEvent.PreviousStep -> {
                _uiState.value = _uiState.value.copy(
                    currentStep = (_uiState.value.currentStep - 1).coerceAtLeast(1),
                    errorMessage = null
                )
            }

            is PatientOnboardingEvent.CompleteOnboarding -> {
                completeOnboarding()
            }

            // Step 1: Personal Info
            is PatientOnboardingEvent.FullNameChanged -> {
                _uiState.value = _uiState.value.copy(fullName = event.name)
            }

            is PatientOnboardingEvent.DateOfBirthChanged -> {
                _uiState.value = _uiState.value.copy(dateOfBirth = event.date)
            }

            is PatientOnboardingEvent.GenderChanged -> {
                _uiState.value = _uiState.value.copy(gender = event.gender)
            }

            is PatientOnboardingEvent.ContactNumberChanged -> {
                _uiState.value = _uiState.value.copy(contactNumber = event.number)
            }

            // Step 2: Medical Info
            is PatientOnboardingEvent.MedicalConditionAdded -> {
                val currentConditions = _uiState.value.medicalConditions.toMutableList()
                if (!currentConditions.contains(event.condition)) {
                    currentConditions.add(event.condition)
                    _uiState.value = _uiState.value.copy(medicalConditions = currentConditions)
                }
            }

            is PatientOnboardingEvent.MedicalConditionRemoved -> {
                val currentConditions = _uiState.value.medicalConditions.toMutableList()
                currentConditions.remove(event.condition)
                _uiState.value = _uiState.value.copy(medicalConditions = currentConditions)
            }

            is PatientOnboardingEvent.AllergyAdded -> {
                val currentAllergies = _uiState.value.allergies.toMutableList()
                if (!currentAllergies.contains(event.allergy)) {
                    currentAllergies.add(event.allergy)
                    _uiState.value = _uiState.value.copy(allergies = currentAllergies)
                }
            }

            is PatientOnboardingEvent.AllergyRemoved -> {
                val currentAllergies = _uiState.value.allergies.toMutableList()
                currentAllergies.remove(event.allergy)
                _uiState.value = _uiState.value.copy(allergies = currentAllergies)
            }

            is PatientOnboardingEvent.PrimaryDoctorNameChanged -> {
                _uiState.value = _uiState.value.copy(primaryDoctorName = event.name)
            }

            is PatientOnboardingEvent.PrimaryDoctorContactChanged -> {
                _uiState.value = _uiState.value.copy(primaryDoctorContact = event.contact)
            }

            // Step 3: Emergency Info
            is PatientOnboardingEvent.EmergencyContactNameChanged -> {
                _uiState.value = _uiState.value.copy(emergencyContactName = event.name)
            }

            is PatientOnboardingEvent.EmergencyContactPhoneChanged -> {
                _uiState.value = _uiState.value.copy(emergencyContactPhone = event.phone)
            }

            is PatientOnboardingEvent.BloodGroupChanged -> {
                _uiState.value = _uiState.value.copy(bloodGroup = event.bloodGroup)
            }

            is PatientOnboardingEvent.ClearError -> {
                _uiState.value = _uiState.value.copy(errorMessage = null)
            }

            is PatientOnboardingEvent.SetEditMode -> {
                _uiState.update {
                    it.copy(
                        isEditMode = true
                    )
                }
            }

            is PatientOnboardingEvent.LoadPatientToEdit -> {
                loadPatient(event.id)
            }
        }
    }

    private fun validateCurrentStep(): Boolean {
        val currentState = _uiState.value

        return when (currentState.currentStep) {
            1 -> {
                if (currentState.fullName.isBlank()) {
                    _uiState.value = currentState.copy(errorMessage = "Full name is required")
                    false
                } else if (currentState.dateOfBirth == null) {
                    _uiState.value = currentState.copy(errorMessage = "Date of birth is required")
                    false
                } else if (currentState.gender == null) {
                    _uiState.value = currentState.copy(errorMessage = "Gender is required")
                    false
                } else {
                    true
                }
            }

            2 -> {
                // Step 2 is optional, so always valid
                true
            }

            3 -> {
                if (currentState.emergencyContactName.isBlank()) {
                    _uiState.value =
                        currentState.copy(errorMessage = "Emergency contact name is required")
                    false
                } else if (currentState.emergencyContactPhone.isBlank()) {
                    _uiState.value =
                        currentState.copy(errorMessage = "Emergency contact phone is required")
                    false
                } else if (currentState.bloodGroup == null) {
                    _uiState.value = currentState.copy(errorMessage = "Blood group is required")
                    false
                } else {
                    true
                }
            }

            else -> true
        }
    }

    private fun completeOnboarding() {
        if (!validateCurrentStep()) return

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser().getOrThrow()

                val patient = Patient(
                    id = if (_uiState.value.isEditMode) _uiState.value.id else UUID.randomUUID().toString(),
                    userId = currentUser.id,
                    personalInfo = PersonalInfo(
                        fullName = _uiState.value.fullName,
                        dateOfBirth = _uiState.value.dateOfBirth!!,
                        gender = _uiState.value.gender!!,
                        contactNumber = _uiState.value.contactNumber.takeIf { it.isNotBlank() }
                    ),
                    medicalInfo = MedicalInfo(
                        medicalConditions = _uiState.value.medicalConditions,
                        allergies = _uiState.value.allergies,
                        primaryDoctorName = _uiState.value.primaryDoctorName.takeIf { it.isNotBlank() },
                        primaryDoctorContact = _uiState.value.primaryDoctorContact.takeIf { it.isNotBlank() }
                    ),

                    emergencyContactName = _uiState.value.emergencyContactName,
                    emergencyContactPhone = _uiState.value.emergencyContactPhone,
                    bloodGroup = _uiState.value.bloodGroup!!,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                val result = if (_uiState.value.isEditMode)patientRepository.updatePatient(patient) else patientRepository.createPatient(patient)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isOnboardingComplete = true
                        )
                        _effects.send(PatientOnboardingEffect.NavigateToHome)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to save patient information"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to complete onboarding"
                )
            }
        }
    }

    private  fun loadPatient(id:String){
        _uiState.update {
            it.copy(
                id = id
            )
        }
        viewModelScope.launch {
            val result = patientRepository.getPatient(id)
            if (result.isSuccess){
                val pat = result.getOrNull()
                pat?.let { pat ->
                    _uiState.update {
                        it.copy(
                            fullName = pat.personalInfo.fullName,
                            dateOfBirth = pat.personalInfo.dateOfBirth,
                            gender = pat.personalInfo.gender,
                            contactNumber = pat.personalInfo.contactNumber?:"",
                            medicalConditions = pat.medicalInfo.medicalConditions,
                            allergies = pat.medicalInfo.allergies,
                            primaryDoctorName = pat.medicalInfo.primaryDoctorName ?:"",
                            primaryDoctorContact = pat.medicalInfo.primaryDoctorContact?:"",
                            emergencyContactName = pat.emergencyContactName,
                            emergencyContactPhone = pat.emergencyContactPhone,
                            bloodGroup = pat.bloodGroup


                        )
                    }
                }
            }
        }
    }
}

data class PatientOnboardingUiState(
    val id:String = "",
    val currentStep: Int = 1,
    val totalSteps: Int = 3,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Step 1: Personal Info
    val fullName: String = "",
    val dateOfBirth: Date? = null,
    val gender: Gender? = null,
    val contactNumber: String = "",

    // Step 2: Medical Info
    val medicalConditions: List<String> = emptyList(),
    val allergies: List<String> = emptyList(),
    val primaryDoctorName: String = "",
    val primaryDoctorContact: String = "",

    // Step 3: Emergency Info
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val bloodGroup: BloodGroup? = null,

    val isOnboardingComplete: Boolean = false,
    val isEditMode:Boolean = false
)
