package com.zahid.dailydose.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.zahid.dailydose.presentation.auth.LoginScreen
import com.zahid.dailydose.presentation.auth.RegisterScreen
import com.zahid.dailydose.presentation.MainBottomNavigation
import com.zahid.dailydose.presentation.care.AddHealthMetricScreen
import com.zahid.dailydose.presentation.care.HealthMetricsHistoryScreen
import com.zahid.dailydose.presentation.home.HomeViewModel
import com.zahid.dailydose.presentation.medication.AddMedicationScreen
import com.zahid.dailydose.presentation.medication.MedicationViewModel
import com.zahid.dailydose.presentation.medication.ViewMedicationScreen
import com.zahid.dailydose.presentation.onboarding.OnboardingScreen
import com.zahid.dailydose.presentation.patient.PatientOnboardingScreen
import com.zahid.dailydose.presentation.splash.SplashScreen
import com.zahid.dailydose.domain.model.HealthMetricType
import com.zahid.dailydose.presentation.care.CareViewModel
import org.koin.androidx.compose.koinViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object PatientOnboarding : Screen("patient_onboarding")
    object Home : Screen("home")
    object AddMedication : Screen("add_medication")
    object ViewMedication : Screen("view_medication/{medicationId}") {
        fun createRoute(medicationId: String) = "view_medication/$medicationId"
    }
    object EditMedication : Screen("edit_medication/{medicationId}") {
        fun createRoute(medicationId: String) = "edit_medication/$medicationId"
    }
    object AddHealthMetric : Screen("add_health_metric/{metricType}") {
        fun createRoute(metricType: HealthMetricType) = "add_health_metric/${metricType.name}"
    }
    object HealthMetricHistory : Screen("health_metric_history/{metricType}") {
        fun createRoute(metricType: HealthMetricType) = "health_metric_history/${metricType.name}"
    }
    object Settings : Screen("settings")
    object EditPatient : Screen("edit_patient/{patientId}") {
        fun createRoute(medicationId: String) = "edit_patient/$medicationId"
    }
}

@Composable
fun DailyDoseNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
) {


    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToPatientProfile = {
                    navController.navigate(Screen.PatientOnboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    // Navigate back to splash to re-evaluate auth state
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToHome = {
                    // Navigate back to splash to re-evaluate auth state
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToPatientOnboarding = {
                    // Navigate back to splash to re-evaluate auth state
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToPatientOnboarding = {
                    // Navigate back to splash to re-evaluate auth state
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.PatientOnboarding.route) {
            PatientOnboardingScreen(
                onNavigateToHome = {
                    // Navigate back to splash to re-evaluate auth state
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(Screen.PatientOnboarding.route) { inclusive = true }
                    }
                },
                patientId = null
            )
        }
        
        composable(Screen.Home.route) { bk->
            val backStackEntry = remember(bk) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            val homeViewModel  = koinViewModel<HomeViewModel>(viewModelStoreOwner = backStackEntry)
            val medViewModel  = koinViewModel<MedicationViewModel>(viewModelStoreOwner = backStackEntry)
            val careViewModel  = koinViewModel<CareViewModel>(viewModelStoreOwner = backStackEntry)
            MainBottomNavigation(
                onNavigateToAddMedication = {
                    navController.navigate(Screen.AddMedication.route)
                },
                onNavigateToViewMedication = { medicationId ->
                    navController.navigate(Screen.ViewMedication.createRoute(medicationId))
                },
                onNavigateToEditMedication = { medicationId ->
                    navController.navigate(Screen.EditMedication.createRoute(medicationId))
                },
                onNavigateToAddHealthMetric = { metricType ->
                    navController.navigate(Screen.AddHealthMetric.createRoute(metricType))
                },
                onNavigateToHealthMetricHistory = { metricType ->
                    navController.navigate(Screen.HealthMetricHistory.createRoute(metricType))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToEditPatient = { patientId->
                    navController.navigate(Screen.EditPatient.createRoute(patientId))
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                homeViewModel,
                medViewModel,
                careViewModel = careViewModel
            )
        }
        
        composable(Screen.AddMedication.route) { bk ->
            val backStackEntry = remember(bk) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            val homeViewModel : HomeViewModel = koinViewModel<HomeViewModel>(viewModelStoreOwner = backStackEntry)
            val medViewModel  = koinViewModel<MedicationViewModel>(viewModelStoreOwner = backStackEntry)
            AddMedicationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onMedicationAdded = {
                    homeViewModel.refreshMedications()
                    medViewModel.refreshMedications()
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ViewMedication.route) { backStackEntry ->
            val medicationId = backStackEntry.arguments?.getString("medicationId") ?: ""
            ViewMedicationScreen(
                medicationId = medicationId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = {
                    navController.navigate(Screen.EditMedication.createRoute(medicationId))
                }
            )
        }
        
        composable(Screen.EditMedication.route) { bk ->
            val medicationId = bk.arguments?.getString("medicationId") ?: ""
            val backStackEntry = remember(bk) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            val homeViewModel : HomeViewModel = koinViewModel<HomeViewModel>(viewModelStoreOwner = backStackEntry)
            val medViewModel  = koinViewModel<MedicationViewModel>(viewModelStoreOwner = backStackEntry)
            AddMedicationScreen(
                medicationId = medicationId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onMedicationAdded = {
                    homeViewModel.refreshMedications()
                    medViewModel.refreshMedications()
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.AddHealthMetric.route) { backStackEntry ->
            val bk = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            val metricTypeString = backStackEntry.arguments?.getString("metricType") ?: ""
            val metricType = try {
                HealthMetricType.valueOf(metricTypeString)
            } catch (e: IllegalArgumentException) {
                HealthMetricType.HEART_RATE // Default fallback
            }
            val careViewModel  = koinViewModel<CareViewModel>(viewModelStoreOwner = bk)
            AddHealthMetricScreen(
                metricType = metricType,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAddedNew = {
                    navController.popBackStack()
                    careViewModel.refresh()
                },
                careViewModel
            )
        }
        
        composable(Screen.HealthMetricHistory.route) { backStackEntry ->
            val bk = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            val metricTypeString = backStackEntry.arguments?.getString("metricType") ?: ""
            val metricType = try {
                HealthMetricType.valueOf(metricTypeString)
            } catch (e: IllegalArgumentException) {
                HealthMetricType.HEART_RATE // Default fallback
            }
            val careViewModel  = koinViewModel<CareViewModel>(viewModelStoreOwner = bk)
            HealthMetricsHistoryScreen(
                metricType = metricType,
                onNavigateBack = {
                    navController.popBackStack()
                },
                careViewModel
            )
        }

        composable(Screen.EditPatient.route) { bk ->
            val medicationId = bk.arguments?.getString("patientId") ?: ""
            val backStackEntry = remember(bk) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            val homeViewModel : HomeViewModel = koinViewModel<HomeViewModel>(viewModelStoreOwner = backStackEntry)
            val medViewModel  = koinViewModel<MedicationViewModel>(viewModelStoreOwner = backStackEntry)
            PatientOnboardingScreen(
                patientId = medicationId,
                onNavigateToHome = {
                    navController.popBackStack()
                }
            )
        }
    }
}
