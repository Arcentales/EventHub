package com.Arcentales.eventhub.domain.repository

import com.Arcentales.eventhub.data.models.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun getUserById(uid: String): User?
    suspend fun requestOrganizerRole(uid: String): Result<Unit>
    suspend fun updateOrganizerStatus(uid: String, status: String): Result<Unit>
    suspend fun getAllUsers(): List<User>
}
