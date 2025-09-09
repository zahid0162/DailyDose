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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zahid.dailydose.domain.model.Medication
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewMedicationScreen(
    medicationId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    viewModel: ViewMedicationViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(medicationId) {
        viewModel.loadMedication(medicationId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Medication Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            uiState.medication != null -> {
                MedicationDetailsContent(
                    medication = uiState.medication!!,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun MedicationDetailsContent(
    medication: Medication,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Basic Information Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Basic Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    DetailRow(
                        icon = Icons.Default.Medication,
                        label = "Name",
                        value = medication.name
                    )
                    
                    DetailRow(
                        icon = Icons.Default.Science,
                        label = "Form",
                        value = medication.form.name.lowercase().replaceFirstChar { it.uppercase() }
                    )
                    
                    DetailRow(
                        icon = Icons.Default.Scale,
                        label = "Strength",
                        value = medication.strength
                    )
                    
                    DetailRow(
                        icon = Icons.Default.Liquor,
                        label = "Dosage",
                        value = medication.dosage
                    )
                    
                    medication.category?.let { category ->
                        DetailRow(
                            icon = Icons.Default.Category,
                            label = "Category",
                            value = category.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                        )
                    }
                }
            }
        }
        
        // Schedule Information Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    DetailRow(
                        icon = Icons.Default.Schedule,
                        label = "Times per day",
                        value = "${medication.timesPerDay}x daily"
                    )
                    
                    if (medication.specificTimes.isNotEmpty()) {
                        DetailRow(
                            icon = Icons.Default.AccessTime,
                            label = "Specific times",
                            value = medication.specificTimes.joinToString(", ")
                        )
                    }
                    
                    medication.mealTiming?.let { timing ->
                        DetailRow(
                            icon = Icons.Default.Restaurant,
                            label = "Meal timing",
                            value = timing.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                        )
                    }
                    
                    DetailRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Start date",
                        value = dateFormat.format(medication.startDate)
                    )
                    
                    if (medication.endDate != null) {
                        DetailRow(
                            icon = Icons.Default.Event,
                            label = "End date",
                            value = dateFormat.format(medication.endDate)
                        )
                    } else if (medication.isOngoing) {
                        DetailRow(
                            icon = Icons.Default.Event,
                            label = "Duration",
                            value = "Ongoing"
                        )
                    }
                }
            }
        }
        
        // Reminder Information Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Reminders",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    DetailRow(
                        icon = Icons.Default.Notifications,
                        label = "Reminders enabled",
                        value = if (medication.remindersEnabled) "Yes" else "No"
                    )
                    
                    if (medication.remindersEnabled) {
                        DetailRow(
                            icon = Icons.Default.VolumeUp,
                            label = "Reminder type",
                            value = medication.reminderType.name.lowercase().replaceFirstChar { it.uppercase() }
                        )
                    }
                }
            }
        }
        
        // Doctor Information Card
        if (medication.prescribedBy != null || medication.prescriptionFileUrl != null || medication.refillCount != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Doctor Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        medication.prescribedBy?.let { doctor ->
                            DetailRow(
                                icon = Icons.Default.Person,
                                label = "Prescribed by",
                                value = doctor
                            )
                        }
                        
                        medication.refillCount?.let { count ->
                            DetailRow(
                                icon = Icons.Default.Refresh,
                                label = "Refill count",
                                value = count.toString()
                            )
                        }
                        
                        medication.prescriptionFileUrl?.let { url ->
                            DetailRow(
                                icon = Icons.Default.AttachFile,
                                label = "Prescription file",
                                value = "Available"
                            )
                        }
                    }
                }
            }
        }
        
        // Notes Card
        if (medication.notes != null && medication.notes.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = medication.notes,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        // Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (medication.isActive) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (medication.isActive) Icons.Default.CheckCircle else Icons.Default.PauseCircle,
                            contentDescription = "Status",
                            tint = if (medication.isActive) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (medication.isActive) "Active" else "Inactive",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (medication.isActive) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
