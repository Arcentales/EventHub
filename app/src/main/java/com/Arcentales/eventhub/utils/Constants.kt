package com.Arcentales.eventhub.utils

object Routes {
    const val LOGIN             = "login"
    const val HOME              = "home"               // Cliente
    const val ADMIN_PANEL       = "admin_panel"         // Administrador (Aprobaciones/Usuarios)
    const val STAFF_MANAGEMENT  = "staff_management"    // Gestión de staff
    const val ORGANIZER_DASHBOARD = "organizer_dashboard" // Organizador (Gestión de eventos)
    const val EVENT_DETAIL      = "event/{eventId}"
    const val MY_TICKETS        = "tickets"
    const val SCANNER           = "scanner"            // Validación de QR
    const val PROFILE           = "profile"
    const val BECOME_ORGANIZER  = "become_organizer"
    const val ADMIN_EVENT_EDIT  = "admin_event_edit/{eventId}"

    fun eventDetail(eventId: String) = "event/$eventId"
    fun adminEventEdit(eventId: String) = "admin_event_edit/$eventId"
}

object UserRoles {
    const val USER           = "user"
    const val ORGANIZER      = "organizer"
    const val ADMIN          = "admin"
    const val ADMINISTRADOR  = "administrador"
    const val SCANNER        = "scanner"
}

object FirestoreCollections {
    const val USERS             = "users"
    const val EVENTS            = "events"
    const val TICKET_TYPES      = "ticketTypes"
    const val ORDERS            = "orders"
    const val TICKETS           = "tickets"
    const val ORGANIZER_REQUESTS = "organizer_requests"
}

object CloudFunctions {
    const val CREATE_ORDER_AND_TICKETS = "createOrderAndTickets"
    const val GET_WALLET_SAVE_URL      = "getWalletSaveUrl"
    const val SCAN_TICKET              = "scanTicket"
}

object TicketStatus {
    const val ISSUED    = "ISSUED"
    const val USED      = "USED"
    const val CANCELLED = "CANCELLED"
}

object PaymentStatus {
    const val PENDING = "PENDING"
    const val PAID    = "PAID"
    const val FAILED  = "FAILED"
}

object WalletConstants {
    const val SAVE_TO_WALLET_REQUEST_CODE = 1001
}
