package com.zahid.dailydose.presentation.medication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zahid.dailydose.domain.model.*
import org.koin.androidx.compose.koinViewModel
import java.util.Date
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.zahid.dailydose.utils.getFileName
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    onNavigateBack: () -> Unit,
    onMedicationAdded: () -> Unit,
    medicationId: String? = null,
    viewModel: AddMedicationViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Load medication for editing if medicationId is provided
    LaunchedEffect(medicationId) {
        medicationId?.let { id ->
            viewModel.loadMedicationForEdit(id)
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState)},
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) "Edit Medication" else "Add Medication",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton =
            {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.currentStep > 1) {
                        OutlinedButton(
                            onClick = { viewModel.previousStep() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Previous")
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Button(
                        onClick = {
                            if (uiState.currentStep < 5) {
                                viewModel.nextStep()
                            } else {
                                viewModel.saveMedication()
                            }
                        },
                        enabled = uiState.canProceed,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (uiState.currentStep < 5) "Next" else if (uiState.isEditMode) "Update" else "Save")
                    }
                }
            },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = 50.dp)
        ) {
            // Progress indicator
            LinearProgressIndicator(
            progress = { uiState.currentStep / 5f },
            modifier = Modifier.fillMaxWidth(),
            color = ProgressIndicatorDefaults.linearColor,
            trackColor = ProgressIndicatorDefaults.linearTrackColor,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
            
            // Step indicator
            Text(
                text = "Step ${uiState.currentStep} of 5",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp)
            )
            
            // Form content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (uiState.currentStep) {
                    1 -> {
                        item { BasicDetailsStep(viewModel = viewModel) }
                    }
                    2 -> {
                        item { ScheduleStep(viewModel = viewModel) }
                    }
                    3 -> {
                        item { RemindersStep(viewModel = viewModel) }
                    }
                    4 -> {
                        item { DoctorStep(viewModel = viewModel) }
                    }
                    5 -> {
                        item { NotesStep(viewModel = viewModel) }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // Navigation buttons

        }
    }
    
    // Handle navigation after successful save
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    if (uiState.isEditMode) "Medication updated successfully!" 
                    else "Medication saved successfully!"
                )
            }
            onMedicationAdded()
        }
    }
}
