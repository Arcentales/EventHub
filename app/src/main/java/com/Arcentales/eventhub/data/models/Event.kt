package com.Arcentales.eventhub.data.models

import com.google.firebase.Timestamp

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val startAt: Timestamp? = null,
    val venueName: String = "",
    val venueAddress: String = "",
    val imageUrl: String = "",
    val status: EventStatus = EventStatus.ACTIVE,
    val walletClassId: String = "",
    val category: String = ""
)

enum class EventStatus { ACTIVE, CANCELLED, COMPLETED }