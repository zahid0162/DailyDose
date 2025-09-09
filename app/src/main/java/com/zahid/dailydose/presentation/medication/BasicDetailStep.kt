package com.zahid.dailydose.presentation.medication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zahid.dailydose.domain.model.MedicationCategory
import com.zahid.dailydose.domain.model.MedicationForm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicDetailsStep(viewModel: AddMedicationViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Basic Details",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = uiState.medicationName,
            onValueChange = viewModel::updateMedicationName,
            label = { Text("Medication Name") },
            placeholder = { Text("e.g., Metformin, Vitamin D") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.medicationNameError != null
        )
        uiState.medicationNameError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Form dropdown
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = uiState.form?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Form") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                MedicationForm.entries.forEach { form ->
                    DropdownMenuItem(
                        text = { Text(form.name) },
                        onClick = {
                            viewModel.updateForm(form)
                            expanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = uiState.strength,
            onValueChange = viewModel::updateStrength,
            label = { Text("Strength/Dosage") },
            placeholder = { Text("e.g., 500mg, 10ml") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.strengthError != null
        )
        uiState.strengthError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Category dropdown
        var categoryExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded }
        ) {
            OutlinedTextField(
                value = uiState.category?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Category (Optional)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("None") },
                    onClick = {
                        viewModel.updateCategory(null)
                        categoryExpanded = false
                    }
                )
                MedicationCategory.entries.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name.replace("_", " ")) },
                        onClick = {
                            viewModel.updateCategory(category)
                            categoryExpanded = false
                        }
                    )
                }
            }
        }
    }
}