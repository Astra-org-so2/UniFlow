package com.studentos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.studentos.app.domain.repository.StudentRepository
import com.studentos.app.ui.components.*
import com.studentos.app.ui.navigation.*
import com.studentos.app.ui.screen.analytics.AnalyticsScreen
import com.studentos.app.ui.screen.onboarding.OnboardingScreen
import com.studentos.app.ui.screen.schedule.ScheduleScreen
import com.studentos.app.ui.screen.settings.SettingsScreen
import com.studentos.app.ui.screen.study.StudyScreen
import com.studentos.app.ui.screen.study.subjects.SubjectDetailScreen
import com.studentos.app.ui.screen.study.subjects.SubjectListScreen
import com.studentos.app.ui.screen.tasks.TasksScreen
import com.studentos.app.ui.screen.today.TodayScreen
import com.studentos.app.ui.theme.StudentOSTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudentOSTheme {
                StudentOSApp()
            }
        }
    }
}

@HiltViewModel
class AppViewModel @Inject constructor(
    private val studentRepo: StudentRepository
) : ViewModel() {
    private val _isOnboarded = MutableStateFlow(false)
    val isOnboarded: StateFlow<Boolean> = _isOnboarded.asStateFlow()
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            val student = studentRepo.getCurrentStudentOnce()
            _isOnboarded.value = student != null
            _isLoading.value = false
        }
    }
}

@Composable
fun StudentOSApp(
    appViewModel: AppViewModel = hiltViewModel()
) {
    val isOnboarded by appViewModel.isOnboarded.collectAsState()
    val isLoading by appViewModel.isLoading.collectAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = if (isOnboarded) Screen.Today.route else Screen.Onboarding.route

    // Bottom bar visibility
    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.screen.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(
                                    item.label,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(initialAlpha = 0.3f) + slideInHorizontally(initialOffsetX = { it / 20 }) },
            exitTransition = { fadeOut(targetAlpha = 0.3f) },
            popEnterTransition = { fadeIn(initialAlpha = 0.3f) },
            popExitTransition = { fadeOut(targetAlpha = 0.3f) + slideOutHorizontally(targetOffsetX = { it / 20 }) }
        ) {
            // Onboarding
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(Screen.Today.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // Main tabs
            composable(Screen.Today.route) {
                TodayScreen(onNavigate = { navController.navigate(it) })
            }
            composable(Screen.Schedule.route) {
                ScheduleScreen(onNavigate = { navController.navigate(it) })
            }
            composable(Screen.Tasks.route) {
                TasksScreen(onNavigate = { navController.navigate(it) })
            }
            composable(Screen.Study.route) {
                StudyScreen(onNavigate = { navController.navigate(it) })
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(onNavigate = { navController.navigate(it) })
            }

            // Settings
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigate = { navController.navigate(it) },
                    onBack = { navController.popBackStack() }
                )
            }

            // Subjects
            composable(Screen.Subjects.route) {
                SubjectListScreen(
                    onNavigate = { navController.navigate(it) },
                    onSubjectClick = { id -> navController.navigate(Screen.SubjectDetail.createRoute(id)) }
                )
            }
            composable(
                route = Screen.SubjectDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getLong("id") ?: return@composable
                SubjectDetailScreen(
                    subjectId = subjectId,
                    onNavigate = { navController.navigate(it) },
                    onBack = { navController.popBackStack() }
                )
            }

            // Subject analytics
            composable(
                route = Screen.SubjectAnalytics.route,
                arguments = listOf(navArgument("subjectId") { type = NavType.LongType })
            ) { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getLong("subjectId") ?: return@composable
                SubjectDetailScreen(
                    subjectId = subjectId,
                    onNavigate = { navController.navigate(it) },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
