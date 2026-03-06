package com.Arcentales.eventhub.data.models

data class TicketType(
    val id: String = "",
    val eventId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val currency: String = "USD",
    val capacity: Int = 0,
    val sold: Int = 0
) {
    val available: Int get() = capacity - sold
    val isSoldOut: Boolean get() = available <= 0
    val isLimited: Boolean get() = available in 1..10
}