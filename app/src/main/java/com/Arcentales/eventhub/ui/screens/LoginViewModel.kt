package com.Arcentales.eventhub.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CustomCredential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ─────────────────────────────────────────────────────────────────────────────
// UI STATE — data class inmutable
// Cada cambio crea una COPIA NUEVA del estado (.copy()).
// Compose detecta el cambio y solo recompone lo necesario.
// ─────────────────────────────────────────────────────────────────────────────

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,    // null = sin error
    val isLoginSuccess: Boolean = false  // true = disparar navegación al Home
)

// ─────────────────────────────────────────────────────────────────────────────
// LOGIN VIEW MODEL
//
// Patrón MVVM + Unidirectional Data Flow:
//   Estado (ViewModel) ──► UI ──► Evento ──► ViewModel ──► Nuevo Estado
//
// La UI solo LEE uiState y ENVÍA eventos (onEmailChange, loginWithEmail…).
// El ViewModel procesa la lógica y actualiza el estado.
// Sobrevive a rotaciones de pantalla.
// ─────────────────────────────────────────────────────────────────────────────

class LoginViewModel : ViewModel() {

    // mutableStateOf: Compose observa este valor y recompone cuando cambia.
    // private set: la UI puede LEER pero NO puede modificar uiState directamente.
    var uiState by mutableStateOf(LoginUiState())
        private set

    private val auth = FirebaseAuth.getInstance()

    // Si ya hay sesión activa al abrir la app, ir directo al Home
    init {
        if (auth.currentUser != null) {
            uiState = uiState.copy(isLoginSuccess = true)
        }
    }

    // ── Actualizar campos ─────────────────────────────────────────────────
    // .copy() crea una nueva copia del estado cambiando solo los campos indicados.
    // Limpia el error al escribir para no mostrar mensajes obsoletos.

    fun onEmailChange(newEmail: String) {
        uiState = uiState.copy(email = newEmail, errorMessage = null)
    }

    fun onPasswordChange(newPassword: String) {
        uiState = uiState.copy(password = newPassword, errorMessage = null)
    }

    // ── Login con Email / Password ────────────────────────────────────────
    // viewModelScope.launch: coroutine que se cancela automáticamente si el
    //   ViewModel se destruye → no hay memory leaks.
    // .await(): convierte el Task de Firebase en suspend function.
    //   Espera sin bloquear el hilo principal → la UI no se congela.

    fun loginWithEmail() {
        val email    = uiState.email.trim()
        val password = uiState.password

        if (email.isBlank() || password.isBlank()) {
            uiState = uiState.copy(errorMessage = "Completa todos los campos")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                uiState = uiState.copy(isLoading = false, isLoginSuccess = true)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading    = false,
                    errorMessage = e.localizedMessage ?: "Error al iniciar sesión"
                )
            }
        }
    }

    // ── Registro con Email / Password ─────────────────────────────────────

    fun registerWithEmail() {
        val email    = uiState.email.trim()
        val password = uiState.password

        if (email.isBlank() || password.isBlank()) {
            uiState = uiState.copy(errorMessage = "Completa todos los campos")
            return
        }
        if (password.length < 6) {
            uiState = uiState.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                uiState = uiState.copy(isLoading = false, isLoginSuccess = true)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading    = false,
                    errorMessage = e.localizedMessage ?: "Error al crear la cuenta"
                )
            }
        }
    }

    // ── Google Sign-In — Parte 1: recibir credencial desde la UI ─────────
    // La UI usa Credential Manager (API moderna, reemplaza GoogleSignInClient)
    // para mostrar el bottom sheet del sistema con las cuentas de Google.
    // Entrega aquí la credencial obtenida.

    fun handleGoogleSignInResult(credential: androidx.credentials.Credential) {
        if (credential is CustomCredential &&
            credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
            firebaseAuthWithGoogle(googleCred.idToken)
        } else {
            uiState = uiState.copy(errorMessage = "Credencial de Google no válida")
        }
    }

    // ── Google Sign-In — Parte 2: autenticar en Firebase con el ID Token ──
    // Flujo: Google ID Token → FirebaseCredential → Firebase Auth → isLoginSuccess

    private fun firebaseAuthWithGoogle(idToken: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential).await()
                uiState = uiState.copy(isLoading = false, isLoginSuccess = true)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading    = false,
                    errorMessage = e.localizedMessage ?: "Error con Google Sign-In"
                )
            }
        }
    }

    // ── Error recibido desde la UI (ej: usuario canceló el selector) ──────

    fun onGoogleSignInError(message: String) {
        uiState = uiState.copy(errorMessage = message)
    }

    // ── Reset después de navegar ──────────────────────────────────────────
    // Evita que LaunchedEffect dispare la navegación más de una vez.

    fun onLoginHandled() {
        uiState = uiState.copy(isLoginSuccess = false)
    }
}
