package com.Arcentales.eventhub.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Arcentales.eventhub.data.models.Ticket
import com.Arcentales.eventhub.data.models.TicketStatus
import com.Arcentales.eventhub.ui.theme.*
import com.Arcentales.eventhub.viewmodel.TicketsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsScreen(
    onBack: () -> Unit,
    viewModel: TicketsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Purchased Tickets", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Blue500)
            }
            return@Scaffold
        }

        if (uiState.tickets.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🎫", fontSize = 56.sp)
                    Text("No tienes tickets todavía", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Compra un ticket para verlo aquí", style = MaterialTheme.typography.bodyMedium, color = Slate400)
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.tickets, key = { it.id }) { ticket ->
                TicketCard(
                    ticket = ticket,
                    onAddToWallet = {
                        viewModel.getWalletSaveUrl(ticket.id)
                    }
                )
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    // Abrir URL de Google Wallet en el navegador
    LaunchedEffect(uiState.walletUrl) {
        uiState.walletUrl?.let { url ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    }
}

@Composable
private fun TicketCard(
    ticket: Ticket,
    onAddToWallet: () -> Unit
) {
    val isUsed     = ticket.status == TicketStatus.USED
    val statusColor = when (ticket.status) {
        TicketStatus.ISSUED   -> Blue500
        TicketStatus.USED     -> Slate400
        TicketStatus.REFUNDED -> Red500
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            // ── Header del ticket ─────────────────────────────────────────
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = ticket.status.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (isUsed) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Green500, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text  = ticket.eventTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = if (isUsed) Slate400 else MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
                    Text(ticket.eventDate, style = MaterialTheme.typography.bodySmall, color = Slate400)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
                    Text(ticket.venueName, style = MaterialTheme.typography.bodySmall, color = Slate400)
                }
            }

            // ── Línea divisora punteada ───────────────────────────────────
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = Slate100
            )

            // ── QR Code ───────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isUsed) Slate100 else MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Placeholder del QR (en la app real se usa el campo ticket.code con una librería QR)
                Surface(
                    modifier  = Modifier.size(120.dp),
                    shape     = RoundedCornerShape(12.dp),
                    color     = if (isUsed) Slate200 else MaterialTheme.colorScheme.surface,
                    shadowElevation = if (isUsed) 0.dp else 8.dp
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.QrCode2,
                            contentDescription = "QR",
                            modifier = Modifier.size(80.dp),
                            tint = if (isUsed) Slate400 else Navy900
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Google Wallet button (solo si el ticket está activo)
                if (!isUsed) {
                    Button(
                        onClick = onAddToWallet,
                        modifier = Modifier.fillMaxWidth(),
                        shape  = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy900)
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add to Google Wallet", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        "Validated on ${ticket.usedAt?.toDate()?.toString() ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    "Ticket ID: #${ticket.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
            }
        }
    }
}
