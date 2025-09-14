package com.zahid.dailydose

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.zahid.dailydose.data.supabase.SupabaseClient
import com.zahid.dailydose.navigation.DailyDoseNavigation
import com.zahid.dailydose.navigation.Screen
import com.zahid.dailydose.presentation.medication.NotificationPermissionHandler
import com.zahid.dailydose.ui.theme.DailyDoseTheme
import io.github.jan.supabase.auth.handleDeeplinks
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            DailyDoseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DailyDoseApp()
                }
            }
        }
    }
}

@Composable
fun DailyDoseApp() {
    val navController = rememberNavController()
    DailyDoseNavigation(
        navController = navController,
        startDestination = Screen.Splash.route,
    )
}