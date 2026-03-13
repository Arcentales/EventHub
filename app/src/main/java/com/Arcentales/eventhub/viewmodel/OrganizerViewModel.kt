package com.Arcentales.eventhub.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Arcentales.eventhub.data.models.Event
import com.Arcentales.eventhub.data.models.TicketType
import com.Arcentales.eventhub.data.repository.EventRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class OrganizerUiState(
    val myEvents: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val uploadProgress: Float = 0f
)

class OrganizerViewModel(
    private val eventRepository: EventRepository = EventRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(OrganizerUiState())
    val uiState: StateFlow<OrganizerUiState> = _uiState.asStateFlow()

    init {
        loadMyEvents()
    }

    fun loadMyEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // En un sistema real filtraríamos por organizerId en Firestore
            eventRepository.getEvents().collect { events ->
                _uiState.value = _uiState.value.copy(
                    myEvents = events,
                    isLoading = false
                )
            }
        }
    }

    suspend fun uploadImage(uri: Uri): String? {
        return try {
            val fileName = "events/${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child(fileName)
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }

    fun createEvent(
        event: Event,
        ticketTypes: List<TicketType>,
        imageUri: Uri? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                var finalImageUrl = event.imageUrl
                if (imageUri != null) {
                    finalImageUrl = uploadImage(imageUri) ?: ""
                }

                val eventRef = db.collection("events").document()
                val eventToSave = event.copy(id = eventRef.id, imageUrl = finalImageUrl)
                
                db.runTransaction { transaction ->
                    transaction.set(eventRef, eventToSave)
                    ticketTypes.forEach { ticket ->
                        val ticketRef = eventRef.collection("ticketTypes").document()
                        transaction.set(ticketRef, ticket.copy(id = ticketRef.id, eventId = eventRef.id))
                    }
                }.await()

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    successMessage = "Evento creado con éxito"
                )
                loadMyEvents()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.localizedMessage
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
