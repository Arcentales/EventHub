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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
    val eventsState  by eventsViewModel.uiState.collectAsStateWithLifecycle()
    val ticketsState by ticketsViewModel.uiState.collectAsStateWithLifecycle()

    val event = eventsState.selectedEvent

    // Navegar cuando la compra sea exitosa
    LaunchedEffect(ticketsState.purchaseSuccess) {
        if (ticketsState.purchaseSuccess) {
            ticketsViewModel.resetPurchaseState()
            onTicketPurchased()
        }
    }

    LaunchedEffect(eventId) {
        // Seleccionar el evento al entrar
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (event == null) {
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
            // ── Hero banner ───────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Brush.linearGradient(listOf(Navy900, Navy800)))
                ) {
                    if (event.status.name == "ACTIVE") {
                        Surface(
                            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = Blue500
                        ) {
                            Text(
                                "Live",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // ── Info card ─────────────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(event.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(16.dp))

                        // Fecha
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Blue500, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("DATE & TIME", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                Text(event.startAt?.toDate()?.toString() ?: "Por confirmar", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(Modifier.height(12.dp))

                        // Venue
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Blue500, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("VENUE", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                Text(event.venueName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(event.venueAddress, style = MaterialTheme.typography.bodySmall, color = Slate400)
                            }
                        }

                        // Google Wallet banner
                        if (event.walletClassId.isNotBlank()) {
                            Spacer(Modifier.height(16.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Navy900
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Fast & Secure Entry", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                                        Text("Save your tickets to Google Wallet", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                }
                            }
                        }
                    }
                }
            }

            // ── Descripción ───────────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("About Event", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        Text(event.description, style = MaterialTheme.typography.bodyMedium, color = Slate600, lineHeight = 22.sp)
                    }
                }
            }

            // ── Tipos de tickets ──────────────────────────────────────────
            item {
                Text(
                    "Ticket Types",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            items(eventsState.ticketTypes) { ticketType ->
                TicketTypeCard(
                    ticketType   = ticketType,
                    isPurchasing = ticketsState.isPurchasing,
                    onBuyClick   = {
                        ticketsViewModel.purchaseTicket(event.id, ticketType.id)
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }

        // Error snackbar
        if (ticketsState.errorMessage != null) {
            LaunchedEffect(ticketsState.errorMessage) {
                ticketsViewModel.clearError()
            }
        }
    }
}

@Composable
private fun TicketTypeCard(
    ticketType: TicketType,
    isPurchasing: Boolean,
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when {
        ticketType.isSoldOut -> Red500
        ticketType.isLimited -> Amber500
        else                 -> Green500
    }
    val statusLabel = when {
        ticketType.isSoldOut -> "SOLD OUT"
        ticketType.isLimited -> "LIMITED"
        else                 -> "AVAILABLE"
    }

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(ticketType.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(ticketType.description, style = MaterialTheme.typography.bodySmall, color = Slate400)
                Spacer(Modifier.height(8.dp))
                Text(
                    if (ticketType.price == 0.0) "Gratis" else "$${ticketType.price}",
                    fontWeight = FontWeight.Black,
                    fontSize   = 22.sp,
                    color      = if (ticketType.isSoldOut) Slate400 else MaterialTheme.colorScheme.onSurface
                )
                Text(statusLabel, style = MaterialTheme.typography.labelSmall, color = statusColor)
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick  = onBuyClick,
                enabled  = !ticketType.isSoldOut && !isPurchasing,
                shape    = RoundedCornerShape(20.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = if (ticketType.isSoldOut) Slate200 else Blue500,
                    contentColor   = if (ticketType.isSoldOut) Slate400 else Color.White
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                if (isPurchasing) {
                    CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(if (ticketType.isSoldOut) "Sold Out" else "Buy Now", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
