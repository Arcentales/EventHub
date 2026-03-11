package com.Arcentales.eventhub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Arcentales.eventhub.data.models.Event
import com.Arcentales.eventhub.data.models.TicketType
import com.Arcentales.eventhub.utils.FirestoreCollections
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AdminUiState(
    val events: List<Event> = emptyList(),
    val filteredEvents: List<Event> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AdminViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        fetchEvents()
    }

    fun fetchEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val snapshot = db.collection(FirestoreCollections.EVENTS).get().await()
                val events = snapshot.toObjects(Event::class.java)
                _uiState.value = _uiState.value.copy(
                    events = events, 
                    filteredEvents = filterEvents(events, _uiState.value.searchQuery),
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.localizedMessage)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredEvents = filterEvents(_uiState.value.events, query)
        )
    }

    private fun filterEvents(events: List<Event>, query: String): List<Event> {
        if (query.isBlank()) return events
        return events.filter { 
            it.title.contains(query, ignoreCase = true) || 
            it.venueName.contains(query, ignoreCase = true) 
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            try {
                db.collection(FirestoreCollections.EVENTS).document(eventId).delete().await()
                fetchEvents()
                _uiState.value = _uiState.value.copy(successMessage = "Evento eliminado correctamente")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }

    // Ticket Types Management
    suspend fun getTicketTypes(eventId: String): List<TicketType> {
        return try {
            val snapshot = db.collection(FirestoreCollections.EVENTS)
                .document(eventId)
                .collection(FirestoreCollections.TICKET_TYPES)
                .get()
                .await()
            snapshot.toObjects(TicketType::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveTicketType(eventId: String, ticketType: TicketType) {
        val docRef = if (ticketType.id.isEmpty()) {
            db.collection(FirestoreCollections.EVENTS)
                .document(eventId)
                .collection(FirestoreCollections.TICKET_TYPES)
                .document()
        } else {
            db.collection(FirestoreCollections.EVENTS)
                .document(eventId)
                .collection(FirestoreCollections.TICKET_TYPES)
                .document(ticketType.id)
        }
        
        val finalTicketType = if (ticketType.id.isEmpty()) ticketType.copy(id = docRef.id, eventId = eventId) else ticketType
        docRef.set(finalTicketType).await()
    }

    suspend fun deleteTicketType(eventId: String, ticketTypeId: String) {
        db.collection(FirestoreCollections.EVENTS)
            .document(eventId)
            .collection(FirestoreCollections.TICKET_TYPES)
            .document(ticketTypeId)
            .delete()
            .await()
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
