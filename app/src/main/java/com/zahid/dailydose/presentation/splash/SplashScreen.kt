package com.zahid.dailydose.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zahid.dailydose.R
import com.zahid.dailydose.ui.theme.SplashBg
import org.koin.androidx.compose.koinViewModel

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToPatientProfile: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Handle navigation based on splash state
    LaunchedEffect(uiState.destination) {
        when (uiState.destination) {
            SplashDestination.Onboarding -> onNavigateToOnboarding()
            SplashDestination.Login -> onNavigateToLogin()
            SplashDestination.PatientProfile -> onNavigateToPatientProfile()
            SplashDestination.Home -> onNavigateToHome()
            SplashDestination.Loading -> { /* Stay on splash */ }
        }
    }
    
    // Splash Screen UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo/Icon
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "DailyDose Logo",
                modifier = Modifier.size(150.dp)
            )

            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Loading Indicator
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
