package com.zahid.dailydose.presentation.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalInfoStep(
    uiState: PatientOnboardingUiState,
    onEvent: (PatientOnboardingEvent) -> Unit
) {
    var newCondition by remember { mutableStateOf("") }
    var newAllergy by remember { mutableStateOf("") }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Medical Essentials",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        
        Text(
            text = "This information helps us provide better medication management and safety alerts.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Medical Conditions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.MedicalServices,
                        contentDescription = "Medical Conditions",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Medical Conditions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // Add new condition
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newCondition,
                        onValueChange = { newCondition = it },
                        label = { Text("Add condition (e.g., Diabetes, Hypertension)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    IconButton(
                        onClick = {
                            if (newCondition.isNotBlank()) {
                                onEvent(PatientOnboardingEvent.MedicalConditionAdded(newCondition.trim()))
                                newCondition = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add condition")
                    }
                }
                
                // Display conditions
                if (uiState.medicalConditions.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.medicalConditions) { condition ->
                            AssistChip(
                                onClick = { onEvent(PatientOnboardingEvent.MedicalConditionRemoved(condition)) },
                                label = { Text(condition) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Allergies
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.MedicalServices,
                        contentDescription = "Allergies",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Allergies (especially drug allergies)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // Add new allergy
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newAllergy,
                        onValueChange = { newAllergy = it },
                        label = { Text("Add allergy (e.g., Penicillin, Latex)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    IconButton(
                        onClick = {
                            if (newAllergy.isNotBlank()) {
                                onEvent(PatientOnboardingEvent.AllergyAdded(newAllergy.trim()))
                                newAllergy = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add allergy")
                    }
                }
                
                // Display allergies
                if (uiState.allergies.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.allergies) { allergy ->
                            AssistChip(
                                onClick = { onEvent(PatientOnboardingEvent.AllergyRemoved(allergy)) },
                                label = { Text(allergy) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Primary Doctor Information
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Primary Doctor",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Primary Doctor (Optional)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                OutlinedTextField(
                    value = uiState.primaryDoctorName,
                    onValueChange = { onEvent(PatientOnboardingEvent.PrimaryDoctorNameChanged(it)) },
                    label = { Text("Doctor's Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = uiState.primaryDoctorContact,
                    onValueChange = { onEvent(PatientOnboardingEvent.PrimaryDoctorContactChanged(it)) },
                    label = { Text("Contact Information") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
        
        Text(
            text = "Note: You can add current medications later in the app.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
