package com.zahid.dailydose.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zahid.dailydose.data.repository.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val currentPage: Int = 0,
    val isOnboardingComplete: Boolean = false,
    val isLoading: Boolean = false
)

class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()
    
    fun onNextClick() {
        val currentPage = _uiState.value.currentPage
        if (currentPage < 2) {
            _uiState.value = _uiState.value.copy(currentPage = currentPage + 1)
        }
    }
    
    fun onPreviousClick() {
        val currentPage = _uiState.value.currentPage
        if (currentPage > 0) {
            _uiState.value = _uiState.value.copy(currentPage = currentPage - 1)
        }
    }
    
    fun onCompleteClick() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                // Mark onboarding as complete
                onboardingRepository.setOnboardingCompleted(true)
                _uiState.value = _uiState.value.copy(
                    isOnboardingComplete = true,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                // Handle error if needed
            }
        }
    }
}
