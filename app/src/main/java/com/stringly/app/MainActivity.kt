package com.stringly.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.stringly.app.ui.screens.*
import com.stringly.app.ui.theme.PrimaryRed
import com.stringly.app.ui.theme.StringlyTheme

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Tuner : Screen("tuner", "Tuner", Icons.Default.GraphicEq)
    object Chords : Screen("chords", "Chords", Icons.Default.GridOn)
    object Metronome : Screen("metronome", "Tempo", Icons.Default.Speed)
    object Tabs : Screen("tabs", "Songs", Icons.Default.QueueMusic)
    object JamSpot : Screen("jam-spot", "Jam Spot", Icons.Default.Groups)
    object Pedalboard : Screen("pedalboard", "Pedals", Icons.Default.Speaker)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object Login : Screen( route = "login", title="Login", Icons.Default.Login)
    object SignUp : Screen( route = "signup", title = "Sign Up", Icons.Default.HowToReg)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }
            var defaultTuning by remember { mutableStateOf("Standard") }
            var a4Calibration by remember { mutableStateOf("440 Hz") }

            StringlyTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

                val bottomNavItems = listOf(
                    Screen.Home,
                    Screen.Tuner,
                    Screen.Chords,
                    Screen.Tabs,
                    Screen.JamSpot
                )
                val topLevelRoutes = bottomNavItems.map { it.route }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentRoute in topLevelRoutes) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 6.dp
                            ) {
                                bottomNavItems.forEach { screen ->
                                    val selected = currentRoute == screen.route
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            if (currentRoute != screen.route) {
                                                navController.navigate(screen.route) {
                                                    popUpTo(Screen.Home.route) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    if (screen.route != Screen.Home.route) {
                                                        restoreState = true
                                                    }
                                                }
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = screen.icon,
                                                contentDescription = screen.title
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = screen.title,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                val initialRoute = remember {
                    if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null) {
                        Screen.Home.route
                    } else {
                        Screen.Login.route
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = initialRoute,
                    modifier = Modifier.padding(innerPadding)
                ) {
                        composable(Screen.Login.route) {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate(Screen.Home.route){
                                        popUpTo(Screen.Login.route){
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                },
                                onNavigateToSignUp = {
                                    navController.navigate(Screen.SignUp.route)
                                }
                            )
                        }

                        composable(Screen.SignUp.route) {
                            SignUpScreen(
                                onSignUpSuccess = {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.SignUp.route) {
                                            inclusive = true
                                        }
                                    }
                                },
                                onNavigateToLogin = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(Screen.Home.route) {
                            HomeScreen(onNavigateTo = { route -> navController.navigate(route) })
                        }
                        composable(Screen.Tuner.route) {
                            TunerScreen(
                                defaultTuning = defaultTuning,
                                a4Calibration = a4Calibration
                            )
                        }
                        composable(Screen.Chords.route) {
                            ChordsScreen()
                        }
                        composable(Screen.Metronome.route) {
                            MetronomeScreen(onBack = { navController.popBackStack() })
                        }
                        composable(Screen.Tabs.route) {
                            TabsScreen(
                                onOpenPedalboard = { navController.navigate(Screen.Pedalboard.route) }
                            )
                        }
                        composable(Screen.JamSpot.route) {
                            JamSpotScreen()
                        }
                        composable(Screen.Pedalboard.route) {
                            PedalboardScreen(onBack = { navController.popBackStack() })
                        }
                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                isDarkTheme = isDarkTheme,
                                onToggleDarkTheme = { isDarkTheme = it },
                                defaultTuning = defaultTuning,
                                onChangeDefaultTuning = { defaultTuning = it },
                                a4Calibration = a4Calibration,
                                onChangeA4Calibration = { a4Calibration = it },
                                onBack = { navController.popBackStack() },
                                onNavigateTo = { route ->
                                    if (route in topLevelRoutes) {
                                        if (currentRoute != route) {
                                            navController.navigate(route) {
                                                popUpTo(Screen.Home.route) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                if (route != Screen.Home.route) {
                                                    restoreState = true
                                                }
                                            }
                                        }
                                    } else {
                                        navController.navigate(route)
                                    }
                                },
                                onLogout = {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
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
