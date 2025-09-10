package com.zahid.dailydose.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zahid.dailydose.presentation.care.CareScreen
import com.zahid.dailydose.presentation.history.HistoryScreen
import com.zahid.dailydose.presentation.home.HomeScreen
import com.zahid.dailydose.presentation.home.HomeViewModel
import com.zahid.dailydose.presentation.medication.MedicationScreen
import com.zahid.dailydose.presentation.medication.MedicationViewModel
import com.zahid.dailydose.presentation.settings.SettingsScreen
import com.zahid.dailydose.domain.model.HealthMetricType
import com.zahid.dailydose.presentation.care.CareViewModel
import org.koin.androidx.compose.koinViewModel

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Medication : BottomNavItem("medication", "Medication", Icons.Default.Medication)
    object History : BottomNavItem("history", "History", Icons.Default.History)
    object Care : BottomNavItem("care", "Care", Icons.Default.Favorite)
    object Settings : BottomNavItem("settings", "Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBottomNavigation(
    onNavigateToAddMedication: () -> Unit = {},
    onNavigateToViewMedication: (String) -> Unit = {},
    onNavigateToEditMedication: (String) -> Unit = {},
    onNavigateToAddHealthMetric: (HealthMetricType) -> Unit = {},
    onNavigateToHealthMetricHistory: (HealthMetricType) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToEditPatient: (String) -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    homeViewModel: HomeViewModel = koinViewModel(),
    medicationViewModel: MedicationViewModel = koinViewModel(),
    careViewModel: CareViewModel = koinViewModel()
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf(
        BottomNavItem.Home,
        BottomNavItem.Medication,
        BottomNavItem.History,
        BottomNavItem.Care,
        BottomNavItem.Settings
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.animateContentSize()
                    ) {
                        Text(
                            text = tabs[selectedTab].title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    when (selectedTab) {
                        0 -> {
                            BadgedBox(
                                modifier = Modifier.padding(8.dp),
                                badge = {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = Color.White
                                    ) {
                                        Text("3") // Example notification count
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    Modifier.padding(4.dp)
                                )

                            }
                        }

                        1 -> {

                            IconButton(
                                onClick = onNavigateToAddMedication,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add Medication",
                                )
                            }

                        }

                        3 -> {
                            IconButton(onClick = { /* Care screen actions */ }) {
                                Icon(Icons.Default.Favorite, contentDescription = "Care")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                item.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Crossfade animation between screens
            Crossfade(
                targetState = selectedTab,
                animationSpec = tween(durationMillis = 300)
            ) { tab ->
                when (tab) {
                    0 -> HomeScreen(viewModel = homeViewModel,onNavigateToAddMedication = onNavigateToAddMedication)
                    1 -> MedicationScreen(
                        viewModel = medicationViewModel, 
                        onNavigateToAddMedication = onNavigateToAddMedication,
                        onNavigateToViewMedication = onNavigateToViewMedication,
                        onNavigateToEditMedication = onNavigateToEditMedication
                    )
                    2 -> HistoryScreen()
                    3 -> CareScreen(
                        onNavigateToAddMetric = onNavigateToAddHealthMetric,
                        onNavigateToMetricHistory = onNavigateToHealthMetricHistory,
                        careViewModel
                    )
                    4 -> SettingsScreen(
                        onNavigateToEditPatient = onNavigateToEditPatient,
                        onNavigateToLogin = onNavigateToLogin
                    )
                }
            }
        }
    }
}
