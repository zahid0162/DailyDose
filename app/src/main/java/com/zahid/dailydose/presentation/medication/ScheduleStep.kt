package com.zahid.dailydose.presentation.medication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zahid.dailydose.domain.model.MealTiming
import com.zahid.dailydose.presentation.DatePickerDialog
import com.zahid.dailydose.presentation.TimePickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleStep(viewModel: AddMedicationViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showTimePickers by remember { mutableStateOf(false) }
    var currentTimeIndex by remember { mutableIntStateOf(0) }

    if (showTimePickers) {
        TimePickerDialog(
            initialTime = uiState.specificTimes[currentTimeIndex],
            onTimeSelected = { selectedTime ->
                viewModel.updateSpecificTime(currentTimeIndex, selectedTime)
                showTimePickers = false
            },
            onDismiss = {
                showTimePickers = false

            }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Schedule & Frequency",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Start Date
        OutlinedTextField(
            value = uiState.startDate,
            onValueChange = {},
            label = { Text("Start Date") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showStartDatePicker = true }) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Select start date")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (showStartDatePicker) {
            DatePickerDialog(
                onDateSelected = { date ->
                    viewModel.updateStartDate(date)
                    showStartDatePicker = false
                },
                onDismiss = { showStartDatePicker = false }
            )
        }

        // End Date or Ongoing
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = uiState.isOngoing,
                onCheckedChange = viewModel::updateIsOngoing
            )
            Text("Ongoing medication")
        }

        if (!uiState.isOngoing) {
            OutlinedTextField(
                value = uiState.endDate,
                onValueChange = {},
                label = { Text("End Date") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showEndDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Select end date")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (showEndDatePicker) {
                DatePickerDialog(
                    onDateSelected = { date ->
                        viewModel.updateEndDate(date)
                        showEndDatePicker = false
                    },
                    onDismiss = { showEndDatePicker = false }
                )
            }
        }

        // Times per day
        Text(
            text = "Times per Day",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(1, 2, 3, 4).forEach { times ->
                FilterChip(
                    onClick = { viewModel.updateTimesPerDay(times) },
                    label = { Text("${times}x") },
                    selected = uiState.timesPerDay == times
                )
            }
        }

        // Specific times
        Text(
            text = "Specific Times",
            style = MaterialTheme.typography.titleMedium
        )

        uiState.specificTimes.forEachIndexed { index, time ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = time,
                    onValueChange = {},
                    label = { Text("Time ${index + 1}") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            currentTimeIndex = index
                            showTimePickers = true
                        }) {
                            Icon(Icons.Default.Schedule, contentDescription = "Select time")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                if (uiState.specificTimes.size > 1) {
                    IconButton(onClick = { viewModel.removeSpecificTime(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove time")
                    }
                }
            }
        }

        if (uiState.specificTimes.size < 4) {
            OutlinedButton(
                onClick = { viewModel.addSpecificTime() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Time")
            }
        }

        // Meal timing
        var mealExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = mealExpanded,
            onExpandedChange = { mealExpanded = !mealExpanded }
        ) {
            OutlinedTextField(
                value = uiState.mealTiming?.name?.replace("_", " ") ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Meal Timing (Optional)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mealExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = mealExpanded,
                onDismissRequest = { mealExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("None") },
                    onClick = {
                        viewModel.updateMealTiming(null)
                        mealExpanded = false
                    }
                )
                MealTiming.entries.forEach { timing ->
                    DropdownMenuItem(
                        text = { Text(timing.name.replace("_", " ")) },
                        onClick = {
                            viewModel.updateMealTiming(timing)
                            mealExpanded = false
                        }
                    )
                }
            }
        }
    }
}