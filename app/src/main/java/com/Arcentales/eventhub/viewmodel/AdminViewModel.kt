package com.Arcentales.eventhub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Arcentales.eventhub.data.models.User
import com.Arcentales.eventhub.data.models.Event
import com.Arcentales.eventhub.data.models.TicketType
import com.Arcentales.eventhub.data.repository.UserRepositoryImpl
import com.Arcentales.eventhub.domain.repository.UserRepository
import com.Arcentales.eventhub.utils.FirestoreCollections
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AdminUiState(
    val pendingRequests: List<User> = emptyList(),
    val allUsers: List<User> = emptyList(),
    val events: List<Event> = emptyList(),
    val filteredEvents: List<Event> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AdminViewModel(
    private val userRepository: UserRepository = UserRepositoryImpl()
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadData()
        fetchEvents()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val users = userRepository.getAllUsers()
                val pending = users.filter { it.organizerStatus == "pending" }
                _uiState.value = _uiState.value.copy(
                    allUsers = users,
                    pendingRequests = pending,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar datos: ${e.message}"
                )
            }
        }
    }

    fun fetchEvents() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection(FirestoreCollections.EVENTS).get().await()
                val events = snapshot.toObjects(Event::class.java)
                _uiState.value = _uiState.value.copy(
                    events = events,
                    filteredEvents = filterEvents(events, _uiState.value.searchQuery)
                )
            } catch (ignore: Exception) {}
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
                _uiState.value = _uiState.value.copy(successMessage = "Evento eliminado")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }

    fun approveOrganizer(uid: String) {
        viewModelScope.launch {
            val result = userRepository.updateOrganizerStatus(uid, "approved")
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(successMessage = "Organizador aprobado")
                loadData()
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Error al aprobar")
            }
        }
    }

    fun rejectOrganizer(uid: String) {
        viewModelScope.launch {
            val result = userRepository.updateOrganizerStatus(uid, "rejected")
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(successMessage = "Solicitud rechazada")
                loadData()
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Error al rechazar")
            }
        }
    }

    // Ticket Types Management for Admin Screens
    suspend fun getTicketTypes(eventId: String): List<TicketType> {
        return try {
            val snapshot = db.collection(FirestoreCollections.EVENTS)
                .document(eventId)
                .collection(FirestoreCollections.TICKET_TYPES)
                .get().await()
            snapshot.toObjects(TicketType::class.java).map { it.copy(eventId = eventId) }
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
