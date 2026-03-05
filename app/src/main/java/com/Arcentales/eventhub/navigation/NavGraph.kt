package com.Arcentales.eventhub.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.Arcentales.eventhub.ui.screens.*
import com.Arcentales.eventhub.utils.Routes

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        // ── Login ─────────────────────────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // ── Home ──────────────────────────────────────────────────────────
        composable(Routes.HOME) {
            HomeScreen(
                onEventClick = { eventId ->
                    navController.navigate(Routes.eventDetail(eventId))
                },
                onNavigateToTickets  = { navController.navigate(Routes.MY_TICKETS) },
                onNavigateToScanner  = { navController.navigate(Routes.SCANNER) },
                onNavigateToProfile  = { navController.navigate(Routes.PROFILE) }
            )
        }

        // ── Event Detail ──────────────────────────────────────────────────
        composable(
            route = Routes.EVENT_DETAIL,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            EventDetailScreen(
                eventId  = eventId,
                onBack   = { navController.popBackStack() },
                onTicketPurchased = {
                    navController.navigate(Routes.MY_TICKETS)
                }
            )
        }

        // ── My Tickets ────────────────────────────────────────────────────
        composable(Routes.MY_TICKETS) {
            MyTicketsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ── Scanner ───────────────────────────────────────────────────────
        composable(Routes.SCANNER) {
            ScannerScreen(
                onClose = { navController.popBackStack() }
            )
        }

        // ── Profile ───────────────────────────────────────────────────────
        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}
