package com.Arcentales.eventhub.data.models

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String? = null,
    val photoUrl: String? = null,
    val role: String = "user" // "user", "admin", "scanner"
)
