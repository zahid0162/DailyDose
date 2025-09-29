package com.zahid.dailydose.presentation.medication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zahid.dailydose.presentation.home.ErrorState
import org.koin.androidx.compose.koinViewModel

@Composable
fun MedicationScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateToAddMedication: () -> Unit = {},
    onNavigateToViewMedication: (String) -> Unit = {},
    onNavigateToEditMedication: (String) -> Unit = {},
    viewModel: MedicationViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.error != null -> {
            ErrorState(
                error = uiState.error!!,
                onRetry = { viewModel.refreshMedications() }
            )
        }

        uiState.medications.isEmpty() -> {
            EmptyMedicationsListState(
                onAddMedication = onNavigateToAddMedication
            )
        }

        else -> {
            MedicationsListContent(
                uiState = uiState,
                onNavigateToAddMedication = onNavigateToAddMedication,
                onNavigateToViewMedication = onNavigateToViewMedication,
                onNavigateToEditMedication = onNavigateToEditMedication,
                onDeleteMedication = { medicationId ->
                    viewModel.deleteMedication(medicationId)
                }
            )
        }
    }
}

@Composable
fun MedicationsListContent(
    uiState: MedicationUiState,
    onNavigateToAddMedication: () -> Unit,
    onNavigateToViewMedication: (String) -> Unit,
    onNavigateToEditMedication: (String) -> Unit,
    onDeleteMedication: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Active Medications
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Medications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${uiState.medications.count { it.isActive }} medications",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(uiState.medications.filter { it.isActive }) { medication ->
            MedicationListItemCard(
                medication = medication,
                onView = { onNavigateToViewMedication(medication.id ?: "") },
                onEdit = { onNavigateToEditMedication(medication.id ?: "") },
                onDelete = { onDeleteMedication(medication.id ?: "") },
                onToggleActive = { /* TODO: Implement toggle active */ }
            )
            HorizontalDivider()
        }

        if (uiState.medications.any { !it.isActive }) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Inactive Medications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.medications.count { !it.isActive }} medications",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(uiState.medications.filter { !it.isActive }) { medication ->
                MedicationListItemCard(
                    medication = medication,
                    onView = { onNavigateToViewMedication(medication.id ?: "") },
                    onEdit = { onNavigateToEditMedication(medication.id ?: "") },
                    onDelete = { onDeleteMedication(medication.id ?: "") },
                    onToggleActive = { /* TODO: Implement toggle active */ }
                )
            }
        }
    }
}

@Composable
fun EmptyMedicationsListState(
    onAddMedication: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Medication,
            contentDescription = "No medications",
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Medications Added",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start managing your medications by adding your first one",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

    }
}

@Composable
fun MedicationListItemCard(
    medication: com.zahid.dailydose.domain.model.Medication,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    var showDropdownMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (medication.isActive)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Medication,
                        contentDescription = "Medication",
                        tint = if (medication.isActive)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = medication.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${medication.strength} ${medication.form.name.lowercase()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${medication.timesPerDay}x daily",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box {
                        IconButton(
                            onClick = { showDropdownMenu = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showDropdownMenu,
                            onDismissRequest = { showDropdownMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("View") },
                                onClick = {
                                    showDropdownMenu = false
                                    onView()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Visibility, contentDescription = "View")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    showDropdownMenu = false
                                    onEdit()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    showDropdownMenu = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            )
                        }
                    }
                }
            }

            if (medication.isActive && medication.specificTimes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Schedule",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Times: ${medication.specificTimes.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (medication.category != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "Category",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = medication.category.name.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Medication") },
            text = { Text("Are you sure you want to delete \"${medication.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
