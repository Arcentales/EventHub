package com.Arcentales.eventhub.data.models

import com.google.firebase.Timestamp

data class Order(
    val id: String = "",
    val userId: String = "",
    val eventId: String = "",
    val status: OrderStatus = OrderStatus.PENDING,
    val total: Double = 0.0,
    val currency: String = "USD",
    val createdAt: Timestamp? = null
)

enum class OrderStatus { PENDING, COMPLETED, REFUNDED, FAILED }