package com.zahid.dailydose.presentation.care

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zahid.dailydose.domain.model.HealthMetric
import com.zahid.dailydose.domain.model.HealthMetricType
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthMetricsHistoryScreen(
    metricType: HealthMetricType,
    onNavigateBack: () -> Unit,
    viewModel: CareViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDateRangePicker by remember { mutableStateOf(false) }

    val title = when (metricType) {
        HealthMetricType.BLOOD_PRESSURE_SYSTOLIC -> "Blood Pressure (Systolic) History"
        HealthMetricType.BLOOD_PRESSURE_DIASTOLIC -> "Blood Pressure (Diastolic) History"
        HealthMetricType.HEART_RATE -> "Heart Rate History"
        HealthMetricType.WEIGHT -> "Weight History"
        HealthMetricType.TEMPERATURE -> "Temperature History"
        HealthMetricType.DIABETES -> "Diabetes History"
    }

    // Handle PDF export events
    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collect { event ->
            when (event) {
                is CareEvents.OnPdfExported -> {
                    Toast.makeText(context, "PDF exported successfully to Downloads folder", Toast.LENGTH_LONG).show()
                    
                    // Open the PDF file
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            event.file
                        )
                        setDataAndType(uri, "application/pdf")
                        flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_SHORT).show()
                    }
                }
                is CareEvents.OnExportError -> {
                    Toast.makeText(context, "Export failed: ${event.message}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(true) {
        viewModel.loadAllData(type = metricType)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!uiState.allMetrics.isEmpty()) {
                        IconButton(
                            onClick = { showDateRangePicker = true }
                        ) {
                            Icon(
                                Icons.Default.FileDownload, 
                                contentDescription = "Export to PDF"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.allMetrics.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues).padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "No readings found",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Start logging your health metrics to see your history here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.allMetrics) { metric ->
                    HealthMetricHistoryCard(metric = metric)
                }
            }
        }
    }
    
    // Date Range Picker Dialog
    if (showDateRangePicker) {
        DateRangePickerDialog(
            onDismiss = { showDateRangePicker = false },
            onDateRangeSelected = { startDate, endDate ->
                viewModel.exportToPdf(context, metricType, startDate, endDate)
            }
        )
    }
}

@Composable
fun HealthMetricHistoryCard(metric: HealthMetric) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${metric.value} ${metric.unit}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFormat.format(Date(metric.recordedAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                metric.notes?.let { notes ->
                    if (notes.isNotBlank()) {
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
