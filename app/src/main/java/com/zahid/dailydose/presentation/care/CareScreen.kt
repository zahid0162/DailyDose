package com.zahid.dailydose.presentation.care

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zahid.dailydose.domain.model.HealthMetricType
import org.koin.androidx.compose.koinViewModel
import androidx.core.net.toUri

@Composable
fun CareScreen(
    onNavigateToAddMetric: (HealthMetricType) -> Unit = {},
    onNavigateToMetricHistory: (HealthMetricType) -> Unit = {},
    viewModel: CareViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current


    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Emergency Contact Card
                uiState.patient?.let { patient ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Emergency,
                                    contentDescription = "Emergency",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Emergency Contact",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${patient.emergencyContactName} - ${patient.emergencyContactPhone}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = "tel:${patient.emergencyContactPhone}".toUri()
                                    }
                                    context.startActivity(intent)
                                }
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Call Now")
                            }
                        }
                    }
                }
            }

            item {
                // Health Metrics
                Text(
                    text = "Health Metrics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Blood Pressure Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val systolicMetric = uiState.healthMetrics.find { it.type == HealthMetricType.BLOOD_PRESSURE_SYSTOLIC }
                    HealthMetricCard(
                        title = "Blood Pressure Systolic",
                        value = if (systolicMetric != null) {
                            "${systolicMetric.latestValue.toInt()}"
                        } else {
                            "No data"
                        },
                        unit = "mmHg",
                        icon = Icons.Default.MonitorHeart,
                        modifier = Modifier.weight(1f),
                        onAddClick = { onNavigateToAddMetric(HealthMetricType.BLOOD_PRESSURE_SYSTOLIC) },
                        onViewAllClick = { onNavigateToMetricHistory(HealthMetricType.BLOOD_PRESSURE_SYSTOLIC) }
                    )

                    val diastolicMetric = uiState.healthMetrics.find { it.type == HealthMetricType.BLOOD_PRESSURE_DIASTOLIC }
                    
                    HealthMetricCard(
                        title = "Blood Pressure Diastolic",
                        value = if (diastolicMetric != null) {
                            "${diastolicMetric.latestValue.toInt()}"
                        } else {
                            "No data"
                        },
                        unit = "mmHg",
                        icon = Icons.Default.MonitorHeart,
                        modifier = Modifier.weight(1f),
                        onAddClick = { onNavigateToAddMetric(HealthMetricType.BLOOD_PRESSURE_DIASTOLIC) },
                        onViewAllClick = { onNavigateToMetricHistory(HealthMetricType.BLOOD_PRESSURE_DIASTOLIC) }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    val diabetesMetric = uiState.healthMetrics.find { it.type == HealthMetricType.DIABETES }
                    HealthMetricCard(
                        title = "Diabetes",
                        value = if (diabetesMetric != null) {
                            "${diabetesMetric.latestValue.toInt()}"
                        } else {
                            "No data"
                        },
                        unit = "mg/dl",
                        icon = Icons.Default.SafetyCheck,
                        modifier = Modifier.weight(1f),
                        onAddClick = { onNavigateToAddMetric(HealthMetricType.DIABETES) },
                        onViewAllClick = { onNavigateToMetricHistory(HealthMetricType.DIABETES) }
                    )

                    val heartRateMetric = uiState.healthMetrics.find { it.type == HealthMetricType.HEART_RATE }
                    HealthMetricCard(
                        title = "Heart Rate",
                        value = heartRateMetric?.latestValue?.toInt()?.toString() ?: "No data",
                        unit = "bpm",
                        icon = Icons.Default.Favorite,
                        modifier = Modifier.weight(1f),
                        onAddClick = { onNavigateToAddMetric(HealthMetricType.HEART_RATE) },
                        onViewAllClick = { onNavigateToMetricHistory(HealthMetricType.HEART_RATE) }
                    )
                }
            }

            // Weight and Temperature Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val weightMetric = uiState.healthMetrics.find { it.type == HealthMetricType.WEIGHT }
                    HealthMetricCard(
                        title = "Weight",
                        value = weightMetric?.latestValue?.toString() ?: "No data",
                        unit = "kg",
                        icon = Icons.Default.Scale,
                        modifier = Modifier.weight(1f),
                        onAddClick = { onNavigateToAddMetric(HealthMetricType.WEIGHT) },
                        onViewAllClick = { onNavigateToMetricHistory(HealthMetricType.WEIGHT) }
                    )
                    
                    val temperatureMetric = uiState.healthMetrics.find { it.type == HealthMetricType.TEMPERATURE }
                    HealthMetricCard(
                        title = "Temperature",
                        value = temperatureMetric?.latestValue?.toString() ?: "No data",
                        unit = "°C",
                        icon = Icons.Default.Thermostat,
                        modifier = Modifier.weight(1f),
                        onAddClick = { onNavigateToAddMetric(HealthMetricType.TEMPERATURE) },
                        onViewAllClick = { onNavigateToMetricHistory(HealthMetricType.TEMPERATURE) }
                    )
                }
            }

        }
    }
}


@Composable
fun HealthMetricCard(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit = {},
    onViewAllClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedButton(
                    onClick = onAddClick,
                ) {
                    Text("Add", style = MaterialTheme.typography.bodySmall)
                }
                OutlinedButton(
                    onClick = onViewAllClick,
                ) {
                    Text("View All", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

