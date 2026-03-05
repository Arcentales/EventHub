package com.Arcentales.eventhub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Arcentales.eventhub.data.models.Ticket
import com.Arcentales.eventhub.data.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── UI State ──────────────────────────────────────────────────────────────

data class TicketsUiState(
    val tickets: List<Ticket> = emptyList(),
    val isLoading: Boolean = false,
    val isPurchasing: Boolean = false,
    val purchaseSuccess: Boolean = false,
    val walletUrl: String? = null,
    val errorMessage: String? = null
)

// ── ViewModel ─────────────────────────────────────────────────────────────

class TicketsViewModel(
    private val repository: TicketRepository = TicketRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketsUiState())
    val uiState: StateFlow<TicketsUiState> = _uiState.asStateFlow()

    init {
        loadMyTickets()
    }

    private fun loadMyTickets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getMyTickets().collect { tickets ->
                _uiState.value = _uiState.value.copy(
                    tickets = tickets,
                    isLoading = false
                )
            }
        }
    }

    // ── Comprar ticket ────────────────────────────────────────────────────
    fun purchaseTicket(eventId: String, ticketTypeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPurchasing = true, errorMessage = null)
            val result = repository.purchaseTicket(eventId, ticketTypeId)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isPurchasing = false,
                        purchaseSuccess = true
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isPurchasing = false,
                        errorMessage = e.message ?: "Error al comprar ticket"
                    )
                }
            )
        }
    }

    // ── Obtener URL de Google Wallet ──────────────────────────────────────
    fun getWalletSaveUrl(ticketId: String) {
        viewModelScope.launch {
            val result = repository.getWalletSaveUrl(ticketId)
            result.fold(
                onSuccess = { url ->
                    _uiState.value = _uiState.value.copy(walletUrl = url)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = e.message ?: "Error al obtener URL de Wallet"
                    )
                }
            )
        }
    }

    fun resetPurchaseState() {
        _uiState.value = _uiState.value.copy(purchaseSuccess = false, errorMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
