package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.SiskoViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Retrieve ViewModel securely running in sandbox
        val viewModel: SiskoViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[SiskoViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "auth"
                    ) {
                        // 1. Auth Page
                        composable("auth") {
                            AuthScreen(
                                viewModel = viewModel,
                                onLoginSuccess = {
                                    navController.navigate("dashboard") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 2. Hub Dashboard Dashboard
                        composable("dashboard") {
                            val currentUser by viewModel.currentUser.collectAsState()
                            androidx.compose.runtime.LaunchedEffect(currentUser) {
                                if (currentUser == null) {
                                    navController.navigate("auth") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }

                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToSection = { section ->
                                    navController.navigate(section)
                                }
                            )
                        }

                        // 3. Members Directory Screen
                        composable("members") {
                            MemberScreens(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 4. Attendance Roll Call with GPS Verification
                        composable("attendance") {
                            AttendanceScreens(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 5. Kas/Treasurer Transactions Screen
                        composable("kas") {
                            KasScreens(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 6. Admissions Interview Scores Screen
                        composable("interviews") {
                            InterviewsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 7. Communication Hub (Notices & Discussions)
                        composable("announcements") {
                            AnnouncementsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 8. Photo Documentation Gallery Screen
                        composable("gallery") {
                            GalleryScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 9. Structured Agenda RSVP Events Screen
                        composable("events") {
                            EventsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 10. Achievements Leaderboard Gamifier
                        composable("achievements") {
                            AchievementsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 11. Master Administrator Dashboard Panel
                        composable("admin_panel") {
                            AdminPanelScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onOpenOrgData = {
                                    navController.navigate("dashboard") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
