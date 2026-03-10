package com.Arcentales.eventhub.ui.screens.admin

// ─────────────────────────────────────────────────────────────────────────────
// CreateEventScreen.kt
//
// Pantalla exclusiva del rol "admin" (evento@gmail.com).
// Permite crear nuevos eventos y ver los eventos activos con sus estadísticas.
//
// Ruta: Routes.ADMIN_HOME
// Acceso: solo cuando role == "admin" (LoginViewModel → NavGraph)
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Arcentales.eventhub.data.models.Event
import com.Arcentales.eventhub.data.models.EventStatus
import com.Arcentales.eventhub.ui.theme.*
import com.Arcentales.eventhub.viewmodel.EventsViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ── Categorías disponibles ─────────────────────────────────────────────────
private val EVENT_CATEGORIES = listOf("Concerts", "Sports", "Workshops", "Tech", "Art", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    eventsViewModel: EventsViewModel = viewModel()
) {
    val uiState  by eventsViewModel.uiState.collectAsStateWithLifecycle()
    val scope    = rememberCoroutineScope()
    val snack    = remember { SnackbarHostState() }
    val db       = remember { FirebaseFirestore.getInstance() }
    val auth     = remember { FirebaseAuth.getInstance() }

    // ── Form state ────────────────────────────────────────────────────────
    var title         by remember { mutableStateOf("") }
    var description   by remember { mutableStateOf("") }
    var venueName     by remember { mutableStateOf("") }
    var venueAddress  by remember { mutableStateOf("") }
    var selectedCat   by remember { mutableStateOf(EVENT_CATEGORIES[0]) }
    var ticketName    by remember { mutableStateOf("") }
    var ticketPrice   by remember { mutableStateOf("") }
    var ticketQty     by remember { mutableStateOf("") }
    var isSaving      by remember { mutableStateOf(false) }
    var catExpanded   by remember { mutableStateOf(false) }
    var showSuccess   by remember { mutableStateOf(false) }

    fun clearForm() {
        title = ""; description = ""; venueName = ""; venueAddress = ""
        ticketName = ""; ticketPrice = ""; ticketQty = ""
        selectedCat = EVENT_CATEGORIES[0]
    }

    fun saveEvent() {
        if (title.isBlank() || venueName.isBlank()) {
            scope.launch { snack.showSnackbar("Completa nombre y lugar del evento") }
            return
        }
        isSaving = true
        scope.launch {
            try {
                // 1. Crear el evento
                val eventData = hashMapOf(
                    "title"        to title,
                    "description"  to description,
                    "venueName"    to venueName,
                    "venueAddress" to venueAddress,
                    "category"     to selectedCat,
                    "status"       to EventStatus.ACTIVE.name,
                    "startAt"      to Timestamp.now(),
                    "imageUrl"     to "",
                    "walletClassId" to ""
                )
                val eventRef = db.collection("events").add(eventData).await()

                // 2. Crear ticketType si tiene datos
                if (ticketName.isNotBlank()) {
                    val ticketData = hashMapOf(
                        "name"     to ticketName,
                        "description" to "",
                        "price"    to (ticketPrice.toDoubleOrNull() ?: 0.0),
                        "currency" to "USD",
                        "capacity" to (ticketQty.toIntOrNull() ?: 100),
                        "sold"     to 0,
                        "eventId"  to eventRef.id
                    )
                    eventRef.collection("ticketTypes").add(ticketData).await()
                }

                clearForm()
                showSuccess = true
                snack.showSnackbar("✅ Evento publicado correctamente")
            } catch (e: Exception) {
                snack.showSnackbar("Error: ${e.localizedMessage}")
            } finally {
                isSaving = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                title = { Text("Panel de Organizador", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Blue500.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = Blue500, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            // ── Hero header ───────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Navy900, Navy800)))
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Column {
                        Text("Nuevo Evento", style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black, color = Color.White)
                        Text("Completa la información para publicar tu próximo evento.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.55f), modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            // ── Stats row ─────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatChip(icon = Icons.Default.Event,       label = "Eventos",  value = "${uiState.events.size}", color = Blue500,  modifier = Modifier.weight(1f))
                    StatChip(icon = Icons.Default.ConfirmationNumber, label = "Activos", value = "${uiState.events.count { it.status == EventStatus.ACTIVE }}", color = Green500, modifier = Modifier.weight(1f))
                    StatChip(icon = Icons.Default.People,      label = "Staff",    value = "2",   color = Amber500, modifier = Modifier.weight(1f))
                }
            }

            // ── Form card ─────────────────────────────────────────────────
            item {
                Card(
                    modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape     = RoundedCornerShape(20.dp),
                    colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

                        Text("Información del evento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)

                        // Nombre
                        AdminTextField(
                            value         = title,
                            onValueChange = { title = it },
                            label         = "Nombre del evento",
                            placeholder   = "Ej. Conferencia de Tecnología 2024",
                            icon          = Icons.Default.Event
                        )

                        // Lugar
                        AdminTextField(
                            value         = venueName,
                            onValueChange = { venueName = it },
                            label         = "Lugar / Venue",
                            placeholder   = "Ej. Auditorio Central",
                            icon          = Icons.Default.LocationOn
                        )

                        // Dirección
                        AdminTextField(
                            value         = venueAddress,
                            onValueChange = { venueAddress = it },
                            label         = "Dirección completa",
                            placeholder   = "Calle, Ciudad, País",
                            icon          = Icons.Default.Map
                        )

                        // Descripción
                        OutlinedTextField(
                            value         = description,
                            onValueChange = { description = it },
                            label         = { Text("Descripción") },
                            placeholder   = { Text("Describe los detalles del evento...") },
                            modifier      = Modifier.fillMaxWidth(),
                            minLines      = 3,
                            shape         = RoundedCornerShape(14.dp),
                            colors        = adminFieldColors()
                        )

                        // Categoría
                        ExposedDropdownMenuBox(
                            expanded        = catExpanded,
                            onExpandedChange = { catExpanded = it }
                        ) {
                            OutlinedTextField(
                                value         = selectedCat,
                                onValueChange = {},
                                readOnly      = true,
                                label         = { Text("Categoría") },
                                trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                                modifier      = Modifier.fillMaxWidth().menuAnchor(),
                                shape         = RoundedCornerShape(14.dp),
                                colors        = adminFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded    = catExpanded,
                                onDismissRequest = { catExpanded = false }
                            ) {
                                EVENT_CATEGORIES.forEach { cat ->
                                    DropdownMenuItem(
                                        text    = { Text(cat) },
                                        onClick = { selectedCat = cat; catExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Ticket type card ──────────────────────────────────────────
            item {
                Card(
                    modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape     = RoundedCornerShape(20.dp),
                    colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ConfirmationNumber, null, tint = Blue500, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Tipo de entrada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        AdminTextField(
                            value         = ticketName,
                            onValueChange = { ticketName = it },
                            label         = "Nombre del ticket",
                            placeholder   = "Ej. Entrada General / VIP",
                            icon          = Icons.Default.Label
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value         = ticketPrice,
                                onValueChange = { ticketPrice = it },
                                label         = { Text("Precio ($)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier      = Modifier.weight(1f),
                                shape         = RoundedCornerShape(14.dp),
                                colors        = adminFieldColors(),
                                leadingIcon   = { Text("$", color = Slate400, fontWeight = FontWeight.Bold) }
                            )
                            OutlinedTextField(
                                value         = ticketQty,
                                onValueChange = { ticketQty = it },
                                label         = { Text("Cantidad") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier      = Modifier.weight(1f),
                                shape         = RoundedCornerShape(14.dp),
                                colors        = adminFieldColors()
                            )
                        }
                    }
                }
            }

            // ── Save button ───────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Button(
                        onClick   = { saveEvent() },
                        enabled   = !isSaving,
                        modifier  = Modifier.fillMaxWidth().height(54.dp),
                        shape     = RoundedCornerShape(16.dp),
                        colors    = ButtonDefaults.buttonColors(containerColor = Blue500)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("Publicando...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.RocketLaunch, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Guardar y Publicar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                    Text(
                        "Al publicar, el evento será visible para todos los usuarios inmediatamente.",
                        style    = MaterialTheme.typography.bodySmall,
                        color    = Slate400,
                        modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // ── Divider ───────────────────────────────────────────────────
            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = Slate200)
                Text(
                    "Mis eventos activos",
                    style    = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            // ── Event list ────────────────────────────────────────────────
            if (uiState.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Blue500)
                    }
                }
            } else if (uiState.events.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎭", fontSize = 40.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Sin eventos publicados aún", style = MaterialTheme.typography.bodyMedium, color = Slate400)
                        }
                    }
                }
            } else {
                items(uiState.events.filter { it.status == EventStatus.ACTIVE }) { event ->
                    AdminEventRow(event = event, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                }
            }

            // ── Logout ────────────────────────────────────────────────────
            item {
                Card(
                    modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = CardDefaults.cardColors(containerColor = Red500.copy(alpha = 0.08f)),
                    elevation = CardDefaults.cardElevation(0.dp),
                    onClick   = { auth.signOut(); onLogout() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Logout, null, tint = Red500, modifier = Modifier.size(20.dp))
                        Text("Cerrar Sesión", color = Red500, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers de UI
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier            = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Slate400)
        }
    }
}

@Composable
private fun AdminTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label) },
        placeholder   = { Text(placeholder) },
        leadingIcon   = { Icon(icon, null, tint = Slate400, modifier = Modifier.size(20.dp)) },
        modifier      = Modifier.fillMaxWidth(),
        singleLine    = true,
        shape         = RoundedCornerShape(14.dp),
        colors        = adminFieldColors()
    )
}

@Composable
private fun adminFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = Blue500,
    unfocusedBorderColor = Slate200,
    focusedLabelColor    = Blue500,
    unfocusedLabelColor  = Slate400,
)

@Composable
private fun AdminEventRow(event: Event, modifier: Modifier = Modifier) {
    val emoji = when (event.category) {
        "Concerts"  -> "🎵"
        "Sports"    -> "⚽"
        "Workshops" -> "💡"
        "Tech"      -> "💻"
        "Art"       -> "🎨"
        else        -> "🎭"
    }
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier  = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(Blue500.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 22.sp)
            }
            Column(Modifier.weight(1f)) {
                Text(event.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(event.venueName, style = MaterialTheme.typography.bodySmall, color = Slate400)
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Green500.copy(alpha = 0.12f)
            ) {
                Text(
                    "ACTIVO",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = Green500,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}