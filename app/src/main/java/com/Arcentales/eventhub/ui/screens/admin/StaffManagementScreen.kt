package com.Arcentales.eventhub.ui.screens.admin

// ─────────────────────────────────────────────────────────────────────────────
// StaffManagementScreen.kt  —  Gestión de Trabajadores
// Ruta: Routes.STAFF_MANAGEMENT
//
// Diseño basado en el bosquejo:
//   - Lista de usuarios tipo tabla con avatar, email y badge de rol
//   - Al tocar un usuario → ModalBottomSheet para asignar rol
//   - Roles: Organizador | Escáner | Cliente  (radio buttons naranjas)
//   - Botones: Cancel / Assign Role (naranja)
// ─────────────────────────────────────────────────────────────────────────────

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Arcentales.eventhub.data.models.User
import com.Arcentales.eventhub.ui.theme.*
import com.Arcentales.eventhub.utils.UserRoles
import com.Arcentales.eventhub.viewmodel.StaffViewModel
import com.Arcentales.eventhub.viewmodel.roleDisplayName
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// ── Colores ───────────────────────────────────────────────────────────────────
private val Orange      = Color(0xFFEC5B13)
private val OrangeLight = Color(0xFFFFF3EE)
private val BlueDeep    = Color(0xFF1E3A8A)
private val BlueMain    = Color(0xFF2563EB)

// ── Roles para el bottom sheet ────────────────────────────────────────────────
private data class RoleOption(
    val key        : String,
    val label      : String,
    val description: String,
    val icon       : ImageVector
)

private val ROLE_OPTIONS = listOf(
    RoleOption(UserRoles.ADMINISTRADOR, "Administrador", "Gestiona roles y permisos del equipo.",         Icons.Default.ManageAccounts),
    RoleOption(UserRoles.ADMIN,         "Organizador",   "Crea y gestiona eventos del sistema.",          Icons.Default.AdminPanelSettings),
    RoleOption(UserRoles.SCANNER,       "Escáner",       "Acceso para validación y escaneo de entradas.", Icons.Default.QrCodeScanner),
    RoleOption(UserRoles.USER,          "Cliente",       "Compra y accede a sus tickets.",                Icons.Default.ConfirmationNumber)
)

// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementScreen(
    onBack   : () -> Unit,
    onLogout : () -> Unit,
    viewModel: StaffViewModel = viewModel()
) {
    val uiState      by viewModel.uiState.collectAsStateWithLifecycle()
    val snack        = remember { SnackbarHostState() }
    val scope        = rememberCoroutineScope()
    val currentUid   = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val currentEmail = remember { FirebaseAuth.getInstance().currentUser?.email ?: "" }

    val sheetState    = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedUser  by remember { mutableStateOf<User?>(null) }
    var showSheet     by remember { mutableStateOf(false) }

    // ── Estado del card "Agregar Trabajador" ──────────────────────────────
    var addEmail      by remember { mutableStateOf("") }
    var addRole       by remember { mutableStateOf(UserRoles.SCANNER) }
    var addExpanded   by remember { mutableStateOf(false) }
    var addEmailError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { snack.showSnackbar(it, duration = SnackbarDuration.Short); viewModel.clearMessages() }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snack.showSnackbar(it, duration = SnackbarDuration.Short); viewModel.clearMessages() }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snack) },
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->

        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {

            // ── Header azul ───────────────────────────────────────────────
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(BlueDeep, BlueMain)))
                        .padding(20.dp)
                ) {
                    // Decorativos
                    Box(Modifier.size(140.dp).offset(x = 260.dp, y = (-40).dp).background(Color.White.copy(.04f), CircleShape))

                    Column {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            // Logo + nombre app
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier.size(32.dp).background(BlueMain, RoundedCornerShape(8.dp)),
                                    Alignment.Center
                                ) {
                                    Text("A", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                }
                                IconButton(
                                    onClick  = onBack,
                                    modifier = Modifier.size(32.dp).background(Color.White.copy(.15f), CircleShape)
                                ) {
                                    Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                            // Avatar admin (KJ)
                            Box(
                                Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(.2f))
                                    .then(
                                        Modifier.clip(CircleShape)
                                    ),
                                Alignment.Center
                            ) {
                                Text(
                                    currentEmail.take(2).uppercase().let {
                                        if (it.length >= 2) it else currentEmail.firstOrNull()?.uppercaseChar()?.toString() ?: "A"
                                    },
                                    color      = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize   = 14.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Text("Gestión de trabajadores", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                        Text(
                            "Administrar roles y permisos de usuario.",
                            color    = Color.White.copy(.6f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(Modifier.height(14.dp))

                        // Stats
                        val adminCount   = uiState.users.count { it.role.lowercase() == UserRoles.ADMIN || it.role.lowercase() == UserRoles.ADMINISTRADOR }
                        val scannerCount = uiState.users.count { it.role == UserRoles.SCANNER }
                        val userCount    = uiState.users.count { it.role == UserRoles.USER }
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatChip("👑 $adminCount", Modifier.weight(1f))
                            StatChip("🔍 $scannerCount", Modifier.weight(1f))
                            StatChip("🎟️ $userCount", Modifier.weight(1f))
                        }
                    }
                }
            }

            // ── Buscador ──────────────────────────────────────────────────
            item {
                Card(
                    modifier  = Modifier.fillMaxWidth().padding(16.dp),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    OutlinedTextField(
                        value         = uiState.searchQuery,
                        onValueChange = viewModel::onSearchChange,
                        placeholder   = { Text("Buscar trabajadores por nombre o correo electrónico...", color = Slate400, fontSize = 14.sp) },
                        leadingIcon   = { Icon(Icons.Default.Search, null, tint = Slate400) },
                        trailingIcon  = {
                            if (uiState.searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.onSearchChange("") }) {
                                    Icon(Icons.Default.Clear, null, tint = Slate400)
                                }
                            }
                        },
                        modifier   = Modifier.fillMaxWidth(),
                        shape      = RoundedCornerShape(14.dp),
                        colors     = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = BlueMain,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
            }

            // ── Loading ───────────────────────────────────────────────────
            if (uiState.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), Alignment.Center) {
                        CircularProgressIndicator(color = BlueMain)
                    }
                }
                return@LazyColumn
            }

            // ── Empty ─────────────────────────────────────────────────────
            if (uiState.filteredUsers.isEmpty() && !uiState.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("👥", fontSize = 40.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (uiState.searchQuery.isBlank()) "No hay usuarios registrados"
                                else "Sin resultados para \"${uiState.searchQuery}\"",
                                color = Slate400
                            )
                        }
                    }
                }
                return@LazyColumn
            }


            // ── Card: Agregar Trabajador ─────────────────────────────────
            item {
                AddWorkerCard(
                    email         = addEmail,
                    onEmailChange = { addEmail = it; addEmailError = null },
                    selectedRole  = addRole,
                    onRoleChange  = { addRole = it },
                    expanded      = addExpanded,
                    onToggle      = { addExpanded = !addExpanded },
                    emailError    = addEmailError,
                    isLoading     = uiState.isAddingWorker,
                    onAdd         = {
                        val emailTrimmed = addEmail.trim()
                        if (emailTrimmed.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches()) {
                            addEmailError = "Ingresa un email válido"
                        } else {
                            viewModel.addWorkerByEmail(emailTrimmed, addRole)
                            addEmail    = ""
                            addRole     = UserRoles.SCANNER
                            addExpanded = false
                        }
                    }
                )
                Spacer(Modifier.height(8.dp))
            }

            // ── Encabezado tabla ──────────────────────────────────────────
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("DETALLES DEL TRABAJADOR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate400, letterSpacing = 1.sp)
                    Text("ROL ACTUAL",   fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate400, letterSpacing = 1.sp)
                }
            }

            // ── Lista ─────────────────────────────────────────────────────
            item {
                Card(
                    modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column {
                        uiState.filteredUsers.forEachIndexed { idx, user ->
                            WorkerRow(
                                user          = user,
                                isCurrentUser = user.uid == currentUid,
                                isUpdating    = uiState.isUpdating == user.uid,
                                onClick       = {
                                    if (user.uid != currentUid) {
                                        selectedUser = user
                                        showSheet    = true
                                    }
                                }
                            )
                            if (idx < uiState.filteredUsers.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color    = Color(0xFFF1F5F9)
                                )
                            }
                        }
                    }
                }
            }

            // ── Logout ────────────────────────────────────────────────────
            item {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = CardDefaults.cardColors(containerColor = Red500.copy(.08f)),
                    elevation = CardDefaults.cardElevation(0.dp),
                    onClick   = { FirebaseAuth.getInstance().signOut(); onLogout() }
                ) {
                    Row(
                        modifier              = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Logout, null, tint = Red500, modifier = Modifier.size(20.dp))
                        Text("Cerrar Sesión", color = Red500, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    // ── Bottom Sheet ─────────────────────────────────────────────────────────
    if (showSheet) {
        selectedUser?.let { user ->
            ModalBottomSheet(
                onDismissRequest = { showSheet = false; selectedUser = null },
                sheetState       = sheetState,
                shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                containerColor   = Color.White,
                dragHandle       = {
                    Box(
                        Modifier.padding(top = 12.dp, bottom = 4.dp).width(48.dp).height(4.dp)
                            .clip(CircleShape).background(Color(0xFFCBD5E1))
                    )
                }
            ) {
                RoleAssignSheet(
                    user       = user,
                    isUpdating = uiState.isUpdating == user.uid,
                    onAssign   = { newRole ->
                        viewModel.updateUserRole(user.uid, newRole)
                        scope.launch { sheetState.hide() }
                        showSheet    = false
                        selectedUser = null
                    },
                    onDismiss  = {
                        scope.launch { sheetState.hide() }
                        showSheet    = false
                        selectedUser = null
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WorkerRow
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WorkerRow(
    user         : User,
    isCurrentUser: Boolean,
    isUpdating   : Boolean,
    onClick      : () -> Unit
) {
    val roleLow = user.role.lowercase()
    val avatarBg = when (roleLow) {
        UserRoles.ADMINISTRADOR -> Color(0xFFFEF3C7)
        UserRoles.ADMIN         -> Color(0xFFEDE9FE)
        UserRoles.SCANNER       -> Color(0xFFD1FAE5)
        else                    -> Color(0xFFDBEAFE)
    }
    val avatarFg = when (roleLow) {
        UserRoles.ADMINISTRADOR -> Color(0xFFD97706)
        UserRoles.ADMIN         -> Color(0xFF7C3AED)
        UserRoles.SCANNER       -> Color(0xFF059669)
        else                    -> Color(0xFF1D4ED8)
    }
    val initials = (user.displayName?.takeIf { it.isNotBlank() } ?: user.email)
        .split(" ", "@").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .take(2).ifEmpty { "?" }

    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isCurrentUser, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            modifier              = Modifier.weight(1f)
        ) {
            Box(Modifier.size(44.dp).background(avatarBg, CircleShape), Alignment.Center) {
                Text(initials, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = avatarFg)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    (user.displayName?.takeIf { it.isNotBlank() } ?: user.email.substringBefore("@")),
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 14.sp,
                    color      = Color(0xFF0F172A),
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(user.email, fontSize = 12.sp, color = Slate400, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (isCurrentUser) Text("Tú", fontSize = 10.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
            }
        }

        if (isUpdating) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Orange, strokeWidth = 2.dp)
        } else {
            RoleBadge(roleLow)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RoleBadge
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RoleBadge(role: String) {
    val bg    = when (role) { UserRoles.ADMINISTRADOR -> Color(0xFFFEF3C7); UserRoles.ADMIN -> Color(0xFFF3E8FF); UserRoles.SCANNER -> Color(0xFFD1FAE5); else -> Color(0xFFDBEAFE) }
    val fg    = when (role) { UserRoles.ADMINISTRADOR -> Color(0xFFD97706); UserRoles.ADMIN -> Color(0xFF7C3AED); UserRoles.SCANNER -> Color(0xFF059669); else -> Color(0xFF1D4ED8) }
    val label = when (role) { UserRoles.ADMINISTRADOR -> "Administrador"; UserRoles.ADMIN -> "Organizador"; UserRoles.SCANNER -> "Escáner"; else -> "Cliente" }
    Surface(shape = RoundedCornerShape(20.dp), color = bg) {
        Text(
            label,
            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize   = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color      = fg
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RoleAssignSheet — Bottom Sheet
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RoleAssignSheet(
    user      : User,
    isUpdating: Boolean,
    onAssign  : (String) -> Unit,
    onDismiss : () -> Unit
) {
    var selectedRole by remember { mutableStateOf(user.role.lowercase()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp)
    ) {
        // Título
        Text("Asignar Rol de Usuario", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFF0F172A))
        Text("Seleccione el nivel de acceso para este miembro del equipo.",
            fontSize = 14.sp, color = Slate400, modifier = Modifier.padding(top = 4.dp))

        Spacer(Modifier.height(20.dp))

        // Card usuario
        val roleLow    = user.role.lowercase()
        val avatarBg   = when (roleLow) { UserRoles.ADMINISTRADOR -> Color(0xFFFEF3C7); UserRoles.ADMIN -> Color(0xFFEDE9FE); UserRoles.SCANNER -> Color(0xFFD1FAE5); else -> Color(0xFFDBEAFE) }
        val avatarFg   = when (roleLow) { UserRoles.ADMINISTRADOR -> Color(0xFFD97706); UserRoles.ADMIN -> Color(0xFF7C3AED); UserRoles.SCANNER -> Color(0xFF059669); else -> Color(0xFF1D4ED8) }
        val initials   = (user.displayName?.takeIf { it.isNotBlank() } ?: user.email)
            .split(" ", "@").filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }.take(2).ifEmpty { "?" }

        Card(
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            elevation = CardDefaults.cardElevation(0.dp),
            modifier  = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier              = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Box(Modifier.size(60.dp).background(avatarBg, CircleShape), Alignment.Center) {
                    Text(initials, fontWeight = FontWeight.Black, fontSize = 20.sp, color = avatarFg)
                }
                Column {
                    Text(
                        user.displayName?.takeIf { it.isNotBlank() }
                            ?: user.email.substringBefore("@").replaceFirstChar { it.uppercase() },
                        fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0F172A)
                    )
                    Text(user.email, fontSize = 13.sp, color = Slate400)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        modifier              = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.VerifiedUser, null, tint = Orange, modifier = Modifier.size(13.dp))
                        Text("Actualmente: ${roleDisplayName(roleLow)}", fontSize = 12.sp, color = Orange, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Opciones de rol
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ROLE_OPTIONS.forEach { option ->
                val isSelected = selectedRole == option.key
                val borderColor by animateColorAsState(
                    targetValue   = if (isSelected) Orange else Color(0xFFE2E8F0),
                    animationSpec = tween(180), label = "border"
                )
                val bgColor by animateColorAsState(
                    targetValue   = if (isSelected) OrangeLight else Color.White,
                    animationSpec = tween(180), label = "bg"
                )

                Surface(
                    modifier  = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { selectedRole = option.key },
                    shape     = RoundedCornerShape(16.dp),
                    color     = bgColor,
                    border    = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
                ) {
                    Row(
                        modifier              = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        // Radio button manual
                        Box(
                            Modifier.size(22.dp).clip(CircleShape)
                                .background(if (isSelected) Orange else Color.White),
                            Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(Modifier.size(8.dp).background(Color.White, CircleShape))
                            } else {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    shape    = CircleShape,
                                    color    = Color.White,
                                    border   = BorderStroke(2.dp, Color(0xFFCBD5E1))
                                ) {}
                            }
                        }

                        // Ícono del rol
                        Icon(
                            option.icon, null,
                            tint     = if (isSelected) Orange else Slate400,
                            modifier = Modifier.size(22.dp)
                        )

                        // Texto
                        Column(Modifier.weight(1f)) {
                            Text(
                                option.label,
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 15.sp,
                                color      = if (isSelected) Color(0xFF0F172A) else Color(0xFF334155)
                            )
                            Text(option.description, fontSize = 12.sp, color = Slate400)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Botones
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick   = onDismiss,
                modifier  = Modifier.weight(2f).height(52.dp),
                shape     = RoundedCornerShape(14.dp),
                colors    = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0), contentColor = Color(0xFF475569)),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text("Cancelar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Button(
                onClick   = { onAssign(selectedRole) },
                enabled   = !isUpdating,
                modifier  = Modifier.weight(2f).height(52.dp),
                shape     = RoundedCornerShape(14.dp),
                colors    = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (isUpdating) CircularProgressIndicator(Modifier.size(20.dp), Color.White, strokeWidth = 2.dp)
                else Text("Asignar Rol", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// StatChip

// ───────────────────────────────────────────────────────────────────────────────
// AddWorkerCard — Card para agregar un trabajador por email
// ───────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWorkerCard(
    email        : String,
    onEmailChange: (String) -> Unit,
    selectedRole : String,
    onRoleChange : (String) -> Unit,
    expanded     : Boolean,
    onToggle     : () -> Unit,
    emailError   : String?,
    isLoading    : Boolean,
    onAdd        : () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            // ── Header del card (siempre visible) ───────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth().clickable(onClick = onToggle),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(40.dp).background(BlueMain.copy(.1f), RoundedCornerShape(10.dp)),
                        Alignment.Center
                    ) {
                        Icon(Icons.Default.PersonAdd, null, tint = BlueMain, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("Agregar Trabajador", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                        Text("Invita por correo y asigna su rol", fontSize = 12.sp, color = Slate400)
                    }
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null,
                    tint = Slate400,
                    modifier = Modifier.size(22.dp)
                )
            }

            // ── Formulario expandible ────────────────────────────────────────────
            if (expanded) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(Modifier.height(16.dp))

                // Campo email
                Text("Correo electrónico", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF334155))
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value         = email,
                    onValueChange = onEmailChange,
                    placeholder   = { Text("ejemplo@correo.com", color = Slate400, fontSize = 14.sp) },
                    leadingIcon   = { Icon(Icons.Default.Email, null, tint = if (emailError != null) Color(0xFFEF4444) else Slate400, modifier = Modifier.size(18.dp)) },
                    isError       = emailError != null,
                    supportingText = emailError?.let { { Text(it, color = Color(0xFFEF4444), fontSize = 11.sp) } },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = BlueMain,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        errorBorderColor     = Color(0xFFEF4444)
                    ),
                    singleLine    = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                    )
                )

                Spacer(Modifier.height(14.dp))

                // Selector de rol
                Text("Rol a asignar", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF334155))
                Spacer(Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ROLE_OPTIONS.forEach { option ->
                        val isSelected = selectedRole == option.key
                        Surface(
                            modifier  = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onRoleChange(option.key) },
                            shape     = RoundedCornerShape(12.dp),
                            color     = if (isSelected) OrangeLight else Color(0xFFF8FAFC),
                            border    = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Orange else Color(0xFFE2E8F0)
                            )
                        ) {
                            Row(
                                modifier              = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                // Radio
                                Box(
                                    Modifier.size(20.dp).clip(CircleShape)
                                        .background(if (isSelected) Orange else Color.White),
                                    Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(Modifier.size(7.dp).background(Color.White, CircleShape))
                                    } else {
                                        Surface(
                                            modifier = Modifier.fillMaxSize(),
                                            shape    = CircleShape,
                                            color    = Color.White,
                                            border   = BorderStroke(2.dp, Color(0xFFCBD5E1))
                                        ) {}
                                    }
                                }
                                Icon(option.icon, null, tint = if (isSelected) Orange else Slate400, modifier = Modifier.size(18.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(option.label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                                        color = if (isSelected) Color(0xFF0F172A) else Color(0xFF475569))
                                    Text(option.description, fontSize = 11.sp, color = Slate400)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Botón agregar
                Button(
                    onClick   = onAdd,
                    enabled   = !isLoading,
                    modifier  = Modifier.fillMaxWidth().height(50.dp),
                    shape     = RoundedCornerShape(12.dp),
                    colors    = ButtonDefaults.buttonColors(containerColor = BlueMain, contentColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.size(20.dp), Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Agregando...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Agregar Trabajador", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatChip(text: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(10.dp), color = Color.White.copy(.15f)) {
        Text(
            text,
            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            fontSize   = 11.sp,
            color      = Color.White,
            fontWeight = FontWeight.SemiBold,
            maxLines   = 1
        )
    }
}