package com.Arcentales.eventhub.viewmodel

// ─────────────────────────────────────────────────────────────────────────────
// StaffViewModel.kt
//
// Gestiona la lista de usuarios registrados y permite al Admin cambiar
// el rol de cualquier usuario directamente en Firestore.
//
// Firestore: colección "users" — campos: uid, email, role, displayName
//
// Roles válidos: "user" | "admin" | "scanner"
// ─────────────────────────────────────────────────────────────────────────────

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Arcentales.eventhub.data.models.User
import com.Arcentales.eventhub.utils.FirestoreCollections
import com.Arcentales.eventhub.utils.UserRoles
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ── UI State ──────────────────────────────────────────────────────────────────

data class StaffUiState(
    val users         : List<User>  = emptyList(),
    val filteredUsers : List<User>  = emptyList(),
    val searchQuery   : String      = "",
    val isLoading     : Boolean     = false,
    val isUpdating    : String?     = null,   // uid del usuario que está siendo actualizado
    val isAddingWorker: Boolean     = false,  // true mientras se busca/crea el trabajador
    val successMessage: String?     = null,
    val errorMessage  : String?     = null
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class StaffViewModel : ViewModel() {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(StaffUiState())
    val uiState: StateFlow<StaffUiState> = _uiState.asStateFlow()

    // UID del admin logueado — nunca puede cambiar su propio rol desde aquí
    private val currentAdminUid get() = auth.currentUser?.uid ?: ""

    init {
        loadUsers()
    }

    // ── Cargar todos los usuarios en tiempo real ──────────────────────────────
    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // Snapshot listener en tiempo real
                db.collection(FirestoreCollections.USERS)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            _uiState.value = _uiState.value.copy(
                                isLoading    = false,
                                errorMessage = "Error cargando usuarios: ${error.message}"
                            )
                            return@addSnapshotListener
                        }
                        val users = snapshot?.documents?.mapNotNull { doc ->
                            try {
                                User(
                                    uid         = doc.getString("uid")         ?: doc.id,
                                    email       = doc.getString("email")       ?: "",
                                    displayName = doc.getString("displayName"),
                                    role        = doc.getString("role")        ?: UserRoles.USER
                                )
                            } catch (e: Exception) { null }
                        } ?: emptyList()

                        val sorted = users.sortedWith(
                            compareBy({ roleOrder(it.role) }, { it.email })
                        )
                        val filtered = applySearch(sorted, _uiState.value.searchQuery)
                        _uiState.value = _uiState.value.copy(
                            users         = sorted,
                            filteredUsers = filtered,
                            isLoading     = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = e.localizedMessage ?: "Error desconocido"
                )
            }
        }
    }

    // ── Cambiar rol de un usuario ─────────────────────────────────────────────
    fun updateUserRole(uid: String, newRole: String) {
        if (uid == currentAdminUid) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "No puedes cambiar tu propio rol"
            )
            return
        }
        if (newRole !in listOf<String>(UserRoles.USER, UserRoles.ADMIN, UserRoles.SCANNER)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Rol no válido")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = uid, errorMessage = null)
            try {
                db.collection(FirestoreCollections.USERS)
                    .document(uid)
                    .update("role", newRole)
                    .await()

                val email = _uiState.value.users.find { it.uid == uid }?.email ?: uid
                val roleLabel = roleDisplayName(newRole)
                _uiState.value = _uiState.value.copy(
                    isUpdating     = null,
                    successMessage = "✅ $email → $roleLabel"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdating   = null,
                    errorMessage = "Error al actualizar: ${e.localizedMessage}"
                )
            }
        }
    }

    // ── Agregar trabajador por email ─────────────────────────────────────────
    // Busca el usuario por email en la colección users/.
    // Si lo encuentra, le cambia el rol directamente.
    // Si NO existe (nunca se registró), crea el documento users/{uid fake}
    // con el email y el rol, para que al registrarse quede con ese rol.
    fun addWorkerByEmail(email: String, role: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAddingWorker = true, errorMessage = null)
            try {
                // Buscar si ya existe un usuario con ese email
                val snapshot = db.collection(FirestoreCollections.USERS)
                    .whereEqualTo("email", email)
                    .get().await()

                if (!snapshot.isEmpty) {
                    // Ya existe → actualizamos su rol
                    val doc = snapshot.documents.first()
                    db.collection(FirestoreCollections.USERS)
                        .document(doc.id)
                        .update("role", role).await()
                    _uiState.value = _uiState.value.copy(
                        isAddingWorker = false,
                        successMessage = "✅ Rol asignado a $email"
                    )
                } else {
                    // No existe aún → creamos un documento pre-asignado
                    // Cuando el usuario se registre con ese email, el rol ya estará listo.
                    val preDoc = hashMapOf(
                        "email"       to email,
                        "role"        to role,
                        "displayName" to email.substringBefore("@"),
                        "uid"         to ""   // se llenará cuando haga login
                    )
                    db.collection(FirestoreCollections.USERS)
                        .document(email.replace(".", "_").replace("@", "__"))
                        .set(preDoc).await()
                    _uiState.value = _uiState.value.copy(
                        isAddingWorker = false,
                        successMessage = "✅ Trabajador pre-registrado. Rol se asignará al primer login."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAddingWorker = false,
                    errorMessage   = "Error: ${e.localizedMessage}"
                )
            }
        }
    }

    // ── Búsqueda por email o nombre ───────────────────────────────────────────
    fun onSearchChange(query: String) {
        val filtered = applySearch(_uiState.value.users, query)
        _uiState.value = _uiState.value.copy(searchQuery = query, filteredUsers = filtered)
    }

    private fun applySearch(users: List<User>, query: String): List<User> {
        if (query.isBlank()) return users
        return users.filter {
            it.email.contains(query, ignoreCase = true) ||
                    (it.displayName?.contains(query, ignoreCase = true) == true)
        }
    }
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }

    // Orden visual: admin primero, scanner segundo, user último
    private fun roleOrder(role: String) = when (role) {
        UserRoles.ADMINISTRADOR -> 0
        UserRoles.ADMIN         -> 1
        UserRoles.SCANNER       -> 2
        else                    -> 3
    }
}

// ── Helpers globales de display ───────────────────────────────────────────────

fun roleDisplayName(role: String) = when (role) {
    UserRoles.ADMINISTRADOR -> "Administrador"
    UserRoles.ADMIN         -> "Organizador"
    UserRoles.SCANNER       -> "Escáner"
    else                    -> "Cliente"
}

fun roleEmoji(role: String) = when (role) {
    UserRoles.ADMINISTRADOR -> "🛡️"
    UserRoles.ADMIN         -> "👑"
    UserRoles.SCANNER       -> "🔍"
    else                    -> "🎟️"
}