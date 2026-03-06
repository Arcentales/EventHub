package com.Arcentales.eventhub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Arcentales.eventhub.data.models.Event
import com.Arcentales.eventhub.data.models.TicketType
import com.Arcentales.eventhub.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── UI State ──────────────────────────────────────────────────────────────

data class EventsUiState(
    val events: List<Event> = emptyList(),
    val filteredEvents: List<Event> = emptyList(),
    val selectedEvent: Event? = null,
    val ticketTypes: List<TicketType> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

// ── ViewModel ─────────────────────────────────────────────────────────────

class EventsViewModel(
    private val repository: EventRepository = EventRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getEvents().collect { events ->
                val filtered = applyFilters(events, _uiState.value.searchQuery, _uiState.value.selectedCategory)
                _uiState.value = _uiState.value.copy(
                    events = events,
                    filteredEvents = filtered,
                    isLoading = false
                )
            }
        }
    }

    fun selectEvent(event: Event) {
        _uiState.value = _uiState.value.copy(selectedEvent = event)
        loadTicketTypes(event.id)
    }

    private fun loadTicketTypes(eventId: String) {
        viewModelScope.launch {
            val types = repository.getTicketTypes(eventId)
            _uiState.value = _uiState.value.copy(ticketTypes = types)
        }
    }

    fun onSearchQueryChange(query: String) {
        val filtered = applyFilters(_uiState.value.events, query, _uiState.value.selectedCategory)
        _uiState.value = _uiState.value.copy(searchQuery = query, filteredEvents = filtered)
    }

    fun onCategoryChange(category: String) {
        val filtered = applyFilters(_uiState.value.events, _uiState.value.searchQuery, category)
        _uiState.value = _uiState.value.copy(selectedCategory = category, filteredEvents = filtered)
    }

    private fun applyFilters(events: List<Event>, query: String, category: String): List<Event> {
        return events.filter { event ->
            val matchesQuery = query.isBlank() || event.title.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || event.category == category
            matchesQuery && matchesCategory
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
