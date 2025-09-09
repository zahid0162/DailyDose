package com.zahid.dailydose.presentation.care

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zahid.dailydose.domain.model.HealthMetric
import com.zahid.dailydose.domain.model.HealthMetricType
import com.zahid.dailydose.domain.model.HealthMetricSummary
import com.zahid.dailydose.domain.model.Patient
import com.zahid.dailydose.domain.repository.HealthMetricRepository
import com.zahid.dailydose.domain.repository.PatientRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Date
import java.util.UUID


class CareViewModel(val supabaseClient: SupabaseClient) : ViewModel(), KoinComponent {
    private val healthMetricRepository: HealthMetricRepository by inject()
    private val patientRepository: PatientRepository by inject()
    private val _uiState = MutableStateFlow(CareUiState())
    val uiState: StateFlow<CareUiState> = _uiState.asStateFlow()

    private val _effects = Channel<CareEvents>()
    val effects = _effects.receiveAsFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val currentUser = supabaseClient.auth.currentUserOrNull()?.id
                if (currentUser != null) {
                    // Load patient data for emergency contact
                    loadProfile(currentUser)

                    // Load health metrics
                    val metricsResult = healthMetricRepository.getLatestHealthMetrics(currentUser)
                    metricsResult.onSuccess { metrics ->
                        _uiState.value = _uiState.value.copy(healthMetrics = metrics)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun loadAllData(type: HealthMetricType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val currentUser = supabaseClient.auth.currentUserOrNull()?.id
                if (currentUser != null) {
                    // Load patient data for emergency contact
                    loadProfile(currentUser)

                    // Load health metrics
                    val metricsResult = healthMetricRepository.getHealthMetricsByType(currentUser,type)
                    metricsResult.onSuccess { metrics ->
                        _uiState.value = _uiState.value.copy(allMetrics = metrics)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun loadProfile(userId: String){
        viewModelScope.launch {
            val patientResult = patientRepository.getPatient(userId)
            patientResult.onSuccess { patient ->
                _uiState.value = _uiState.value.copy(patient = patient)
            }
        }
    }

    fun addHealthMetric(
        type: HealthMetricType,
        value: Double,
        unit: String,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val currentUser = supabaseClient.auth.currentUserOrNull() ?: return@launch

            val metric = HealthMetric(
                id = UUID.randomUUID().toString(),
                userId = currentUser.id,
                type = type,
                value = value,
                unit = unit,
                notes = notes,
                recordedAt = Date().time,
                createdAt = Date().time,
                updatedAt = Date().time
            )

            val result = healthMetricRepository.addHealthMetric(metric)
            if (result.isSuccess) {
                _effects.send(CareEvents.OnAddedNew)
            } else {
                _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refresh() {
        loadData()
    }
}

data class CareUiState(
    val isLoading: Boolean = false,
    val patient: Patient? = null,
    val healthMetrics: List<HealthMetricSummary> = emptyList(),
    val allMetrics : List<HealthMetric> = emptyList(),
    val error: String? = null,
    val isAddedNew: Boolean = false
)

sealed class CareEvents {
    object OnAddedNew : CareEvents()
}
