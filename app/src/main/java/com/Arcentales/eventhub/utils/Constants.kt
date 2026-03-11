package com.Arcentales.eventhub.utils

// ─────────────────────────────────────────────────────────────────────────────
// Rutas de navegación
// ─────────────────────────────────────────────────────────────────────────────
object Routes {
    const val LOGIN             = "login"
    const val HOME              = "home"               // ← clientes
    const val ADMIN_HOME        = "admin_home"         // ← organizador (evento@gmail.com)
    const val STAFF_MANAGEMENT  = "staff_management"   // ← gestión de roles (admin)
    const val EVENT_DETAIL      = "event/{eventId}"
    const val MY_TICKETS        = "tickets"
    const val SCANNER           = "scanner"            // ← escaner@gmail.com entra aquí directo
    const val PROFILE           = "profile"

    fun eventDetail(eventId: String) = "event/$eventId"
}

// ─────────────────────────────────────────────────────────────────────────────
// Colecciones Firestore
// ─────────────────────────────────────────────────────────────────────────────
object FirestoreCollections {
    const val EVENTS       = "events"
    const val TICKET_TYPES = "ticketTypes"
    const val ORDERS       = "orders"
    const val TICKETS      = "tickets"
    const val USERS        = "users"
}

// ─────────────────────────────────────────────────────────────────────────────
// Cloud Functions
// ─────────────────────────────────────────────────────────────────────────────
object CloudFunctions {
    const val CREATE_ORDER_AND_TICKETS = "createOrderAndTickets"
    const val GET_WALLET_SAVE_URL      = "getWalletSaveUrl"
    const val SCAN_TICKET              = "scanTicket"
}

// ─────────────────────────────────────────────────────────────────────────────
// Roles de usuario
// ─────────────────────────────────────────────────────────────────────────────
object UserRoles {
    const val USER           = "user"           // cliente normal — se registra en la app
    const val ADMIN          = "admin"          // organizador — crea y gestiona eventos
    const val ADMINISTRADOR  = "administrador"  // administrador — gestiona roles del staff
    const val SCANNER        = "scanner"        // escaner — valida QR en la entrada
}

// ─────────────────────────────────────────────────────────────────────────────
// Google Wallet
// ─────────────────────────────────────────────────────────────────────────────
object WalletConstants {
    const val SAVE_TO_WALLET_REQUEST_CODE = 1001
}