package com.Arcentales.eventhub.ui.screens.login

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CustomCredential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Arcentales.eventhub.utils.FirestoreCollections
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ─────────────────────────────────────────────────────────────────────────────
// UI STATE — data class inmutable
// ─────────────────────────────────────────────────────────────────────────────

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val isLoginSuccess: Boolean = false,
    val userRole: String? = null
)

// ─────────────────────────────────────────────────────────────────────────────
// LOGIN VIEW MODEL
// ─────────────────────────────────────────────────────────────────────────────

class LoginViewModel : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    init {
        checkSession()
    }

    private fun checkSession() {
        auth.currentUser?.let { user ->
            fetchUserRoleAndNavigate(user.uid)
        }
    }

    private fun fetchUserRoleAndNavigate(uid: String) {
        viewModelScope.launch {
            try {
                val doc = db.collection(FirestoreCollections.USERS).document(uid).get().await()
                val rawRole = doc.getString("role") ?: "user"
                // Normaliza cualquier variante del rol a los valores estándar
                val role = when {
                    rawRole.lowercase() == "administrador" ||
                            rawRole.lowercase().contains("administr") -> "administrador"
                    rawRole.lowercase() == "admin" ||
                            rawRole.lowercase().contains("organiz")   -> "admin"
                    rawRole.lowercase() == "scanner" ||
                            rawRole.lowercase().contains("scan") ||
                            rawRole.lowercase().contains("esc")       -> "scanner"
                    else                                       -> "user"
                }
                uiState = uiState.copy(userRole = role, isLoginSuccess = true)
            } catch (e: Exception) {
                // Si falla Firestore por permisos en el get, permitimos entrar como user
                uiState = uiState.copy(userRole = "user", isLoginSuccess = true)
            }
        }
    }

    fun onEmailChange(newEmail: String) {
        uiState = uiState.copy(email = newEmail, emailError = null, errorMessage = null)
    }

    fun onPasswordChange(newPassword: String) {
        uiState = uiState.copy(password = newPassword, passwordError = null, errorMessage = null)
    }

    private fun validateFields(): Boolean {
        val email = uiState.email.trim()
        val password = uiState.password
        var isValid = true

        if (email.isBlank()) {
            uiState = uiState.copy(emailError = "El correo es obligatorio")
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            uiState = uiState.copy(emailError = "Formato de correo no válido")
            isValid = false
        }

        if (password.isBlank()) {
            uiState = uiState.copy(passwordError = "La contraseña es obligatoria")
            isValid = false
        } else if (password.length < 6) {
            uiState = uiState.copy(passwordError = "Mínimo 6 caracteres")
            isValid = false
        }

        return isValid
    }

    fun loginWithEmail() {
        if (!validateFields()) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val result = auth.signInWithEmailAndPassword(uiState.email, uiState.password).await()
                result.user?.let { fetchUserRoleAndNavigate(it.uid) }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error al iniciar sesión"
                )
            }
        }
    }

    fun registerWithEmail() {
        if (!validateFields()) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val result = auth.createUserWithEmailAndPassword(uiState.email, uiState.password).await()
                result.user?.let { user ->
                    try {
                        // IMPORTANTE: Incluir 'uid' para cumplir con las reglas de Firestore
                        val userData = hashMapOf(
                            "uid" to user.uid,
                            "role" to "user",
                            "email" to user.email
                        )
                        db.collection(FirestoreCollections.USERS).document(user.uid).set(userData).await()
                    } catch (ignore: Exception) {}
                    fetchUserRoleAndNavigate(user.uid)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error al crear la cuenta"
                )
            }
        }
    }

    fun sendPasswordReset() {
        val email = uiState.email.trim()
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            uiState = uiState.copy(emailError = "Ingresa un correo válido para recuperar")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null, infoMessage = null)
            try {
                auth.sendPasswordResetEmail(email).await()
                uiState = uiState.copy(
                    isLoading = false,
                    infoMessage = "Correo de recuperación enviado"
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error al enviar correo"
                )
            }
        }
    }

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

    private fun firebaseAuthWithGoogle(idToken: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(firebaseCredential).await()

                result.user?.let { user ->
                    try {
                        val doc = db.collection(FirestoreCollections.USERS).document(user.uid).get().await()
                        if (!doc.exists()) {
                            // IMPORTANTE: Incluir 'uid' para cumplir con las reglas de Firestore
                            val userData = hashMapOf(
                                "uid" to user.uid,
                                "role" to "user",
                                "email" to user.email
                            )
                            db.collection(FirestoreCollections.USERS).document(user.uid).set(userData).await()
                        }
                        fetchUserRoleAndNavigate(user.uid)
                    } catch (e: Exception) {
                        uiState = uiState.copy(userRole = "user", isLoginSuccess = true)
                    }
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error con Google Sign-In"
                )
            }
        }
    }

    fun onGoogleSignInError(message: String) {
        uiState = uiState.copy(errorMessage = message)
    }

    fun onLoginHandled() {
        uiState = uiState.copy(isLoginSuccess = false)
    }
}