package com.zahid.dailydose.presentation.patient

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zahid.dailydose.domain.model.Gender
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoStep(
    uiState: PatientOnboardingUiState,
    onEvent: (PatientOnboardingEvent) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showGenderMenu by remember { mutableStateOf(false) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Basic Personal Information",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        
        Text(
            text = "This information helps us provide age-specific reminders and dose adjustments.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Full Name
        OutlinedTextField(
            value = uiState.fullName,
            onValueChange = { onEvent(PatientOnboardingEvent.FullNameChanged(it)) },
            label = { Text("Full Name") },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = "Name")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        // Date of Birth
        OutlinedTextField(

            value = uiState.dateOfBirth?.let { 
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it) 
            } ?: "",
            onValueChange = { },
            label = { Text("Date of Birth") },
            leadingIcon = {
                Icon(Icons.Default.CalendarToday, contentDescription = "Date of Birth", Modifier.clickable{
                    showDatePicker = true
                })
            },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
        )
        
        // Gender
        ExposedDropdownMenuBox(
            expanded = showGenderMenu,
            onExpandedChange = { showGenderMenu = !showGenderMenu }
        ) {
            OutlinedTextField(
                value = uiState.gender?.name ?: "",
                onValueChange = { },
                label = { Text("Gender") },
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showGenderMenu)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = showGenderMenu,
                onDismissRequest = { showGenderMenu = false }
            ) {
                Gender.entries.forEach { gender ->
                    DropdownMenuItem(
                        text = { Text(gender.name) },
                        onClick = {
                            onEvent(PatientOnboardingEvent.GenderChanged(gender))
                            showGenderMenu = false
                        }
                    )
                }
            }
        }
        
        // Contact Number (Optional)
        OutlinedTextField(
            value = uiState.contactNumber,
            onValueChange = { onEvent(PatientOnboardingEvent.ContactNumberChanged(it)) },
            label = { Text("Contact Number (Optional)") },
            leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = "Phone")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Text(
            text = "Contact number is useful for emergencies and caregiver linking.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    
    // Date Picker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dateOfBirth?.time ?: System.currentTimeMillis()
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onEvent(PatientOnboardingEvent.DateOfBirthChanged(Date(millis)))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
