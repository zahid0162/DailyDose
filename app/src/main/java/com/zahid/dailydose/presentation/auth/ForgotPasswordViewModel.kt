package com.zahid.dailydose.presentation.auth


import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zahid.dailydose.domain.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForgotUiState(
    val email: String = "zaidmuneer25@gmail.com",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmailSuccessful: Boolean = false
)

sealed class ForgotEvent {
    data class EmailChanged(val email: String) : ForgotEvent()
    object SendClicked : ForgotEvent()
}

sealed class ForgotEffect {
    object NavigateBack: ForgotEffect()
    data class ShowMessage(val msg: String): ForgotEffect()
}

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotUiState())
    val uiState: StateFlow<ForgotUiState> = _uiState.asStateFlow()

    private val _effects = Channel<ForgotEffect>()
    val effects = _effects.receiveAsFlow()

    fun handleEvent(event: ForgotEvent) {
        when (event) {
            is ForgotEvent.EmailChanged -> {
                _uiState.update {
                    it.copy(
                        email = event.email.trim()
                    )
                }
            }
            ForgotEvent.SendClicked -> {
                sendLink()
            }
        }
    }

    private fun sendLink(){
        if (!isValidEmail()){
            viewModelScope.launch {
                _effects.send(ForgotEffect.ShowMessage("Please enter a valid email"))
            }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true
                )
            }
            val result = authRepository.resetPassword(_uiState.value.email)
            if (result.isSuccess){
                _effects.send(ForgotEffect.ShowMessage("Email sent successfully!"))
                _uiState.update {
                    it.copy(
                        isLoading = false
                    )
                }
            }
            else{
                _effects.send(ForgotEffect.ShowMessage("An error occurred, Please try again!"))
                _uiState.update {
                    it.copy(
                        isLoading = false
                    )
                }
            }
            _effects.send(ForgotEffect.NavigateBack)
        }

    }

    fun isValidEmail(): Boolean {
        return _uiState.value.email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(_uiState.value.email).matches()
    }
}
