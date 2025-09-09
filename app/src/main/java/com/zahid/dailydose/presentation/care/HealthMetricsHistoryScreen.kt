package com.zahid.dailydose.presentation.care

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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



    val title = when (metricType) {
        HealthMetricType.BLOOD_PRESSURE_SYSTOLIC -> "Blood Pressure (Systolic) History"
        HealthMetricType.BLOOD_PRESSURE_DIASTOLIC -> "Blood Pressure (Diastolic) History"
        HealthMetricType.HEART_RATE -> "Heart Rate History"
        HealthMetricType.WEIGHT -> "Weight History"
        HealthMetricType.TEMPERATURE -> "Temperature History"
        HealthMetricType.DIABETES -> "Diabetes History"
    }

    LaunchedEffect(true) {
        viewModel.loadAllData(type = metricType)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
