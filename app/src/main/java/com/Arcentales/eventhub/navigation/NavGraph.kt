package com.Arcentales.eventhub.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.Arcentales.eventhub.ui.screens.*
import com.Arcentales.eventhub.ui.screens.login.LoginScreen
import com.Arcentales.eventhub.utils.Routes

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { role ->
                    if (role == "admin") {
                        navController.navigate(Routes.ADMIN_HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                }
            )
        }

        // --- Vista de Cliente ---
        composable(Routes.HOME) {
            HomeScreen(
                onEventClick = { eventId -> navController.navigate(Routes.eventDetail(eventId)) },
                onNavigateToTickets = { navController.navigate(Routes.MY_TICKETS) },
                onNavigateToScanner = { navController.navigate(Routes.SCANNER) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) }
            )
        }

        composable(
            route = Routes.EVENT_DETAIL,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            EventDetailScreen(
                eventId = eventId,
                onBack = { navController.popBackStack() },
                onTicketPurchased = { navController.navigate(Routes.MY_TICKETS) }
            )
        }

        // --- Vista de Administrador ---
        composable(Routes.ADMIN_HOME) {
            AdminHomeScreen(
                onEditEvent = { eventId -> navController.navigate(Routes.adminEventEdit(eventId)) },
                onCreateEvent = { navController.navigate(Routes.adminEventEdit("new")) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ADMIN_HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.ADMIN_EVENT_EDIT,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: "new"
            AdminEventEditScreen(
                eventId = eventId,
                onBack = { navController.popBackStack() }
            )
        }

        // --- Comunes ---
        composable(Routes.MY_TICKETS) {
            MyTicketsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SCANNER) {
            ScannerScreen(onClose = { navController.popBackStack() })
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
