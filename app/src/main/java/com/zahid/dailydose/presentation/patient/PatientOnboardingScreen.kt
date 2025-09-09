package com.zahid.dailydose.presentation.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun PatientOnboardingScreen(
    onNavigateToHome: () -> Unit,
    viewModel: PatientOnboardingViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val effects = viewModel.effects
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Handle effects
    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                is PatientOnboardingEffect.NavigateToHome -> onNavigateToHome()
                is PatientOnboardingEffect.ShowError -> {
                    // Error is already handled in UI state
                }
            }
        }
    }

    uiState.errorMessage?.let {
        scope.launch {
            snackbarHostState.showSnackbar(it.split("\n")[0])
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState)},
            ) {  contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp)
        ) {
            // Header
            Spacer(Modifier.height(20.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Tell us about yourself",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Step ${uiState.currentStep} of ${uiState.totalSteps}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress indicator
            LinearProgressIndicator(
            progress = { uiState.currentStep.toFloat() / uiState.totalSteps.toFloat() },
            modifier = Modifier.fillMaxWidth(),
            color = ProgressIndicatorDefaults.linearColor,
            trackColor = ProgressIndicatorDefaults.linearTrackColor,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Step content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                when (uiState.currentStep) {
                    1 -> PersonalInfoStep(
                        uiState = uiState,
                        onEvent = viewModel::handleEvent
                    )
                    2 -> MedicalInfoStep(
                        uiState = uiState,
                        onEvent = viewModel::handleEvent
                    )
                    3 -> EmergencyInfoStep(
                        uiState = uiState,
                        onEvent = viewModel::handleEvent
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (uiState.currentStep > 1) {
                    OutlinedButton(
                        onClick = { viewModel.handleEvent(PatientOnboardingEvent.PreviousStep) },
                        enabled = !uiState.isLoading
                    ) {
                        Text("Previous")
                    }
                } else {
                    Spacer(modifier = Modifier.width(0.dp))
                }

                if (uiState.currentStep < uiState.totalSteps) {
                    Button(
                        onClick = { viewModel.handleEvent(PatientOnboardingEvent.NextStep) },
                        enabled = !uiState.isLoading
                    ) {
                        Text("Next")
                    }
                } else {
                    Button(
                        onClick = { viewModel.handleEvent(PatientOnboardingEvent.CompleteOnboarding) },
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Complete")
                        }
                    }
                }
            }
        }
    }
    

}
