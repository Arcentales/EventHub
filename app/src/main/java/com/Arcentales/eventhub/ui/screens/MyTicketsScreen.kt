package com.Arcentales.eventhub.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Arcentales.eventhub.data.models.Ticket
import com.Arcentales.eventhub.data.models.TicketStatus
import com.Arcentales.eventhub.ui.theme.*
import com.Arcentales.eventhub.utils.QRGenerator
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
                title = { Text("Mis Entradas", fontWeight = FontWeight.Bold) },
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
    val isUsed = ticket.status == TicketStatus.USED
    val statusColor = when (ticket.status) {
        TicketStatus.ISSUED -> Blue500
        TicketStatus.USED -> Slate400
        else -> Red500 // Incluye REFUNDED o CANCELLED
    }

    // Generar QR en segundo plano
    val qrBitmap by produceState<Bitmap?>(initialValue = null, key1 = ticket.code) {
        value = QRGenerator.generateQRCode(ticket.code, size = 400)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                            text = if (isUsed) "VALIDADO" else "ACTIVO",
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

                Spacer(Modifier.height(12.dp))
                Text(
                    text = ticket.eventTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = if (isUsed) Slate400 else Color(0xFF0F172A)
                )
                Text(
                    text = ticket.ticketTypeName,
                    style = MaterialTheme.typography.labelMedium,
                    color = Blue500,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CalendarToday, null, tint = Slate400, modifier = Modifier.size(14.dp))
                    Text(ticket.eventDate, style = MaterialTheme.typography.bodySmall, color = Slate400)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.LocationOn, null, tint = Slate400, modifier = Modifier.size(14.dp))
                    Text(ticket.venueName, style = MaterialTheme.typography.bodySmall, color = Slate400)
                }
            }

            // ── Divisor ──────────────────────────────────────────────────
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Slate100)

            // ── QR Code ───────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isUsed) Slate100.copy(alpha = 0.5f) else Color.White)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(160.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = if (isUsed) 0.dp else 4.dp,
                    border = if (isUsed) null else androidx.compose.foundation.BorderStroke(1.dp, Slate100)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap!!.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.size(140.dp).then(
                                    if (isUsed) Modifier.background(Color.White.copy(alpha = 0.7f)) else Modifier
                                )
                            )
                        } else {
                            CircularProgressIndicator(modifier = Modifier.size(30.dp), strokeWidth = 2.dp)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (!isUsed) {
                    Button(
                        onClick = onAddToWallet,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Añadir a Google Wallet", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        "Validado el ${ticket.usedAt?.toDate()?.toString()?.take(16) ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "ID: ${ticket.id.takeLast(8).uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400.copy(alpha = 0.7f)
                )
            }
        }
    }
}
