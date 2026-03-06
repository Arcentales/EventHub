package com.Arcentales.eventhub.data.repository

import com.Arcentales.eventhub.data.models.Event
import com.Arcentales.eventhub.data.models.TicketType
import com.Arcentales.eventhub.utils.FirestoreCollections
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EventRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // ── Obtener todos los eventos activos en tiempo real ──────────────────
    fun getEvents(): Flow<List<Event>> = callbackFlow {
        val listener = db.collection(FirestoreCollections.EVENTS)
            .orderBy("startAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Manejo seguro: enviar lista vacía en lugar de cerrar con error
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Event::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    // ── Obtener un evento por ID ──────────────────────────────────────────
    suspend fun getEventById(eventId: String): Event? {
        return try {
            val doc = db.collection(FirestoreCollections.EVENTS)
                .document(eventId).get().await()
            doc.toObject(Event::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    // ── Obtener tipos de ticket de un evento ──────────────────────────────
    suspend fun getTicketTypes(eventId: String): List<TicketType> {
        return try {
            val snapshot = db.collection(FirestoreCollections.EVENTS)
                .document(eventId)
                .collection(FirestoreCollections.TICKET_TYPES)
                .get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(TicketType::class.java)?.copy(id = doc.id, eventId = eventId)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ── Buscar eventos por título ─────────────────────────────────────────
    suspend fun searchEvents(query: String): List<Event> {
        return try {
            // Firestore no soporta full-text search nativo.
            // Solución simple: traer todos y filtrar en cliente.
            // Para producción usar Algolia o Typesense.
            val snapshot = db.collection(FirestoreCollections.EVENTS).get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Event::class.java)?.copy(id = doc.id)
            }.filter { it.title.contains(query, ignoreCase = true) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
