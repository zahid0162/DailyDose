package com.zahid.dailydose.presentation.medication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zahid.dailydose.domain.model.ReminderType

@Composable
fun RemindersStep(viewModel: AddMedicationViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Reminders",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = uiState.remindersEnabled,
                onCheckedChange = viewModel::updateRemindersEnabled
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Enable Reminders")
        }

        if (uiState.remindersEnabled) {
            Text(
                text = "Reminder Type",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReminderType.entries.forEach { type ->
                    FilterChip(
                        onClick = { viewModel.updateReminderType(type) },
                        label = { Text(type.name) },
                        selected = uiState.reminderType == type
                    )
                }
            }
        }
    }
}