package com.Arcentales.eventhub.data.models

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String? = null,
    val photoUrl: String? = null,
    val role: String = "user", // "user", "organizer", "admin"
    val isOrganizerRequested: Boolean = false,
    val organizerStatus: String = "none" // "none", "pending", "approved", "rejected"
)
