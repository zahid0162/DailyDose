package com.zahid.dailydose.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToPatientOnboarding: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val effects = viewModel.effects
    
    // Handle effects
    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                is RegisterEffect.NavigateToLogin -> onNavigateToLogin()
                is RegisterEffect.NavigateToPatientOnboarding -> onNavigateToPatientOnboarding()
                is RegisterEffect.ShowError -> {
                    // Error is already handled in UI state
                }
            }
        }
    }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Header Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Create Your DailyDose Account",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Stay on top of your health with smart reminders and care tracking.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Form Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Email Field
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.handleEvent(RegisterEvent.EmailChanged(it)) },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = "Email")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.errorMessage != null,
                shape = MaterialTheme.shapes.medium
            )
            
            // Password Field
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.handleEvent(RegisterEvent.PasswordChanged(it)) },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Password")
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.errorMessage != null,
                shape = MaterialTheme.shapes.medium
            )
            
            // Confirm Password Field
            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = { viewModel.handleEvent(RegisterEvent.ConfirmPasswordChanged(it)) },
                label = { Text("Confirm Password") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Confirm Password")
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.errorMessage != null,
                shape = MaterialTheme.shapes.medium
            )
            
            // Error Message
            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Register Button
            Button(
                onClick = {
                    viewModel.handleEvent(RegisterEvent.RegisterClicked)

                          },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Account")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Login Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(
                    onClick = { viewModel.handleEvent(RegisterEvent.NavigateToLogin) }
                ) {
                    Text("Sign In")
                }
            }
        }
    }
}
