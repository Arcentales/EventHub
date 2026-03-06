package com.Arcentales.eventhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.Arcentales.eventhub.data.models.Ticket
import com.Arcentales.eventhub.data.models.TicketStatus
import com.Arcentales.eventhub.ui.theme.*

@Composable
fun TicketCard(
    ticket: Ticket,
    onAddToWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUsed      = ticket.status == TicketStatus.USED
    val statusColor = when (ticket.status) {
        TicketStatus.ISSUED   -> Blue500
        TicketStatus.USED     -> Slate400
        TicketStatus.REFUNDED -> Red500
    }

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            // ── Header ────────────────────────────────────────────────────
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text     = ticket.status.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (isUsed) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Green500, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text       = ticket.eventTitle,
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color      = if (isUsed) Slate400 else MaterialTheme.colorScheme.onSurface
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

            // ── Divisor punteado ──────────────────────────────────────────
            HorizontalDivider(
                modifier  = Modifier.padding(horizontal = 16.dp),
                thickness = 1.dp,
                color     = Slate100
            )

            // ── QR + Wallet ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isUsed) Slate100 else MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier        = Modifier.size(120.dp),
                    shape           = RoundedCornerShape(12.dp),
                    color           = if (isUsed) Slate200 else MaterialTheme.colorScheme.surface,
                    shadowElevation = if (isUsed) 0.dp else 8.dp
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.QrCode2,
                            contentDescription = "Código QR",
                            modifier           = Modifier.size(80.dp),
                            tint               = if (isUsed) Slate400 else Navy900
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (!isUsed) {
                    GoogleWalletButton(onClick = onAddToWallet)
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