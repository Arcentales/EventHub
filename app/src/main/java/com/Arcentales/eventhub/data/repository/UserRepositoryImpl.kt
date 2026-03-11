package com.Arcentales.eventhub.data.repository

import com.Arcentales.eventhub.data.models.User
import com.Arcentales.eventhub.domain.repository.UserRepository
import com.Arcentales.eventhub.utils.FirestoreCollections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : UserRepository {

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = db.collection(FirestoreCollections.USERS).document(uid)
            .addSnapshotListener { snapshot, _ ->
                val user = snapshot?.toObject(User::class.java)
                trySend(user)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getUserById(uid: String): User? {
        return try {
            db.collection(FirestoreCollections.USERS).document(uid).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun requestOrganizerRole(uid: String): Result<Unit> {
        return try {
            db.collection(FirestoreCollections.USERS).document(uid)
                .update(
                    "isOrganizerRequested", true,
                    "organizerStatus", "pending"
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateOrganizerStatus(uid: String, status: String): Result<Unit> {
        return try {
            val role = if (status == "approved") "organizer" else "user"
            db.collection(FirestoreCollections.USERS).document(uid)
                .update(
                    "role", role,
                    "organizerStatus", status,
                    "isOrganizerRequested", false
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllUsers(): List<User> {
        return try {
            db.collection(FirestoreCollections.USERS).get().await().toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getOrganizerRequests(): List<User> {
        return try {
            db.collection(FirestoreCollections.USERS)
                .whereEqualTo("organizerStatus", "pending")
                .get().await().toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
