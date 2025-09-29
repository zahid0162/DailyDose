package com.zahid.dailydose.presentation.care

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.zahid.dailydose.domain.model.HealthMetricType
import com.zahid.dailydose.presentation.auth.LoginEffect
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHealthMetricScreen(
    metricType: HealthMetricType,
    onNavigateBack: () -> Unit,
    onAddedNew: () -> Unit,
    viewModel: CareViewModel = koinViewModel()
) {
    var value by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CareEvents.OnAddedNew -> {
                    onAddedNew()
                }
                 else -> {}
            }
        }
    }

    val (title, unit, placeholder) = when (metricType) {
        HealthMetricType.BLOOD_PRESSURE_SYSTOLIC -> Triple("Blood Pressure (Systolic)", "mmHg", "120")
        HealthMetricType.BLOOD_PRESSURE_DIASTOLIC -> Triple("Blood Pressure (Diastolic)", "mmHg", "80")
        HealthMetricType.HEART_RATE -> Triple("Heart Rate", "bpm", "72")
        HealthMetricType.WEIGHT -> Triple("Weight", "kg", "70.5")
        HealthMetricType.TEMPERATURE -> Triple("Temperature", "°C", "36.5")
        HealthMetricType.DIABETES -> Triple("Diabetes", "mg/dl", "77.5")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add $title", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = {
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text("Value") },
                        placeholder = { Text(placeholder) },
                        suffix = { Text(unit) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        placeholder = { Text("Add any notes...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Button(
                        onClick = {
                            val numericValue = value.toDoubleOrNull()
                            if (numericValue != null) {
                                viewModel.addHealthMetric(metricType, numericValue, unit, notes.takeIf { it.isNotBlank() })
                                // Don't navigate back immediately - let the ViewModel handle success/error
                            }
                        },
                        enabled = value.isNotBlank() && value.toDoubleOrNull() != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Reading")
                    }
                }
            }

            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
