package com.Arcentales.eventhub.data.repository

import com.Arcentales.eventhub.data.models.ScanResult
import com.Arcentales.eventhub.data.models.Ticket
import com.Arcentales.eventhub.data.models.TicketStatus
import com.Arcentales.eventhub.utils.CloudFunctions
import com.Arcentales.eventhub.utils.FirestoreCollections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TicketRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val currentUserId get() = auth.currentUser?.uid ?: ""

    // ── Obtener tickets del usuario actual en tiempo real ─────────────────
    fun getMyTickets(): Flow<List<Ticket>> = callbackFlow {
        val listener = db.collection(FirestoreCollections.TICKETS)
            .whereEqualTo("userId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val tickets = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Ticket::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(tickets)
            }
        awaitClose { listener.remove() }
    }

    // ── Crear orden y tickets via Cloud Function ──────────────────────────
    // Llama a la CF `createOrderAndTickets` que controla cupos y crea
    // la orden + tickets de forma atómica en una transacción Firestore.
    suspend fun purchaseTicket(eventId: String, ticketTypeId: String): Result<String> {
        return try {
            val data = hashMapOf(
                "eventId"      to eventId,
                "ticketTypeId" to ticketTypeId,
                "userId"       to currentUserId
            )
            val result = functions
                .getHttpsCallable(CloudFunctions.CREATE_ORDER_AND_TICKETS)
                .call(data).await()

            val orderId = (result.data as? Map<*, *>)?.get("orderId") as? String ?: ""
            Result.success(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Obtener URL para guardar en Google Wallet ─────────────────────────
    suspend fun getWalletSaveUrl(ticketId: String): Result<String> {
        return try {
            val data = hashMapOf("ticketId" to ticketId)
            val result = functions
                .getHttpsCallable(CloudFunctions.GET_WALLET_SAVE_URL)
                .call(data).await()

            val url = (result.data as? Map<*, *>)?.get("saveUrl") as? String ?: ""
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Validar ticket QR via Cloud Function ──────────────────────────────
    // La CF `scanTicket` verifica el ticket, marca como USED y registra
    // la hora de ingreso. Previene uso duplicado con transacción Firestore.
    suspend fun scanTicket(ticketCode: String): Result<ScanResult> {
        return try {
            val data = hashMapOf("ticketCode" to ticketCode)
            val result = functions
                .getHttpsCallable(CloudFunctions.SCAN_TICKET)
                .call(data).await()

            val map = result.data as? Map<*, *>
            val scanResult = ScanResult(
                isValid       = map?.get("isValid") as? Boolean ?: false,
                ticketCode    = ticketCode,
                eventTitle    = map?.get("eventTitle") as? String ?: "",
                attendeeName  = map?.get("attendeeName") as? String ?: "",
                ticketType    = map?.get("ticketType") as? String ?: "",
                errorMessage  = map?.get("errorMessage") as? String ?: ""
            )
            Result.success(scanResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
