package com.Arcentales.eventhub.ui.screens.admin

// ─────────────────────────────────────────────────────────────────────────────
// CreateEventScreen.kt  —  Vista del ORGANIZADOR
// Rol:  "admin"  (evento@gmail.com)   Ruta: Routes.ADMIN_HOME
// ─────────────────────────────────────────────────────────────────────────────

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Arcentales.eventhub.data.models.EventStatus
import com.Arcentales.eventhub.ui.theme.*
import com.Arcentales.eventhub.viewmodel.EventsViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val EVENT_CATEGORIES = listOf("Concerts", "Sports", "Workshops", "Tech", "Art", "Other")
private val TEAL = Color(0xFF0D9488)
private val TEAL_DARK = Color(0xFF0F766E)

private fun categoryEmoji(cat: String) = when (cat) {
    "Concerts"  -> "🎵"; "Sports" -> "⚽"; "Workshops" -> "💡"
    "Tech"      -> "💻"; "Art"    -> "🎨"; else        -> "🎭"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    onNavigateToProfile      : () -> Unit,
    onNavigateToStaffManager : () -> Unit = {},
    onLogout                 : () -> Unit,
    eventsViewModel          : EventsViewModel = viewModel()
) {
    val uiState = eventsViewModel.uiState.collectAsStateWithLifecycle().value
    val scope   = rememberCoroutineScope()
    val snack   = remember { SnackbarHostState() }
    val db      = remember { FirebaseFirestore.getInstance() }
    val auth    = remember { FirebaseAuth.getInstance() }
    val email   = auth.currentUser?.email ?: "evento@gmail.com"

    var title        by remember { mutableStateOf("") }
    var description  by remember { mutableStateOf("") }
    var venueName    by remember { mutableStateOf("") }
    var venueAddress by remember { mutableStateOf("") }
    var selectedCat  by remember { mutableStateOf(EVENT_CATEGORIES[0]) }
    var catExpanded  by remember { mutableStateOf(false) }
    var ticketName   by remember { mutableStateOf("") }
    var ticketPrice  by remember { mutableStateOf("") }
    var ticketQty    by remember { mutableStateOf("") }
    var isSaving     by remember { mutableStateOf(false) }
    var attempted    by remember { mutableStateOf(false) }

    val titleError = attempted && title.isBlank()
    val venueError = attempted && venueName.isBlank()

    fun clearForm() {
        title = ""; description = ""; venueName = ""; venueAddress = ""
        ticketName = ""; ticketPrice = ""; ticketQty = ""; attempted = false
        selectedCat = EVENT_CATEGORIES[0]
    }

    fun saveEvent() {
        attempted = true
        if (title.isBlank() || venueName.isBlank()) {
            scope.launch { snack.showSnackbar("⚠️ Completa al menos nombre y lugar") }
            return
        }
        isSaving = true
        scope.launch {
            try {
                val eventRef = db.collection("events").add(hashMapOf(
                    "title" to title, "description" to description,
                    "venueName" to venueName, "venueAddress" to venueAddress,
                    "category" to selectedCat, "status" to EventStatus.ACTIVE.name,
                    "startAt" to Timestamp.now(), "imageUrl" to "", "walletClassId" to ""
                )).await()
                if (ticketName.isNotBlank()) {
                    eventRef.collection("ticketTypes").add(hashMapOf(
                        "name" to ticketName, "description" to "",
                        "price" to (ticketPrice.toDoubleOrNull() ?: 0.0),
                        "currency" to "USD",
                        "capacity" to (ticketQty.toIntOrNull() ?: 100),
                        "sold" to 0, "eventId" to eventRef.id
                    )).await()
                }
                clearForm()
                snack.showSnackbar("✅ Evento publicado correctamente")
            } catch (e: Exception) {
                snack.showSnackbar("Error: ${e.localizedMessage}")
            } finally { isSaving = false }
        }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {

            // ── Header ────────────────────────────────────────────────────
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(TEAL_DARK, TEAL)))
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Box(Modifier.size(90.dp).offset(x = 280.dp, y = (-20).dp).background(Color.White.copy(.07f), CircleShape))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Column {
                            Text("Panel de Organizador", color = Color.White.copy(.65f), fontSize = 12.sp)
                            Text("Nuevo Evento", color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineMedium)
                            Text("Completa los datos para publicar", color = Color.White.copy(.5f), fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onNavigateToProfile, modifier = Modifier.size(40.dp).background(Color.White.copy(.15f), CircleShape)) {
                                Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Box(Modifier.size(40.dp).background(Color.White.copy(.2f), CircleShape), Alignment.Center) {
                                Text(email.first().uppercaseChar().toString(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }

            // ── Stats ─────────────────────────────────────────────────────
            item {
                val activeCount = uiState.events.count { it.status == EventStatus.ACTIVE }
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatChip(Icons.Default.Event, "Mis Eventos", "${uiState.events.size}", TEAL, Modifier.weight(1f))
                    StatChip(Icons.Default.CheckCircle, "Activos", "$activeCount", Green500, Modifier.weight(1f))
                    StatChip(Icons.Default.ConfirmationNumber, "Vendidos", "—", Blue500, Modifier.weight(1f))
                }
            }

            // ── Card: Datos del evento ────────────────────────────────────
            item {
                SectionCard("Información del Evento", Icons.Default.Event) {
                    AdminTextField(title, { title = it }, "Nombre del evento *", "Ej. Conferencia de Tecnología 2024", Icons.Default.Label, titleError, "Campo requerido")
                    AdminTextField(venueName, { venueName = it }, "Lugar / Venue *", "Ej. Auditorio Central", Icons.Default.LocationOn, venueError, "Campo requerido")
                    AdminTextField(venueAddress, { venueAddress = it }, "Dirección completa", "Calle, Ciudad, País", Icons.Default.Map)
                    OutlinedTextField(
                        value = description, onValueChange = { description = it },
                        label = { Text("Descripción del evento") },
                        placeholder = { Text("Describe artistas, agenda, detalles...") },
                        modifier = Modifier.fillMaxWidth(), minLines = 3,
                        shape = RoundedCornerShape(14.dp), colors = tealFieldColors()
                    )
                    ExposedDropdownMenuBox(catExpanded, { catExpanded = it }) {
                        OutlinedTextField(
                            "${categoryEmoji(selectedCat)}  $selectedCat", {},
                            readOnly = true, label = { Text("Categoría") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(14.dp), colors = tealFieldColors()
                        )
                        ExposedDropdownMenu(catExpanded, { catExpanded = false }) {
                            EVENT_CATEGORIES.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text("${categoryEmoji(cat)}  $cat") },
                                    onClick = { selectedCat = cat; catExpanded = false }
                                )
                            }
                        }
                    }
                }
            }

            // ── Card: Tipo de entrada ─────────────────────────────────────
            item {
                SectionCard("Tipo de Entrada", Icons.Default.ConfirmationNumber) {
                    AdminTextField(ticketName, { ticketName = it }, "Nombre del ticket", "Ej. General, VIP, Early Bird", Icons.Default.LocalActivity)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            ticketPrice, { ticketPrice = it },
                            label = { Text("Precio (USD)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp),
                            colors = tealFieldColors(),
                            leadingIcon = { Text("$", color = Slate400, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp)) }
                        )
                        OutlinedTextField(
                            ticketQty, { ticketQty = it },
                            label = { Text("Capacidad") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp),
                            colors = tealFieldColors(),
                            leadingIcon = { Icon(Icons.Default.People, null, tint = Slate400, modifier = Modifier.size(18.dp)) }
                        )
                    }
                    if (ticketName.isNotBlank()) {
                        Surface(shape = RoundedCornerShape(10.dp), color = TEAL.copy(.07f), modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.ConfirmationNumber, null, tint = TEAL, modifier = Modifier.size(18.dp))
                                Column {
                                    Text(ticketName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(buildString {
                                        if (ticketPrice.isNotBlank()) append("\$$ticketPrice  ")
                                        if (ticketQty.isNotBlank()) append("· $ticketQty disponibles")
                                    }, fontSize = 12.sp, color = Slate400)
                                }
                            }
                        }
                    }
                }
            }

            // ── Botón Publicar ────────────────────────────────────────────
            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Button(
                        onClick = { saveEvent() }, enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TEAL)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(Modifier.size(20.dp), Color.White, strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("Publicando...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        } else {
                            Icon(Icons.Default.RocketLaunch, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Guardar y Publicar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                    Text(
                        "Al publicar, el evento será visible para todos los usuarios inmediatamente.",
                        style = MaterialTheme.typography.bodySmall, color = Slate400,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }
            }

            // ── Eventos activos ───────────────────────────────────────────
            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), color = Slate200)
                Text("Mis Eventos Activos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
            }

            if (uiState.isLoading) {
                item { Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) { CircularProgressIndicator(color = TEAL) } }
            } else if (uiState.events.none { it.status == EventStatus.ACTIVE }) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🗓️", fontSize = 40.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Sin eventos publicados aún", color = Slate400)
                            Text("¡Crea tu primer evento arriba!", color = Slate400, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } else {
                items(uiState.events.filter { it.status == EventStatus.ACTIVE }) { event ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).background(TEAL.copy(.08f), RoundedCornerShape(12.dp)), Alignment.Center) {
                                Text(categoryEmoji(event.category), fontSize = 22.sp)
                            }
                            Column(Modifier.weight(1f)) {
                                Text(event.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1)
                                Text(event.venueName, style = MaterialTheme.typography.bodySmall, color = Slate400, maxLines = 1)
                            }
                            Surface(shape = RoundedCornerShape(8.dp), color = Green500.copy(.12f)) {
                                Text("ACTIVO", Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = Green500, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // ── Gestión de Staff ──────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF7C3AED).copy(.08f)),
                    elevation = CardDefaults.cardElevation(0.dp),
                    onClick = { onNavigateToStaffManager() }
                ) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(36.dp).background(Color(0xFF7C3AED).copy(.15f), RoundedCornerShape(10.dp)),
                            Alignment.Center
                        ) {
                            Icon(Icons.Default.ManageAccounts, null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Gestionar Staff", fontWeight = FontWeight.SemiBold, color = Color(0xFF7C3AED))
                            Text("Asignar roles a trabajadores", style = MaterialTheme.typography.bodySmall, color = Slate400)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF7C3AED), modifier = Modifier.size(18.dp))
                    }
                }
            }

            // ── Logout ────────────────────────────────────────────────────
            item {
                Spacer(Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Red500.copy(.08f)),
                    elevation = CardDefaults.cardElevation(0.dp),
                    onClick = { auth.signOut(); onLogout() }
                ) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Logout, null, tint = Red500, modifier = Modifier.size(20.dp))
                        Text("Cerrar Sesión", color = Red500, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun SectionCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, null, tint = Color(0xFF0D9488), modifier = Modifier.size(20.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            content()
        }
    }
}

@Composable
private fun StatChip(icon: ImageVector, label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = color.copy(.08f)), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Slate400)
        }
    }
}

@Composable
private fun AdminTextField(value: String, onValueChange: (String) -> Unit, label: String, placeholder: String, icon: ImageVector, isError: Boolean = false, errorMsg: String = "") {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) }, placeholder = { Text(placeholder) },
        leadingIcon = { Icon(icon, null, tint = Slate400, modifier = Modifier.size(20.dp)) },
        modifier = Modifier.fillMaxWidth(), singleLine = true,
        shape = RoundedCornerShape(14.dp), isError = isError,
        supportingText = if (isError) {{ Text(errorMsg, color = Red500) }} else null,
        colors = tealFieldColors()
    )
}

@Composable
private fun tealFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF0D9488), unfocusedBorderColor = Slate200,
    focusedLabelColor  = Color(0xFF0D9488), unfocusedLabelColor  = Slate400,
    errorBorderColor   = Red500,            errorLabelColor      = Red500
)