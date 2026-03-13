package com.Arcentales.eventhub.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.Arcentales.eventhub.data.models.TicketType
import com.Arcentales.eventhub.ui.theme.*
import com.Arcentales.eventhub.viewmodel.EventsViewModel
import com.Arcentales.eventhub.viewmodel.TicketsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    onBack: () -> Unit,
    onTicketPurchased: () -> Unit,
    eventsViewModel: EventsViewModel = viewModel(),
    ticketsViewModel: TicketsViewModel = viewModel()
) {
    val eventsState by eventsViewModel.uiState.collectAsStateWithLifecycle()
    val ticketsState by ticketsViewModel.uiState.collectAsStateWithLifecycle()

    val event = eventsState.selectedEvent

    // Simulador de pago (Dialog)
    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedTicketType by remember { mutableStateOf<TicketType?>(null) }

    // Cargar datos al entrar
    LaunchedEffect(eventId) {
        eventsViewModel.getEventById(eventId)
    }

    // Navegar cuando la compra sea exitosa
    LaunchedEffect(ticketsState.purchaseSuccess) {
        if (ticketsState.purchaseSuccess) {
            ticketsViewModel.resetPurchaseState()
            onTicketPurchased()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del Evento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
                    }
                }
            )
        }
    ) { padding ->
        if (eventsState.isLoading || event == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Blue500)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // ── Imagen del evento ─────────────────────────────────────────
            item {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Slate200),
                    contentScale = ContentScale.Crop
                )
            }

            // ── Info card principal ───────────────────────────────────────
            item {
                Column(Modifier.padding(20.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Blue500.copy(alpha = 0.1f)
                    ) {
                        Text(
                            event.category.uppercase(),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = Blue500,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        event.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(20.dp))

                    // Fecha y Lugar
                    InfoRow(Icons.Default.CalendarToday, "FECHA Y HORA", event.startAt?.toDate()?.toString() ?: "TBD")
                    Spacer(Modifier.height(16.dp))
                    InfoRow(Icons.Default.LocationOn, "LUGAR", "${event.venueName}\n${event.venueAddress}")
                }
            }

            // ── Descripción ───────────────────────────────────────────────
            item {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text("Acerca de este evento", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        event.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate600,
                        lineHeight = 22.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Slate200)
            }

            // ── Tipos de tickets ──────────────────────────────────────────
            item {
                Text(
                    "Selecciona tu entrada",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 12.dp)
                )
            }

            items(eventsState.ticketTypes) { ticketType ->
                TicketTypeItem(
                    ticketType = ticketType,
                    onBuyClick = {
                        selectedTicketType = ticketType
                        showPaymentDialog = true
                    }
                )
            }

            item { Spacer(Modifier.height(40.dp)) }
        }

        // Diálogo de Pago Simulado
        if (showPaymentDialog && selectedTicketType != null) {
            PaymentSimulationDialog(
                ticketType = selectedTicketType!!,
                isPurchasing = ticketsState.isPurchasing,
                onConfirm = {
                    ticketsViewModel.purchaseTicket(event.id, selectedTicketType!!.id)
                },
                onDismiss = { showPaymentDialog = false }
            )
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(36.dp).background(Slate100, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Blue500, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Slate400, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TicketTypeItem(ticketType: TicketType, onBuyClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(ticketType.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    if (ticketType.isSoldOut) "Agotado" else "${ticketType.available} disponibles",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (ticketType.isSoldOut) Red500 else Green500
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "$${ticketType.price} ${ticketType.currency}",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = Blue500
                )
            }
            Button(
                onClick = onBuyClick,
                enabled = !ticketType.isSoldOut,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue500)
            ) {
                Text("Comprar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PaymentSimulationDialog(
    ticketType: TicketType,
    isPurchasing: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Compra") },
        text = {
            Column {
                Text("Estás por comprar:")
                Text(ticketType.name, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Total a pagar:", fontSize = 12.sp, color = Slate400)
                Text("$${ticketType.price} ${ticketType.currency}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Blue500)
                Spacer(Modifier.height(16.dp))
                Text("Este es un pago simulado. Al confirmar, se generará tu ticket automáticamente.", style = MaterialTheme.typography.labelSmall, color = Slate400)
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = !isPurchasing) {
                if (isPurchasing) CircularProgressIndicator(Modifier.size(20.dp), Color.White)
                else Text("Confirmar Pago")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isPurchasing) {
                Text("Cancelar")
            }
        }
    )
}
