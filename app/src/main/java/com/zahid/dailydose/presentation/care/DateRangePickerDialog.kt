package com.zahid.dailydose.presentation.care

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    onDismiss: () -> Unit,
    onDateRangeSelected: (startDate: Date, endDate: Date) -> Unit
) {
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Date Range for Export",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Choose the start and end dates for the health metrics you want to include in the PDF export.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Start Date
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showStartDatePicker = true }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Start Date",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = startDate?.let { dateFormat.format(it) } ?: "Select start date",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                // End Date
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showEndDatePicker = true }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "End Date",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = endDate?.let { dateFormat.format(it) } ?: "Select end date",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                // Validation message
                if (startDate != null && endDate != null && startDate!!.after(endDate)) {
                    Text(
                        text = "Start date must be before end date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            if (startDate != null && endDate != null && !startDate!!.after(endDate)) {
                                onDateRangeSelected(startDate!!, endDate!!)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = startDate != null && endDate != null && !startDate!!.after(endDate)
                    ) {
                        Text("Export")
                    }
                }
            }
        }
    }
    
    // Start Date Picker
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate?.time ?: System.currentTimeMillis()
        )
        
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            startDate = Date(millis)
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // End Date Picker
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate?.time ?: System.currentTimeMillis()
        )
        
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            endDate = Date(millis)
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
