package com.Arcentales.eventhub.utils

object Routes {
    const val LOGIN        = "login"
    const val HOME         = "home"
    const val EVENT_DETAIL = "event/{eventId}"
    const val MY_TICKETS   = "tickets"
    const val SCANNER      = "scanner"
    const val PROFILE      = "profile"

    fun eventDetail(eventId: String) = "event/$eventId"
}

object FirestoreCollections {
    const val EVENTS       = "events"
    const val TICKET_TYPES = "ticketTypes"   // sub-colección de events
    const val ORDERS       = "orders"
    const val TICKETS      = "tickets"
    const val USERS        = "users"
}

object CloudFunctions {
    const val CREATE_ORDER_AND_TICKETS = "createOrderAndTickets"
    const val GET_WALLET_SAVE_URL      = "getWalletSaveUrl"
    const val SCAN_TICKET              = "scanTicket"
}

object WalletConstants {
    const val SAVE_TO_WALLET_REQUEST_CODE = 1001
}
