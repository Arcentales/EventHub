package com.Arcentales.eventhub.data.models

import com.google.firebase.Timestamp

data class Ticket(
    val id: String = "",
    val ticketTypeId: String = "",
    val ticketTypeName: String = "",
    val eventId: String = "",
    val eventTitle: String = "",
    val eventDate: String = "",
    val venueName: String = "",
    val userId: String = "",
    val code: String = "",               // UUID único — contenido del QR
    val status: TicketStatus = TicketStatus.ISSUED,
    val usedAt: Timestamp? = null,
    val walletObjectId: String = "",
    val createdAt: Timestamp? = null
)

enum class TicketStatus { ISSUED, USED, REFUNDED }

// ScanResult — resultado del escáner QR
data class ScanResult(
    val isValid: Boolean,
    val ticketCode: String = "",
    val eventTitle: String = "",
    val attendeeName: String = "",
    val ticketType: String = "",
    val errorMessage: String = ""
)