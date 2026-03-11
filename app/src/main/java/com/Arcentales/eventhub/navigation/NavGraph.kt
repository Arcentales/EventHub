package com.Arcentales.eventhub.navigation

// ─────────────────────────────────────────────────────────────────────────────
// NavGraph.kt
//
// Flujo de navegación por rol al hacer login:
//
//   role == "scanner"  →  SCANNER          (escaner@gmail.com)
//   role == "admin"    →  ADMIN_HOME        (evento@gmail.com / kpojuanluis2025)
//   cualquier otro     →  HOME              (clientes registrados)
//
// Desde ADMIN_HOME hay un botón que navega a STAFF_MANAGEMENT.
//
// Todas las rutas hacen popUpTo(LOGIN) { inclusive = true } para que
// el botón atrás no regrese a la pantalla de login.
// ─────────────────────────────────────────────────────────────────────────────

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.Arcentales.eventhub.ui.screens.*
import com.Arcentales.eventhub.ui.screens.admin.CreateEventScreen
import com.Arcentales.eventhub.ui.screens.admin.StaffManagementScreen
import com.Arcentales.eventhub.ui.screens.login.LoginScreen
import com.Arcentales.eventhub.utils.Routes
import com.Arcentales.eventhub.utils.UserRoles

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController    = navController,
        startDestination = Routes.LOGIN
    ) {

        // ── Login — redirige según rol ─────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { role ->
                    val destination = when (role) {
                        UserRoles.SCANNER       -> Routes.SCANNER          // escaner
                        UserRoles.ADMIN         -> Routes.ADMIN_HOME       // organizador
                        UserRoles.ADMINISTRADOR -> Routes.STAFF_MANAGEMENT // administrador
                        else                    -> Routes.HOME             // cliente / null
                    }
                    navController.navigate(destination) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // ── HOME — Cliente ────────────────────────────────────────────────
        composable(Routes.HOME) {
            HomeScreen(
                onEventClick        = { eventId -> navController.navigate(Routes.eventDetail(eventId)) },
                onNavigateToTickets = { navController.navigate(Routes.MY_TICKETS) },
                onNavigateToScanner = { navController.navigate(Routes.SCANNER) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) }
            )
        }

        // ── ADMIN_HOME — Organizador ──────────────────────────────────────
        composable(Routes.ADMIN_HOME) {
            CreateEventScreen(
                onNavigateToProfile      = { navController.navigate(Routes.PROFILE) },
                onNavigateToStaffManager = { navController.navigate(Routes.STAFF_MANAGEMENT) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── STAFF_MANAGEMENT — Gestión de trabajadores ────────────────────
        composable(Routes.STAFF_MANAGEMENT) {
            StaffManagementScreen(
                onBack   = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Event Detail ──────────────────────────────────────────────────
        composable(
            route     = Routes.EVENT_DETAIL,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            EventDetailScreen(
                eventId           = eventId,
                onBack            = { navController.popBackStack() },
                onTicketPurchased = { navController.navigate(Routes.MY_TICKETS) }
            )
        }

        // ── Mis Tickets ───────────────────────────────────────────────────
        composable(Routes.MY_TICKETS) {
            MyTicketsScreen(onBack = { navController.popBackStack() })
        }

        // ── Scanner — Trabajador escáner QR ──────────────────────────────
        composable(Routes.SCANNER) {
            ScannerScreen(
                onClose = {
                    if (navController.previousBackStackEntry == null) {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.SCANNER) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }

        // ── Perfil — todos los roles ──────────────────────────────────────
        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack   = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}