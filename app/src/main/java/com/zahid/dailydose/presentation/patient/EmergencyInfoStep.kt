package com.zahid.dailydose.presentation.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zahid.dailydose.domain.model.BloodGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyInfoStep(
    uiState: PatientOnboardingUiState,
    onEvent: (PatientOnboardingEvent) -> Unit
) {
    var showBloodGroupMenu by remember { mutableStateOf(false) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Emergency Information",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        
        Text(
            text = "This information is critical in emergency situations and will be easily accessible to healthcare providers.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Emergency Contact
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Emergency,
                        contentDescription = "Emergency Contact",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Emergency Contact",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                OutlinedTextField(
                    value = uiState.emergencyContactName,
                    onValueChange = { onEvent(PatientOnboardingEvent.EmergencyContactNameChanged(it)) },
                    label = { Text("Contact Name") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = "Name")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = uiState.emergencyContactPhone,
                    onValueChange = { onEvent(PatientOnboardingEvent.EmergencyContactPhoneChanged(it)) },
                    label = { Text("Phone Number") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = "Phone")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
        
        // Blood Group
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Bloodtype,
                        contentDescription = "Blood Group",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Blood Group",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                ExposedDropdownMenuBox(
                    expanded = showBloodGroupMenu,
                    onExpandedChange = { showBloodGroupMenu = !showBloodGroupMenu }
                ) {
                    OutlinedTextField(
                        value = uiState.bloodGroup?.name?.replace("_", " ") ?: "",
                        onValueChange = { },
                        label = { Text("Blood Group") },
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBloodGroupMenu)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showBloodGroupMenu,
                        onDismissRequest = { showBloodGroupMenu = false }
                    ) {
                        BloodGroup.values().forEach { bloodGroup ->
                            DropdownMenuItem(
                                text = { Text(bloodGroup.name.replace("_", " ")) },
                                onClick = {
                                    onEvent(PatientOnboardingEvent.BloodGroupChanged(bloodGroup))
                                    showBloodGroupMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Important Note
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Important Note",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "This emergency information will be displayed prominently in your profile and can be quickly accessed by healthcare providers in case of emergencies. Please ensure all information is accurate and up-to-date.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
