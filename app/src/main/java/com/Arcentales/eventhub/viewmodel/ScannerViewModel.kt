package com.Arcentales.eventhub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Arcentales.eventhub.data.models.ScanResult
import com.Arcentales.eventhub.data.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── UI State ──────────────────────────────────────────────────────────────

sealed class ScanState {
    object Idle       : ScanState()
    object Scanning   : ScanState()
    object Processing : ScanState()
    data class Valid  (val result: ScanResult) : ScanState()
    data class Invalid(val message: String)    : ScanState()
}

data class ScannerUiState(
    val scanState: ScanState = ScanState.Idle,
    val lastScannedCode: String = "",
    val flashEnabled: Boolean = false,
    val scanCount: Int = 0
)

// ── ViewModel ─────────────────────────────────────────────────────────────

class ScannerViewModel(
    private val repository: TicketRepository = TicketRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    // Evita procesar el mismo QR dos veces seguidas
    private var lastProcessedCode = ""

    fun onQrCodeDetected(code: String) {
        if (code == lastProcessedCode) return
        if (_uiState.value.scanState is ScanState.Processing) return

        lastProcessedCode = code
        validateTicket(code)
    }

    private fun validateTicket(code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                scanState = ScanState.Processing,
                lastScannedCode = code
            )

            val result = repository.scanTicket(code)
            result.fold(
                onSuccess = { scanResult ->
                    val newState = if (scanResult.isValid)
                        ScanState.Valid(scanResult)
                    else
                        ScanState.Invalid(scanResult.errorMessage)

                    _uiState.value = _uiState.value.copy(
                        scanState = newState,
                        scanCount = _uiState.value.scanCount + 1
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        scanState = ScanState.Invalid(e.message ?: "Error de conexión")
                    )
                }
            )
        }
    }

    fun toggleFlash() {
        _uiState.value = _uiState.value.copy(flashEnabled = !_uiState.value.flashEnabled)
    }

    fun resetToScanning() {
        lastProcessedCode = ""
        _uiState.value = _uiState.value.copy(scanState = ScanState.Scanning)
    }

    fun startScanning() {
        _uiState.value = _uiState.value.copy(scanState = ScanState.Scanning)
    }
}
