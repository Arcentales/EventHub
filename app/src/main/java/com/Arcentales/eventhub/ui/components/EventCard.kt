package com.Arcentales.eventhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Arcentales.eventhub.data.models.Event
import com.Arcentales.eventhub.ui.theme.*

@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier            = Modifier.padding(14.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Thumbnail ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(
                        Brush.linearGradient(listOf(Navy900, Navy800)),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (event.category) {
                        "Concerts"  -> "🎵"
                        "Sports"    -> "⚽"
                        "Workshops" -> "🎨"
                        else        -> "🎭"
                    },
                    fontSize = 28.sp
                )
            }

            // ── Info ──────────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = event.title,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Slate400, modifier = Modifier.size(13.dp))
                    Text(event.venueName, style = MaterialTheme.typography.bodySmall, color = Slate400, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Slate400, modifier = Modifier.size(13.dp))
                    Text(
                        text  = event.startAt?.toDate()?.toString()?.take(16) ?: "Próximamente",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text       = "Desde \$45.00",   // reemplazar con precio real del ticketType más barato
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 15.sp,
                    color      = Blue500
                )
            }

            // ── Bookmark ──────────────────────────────────────────────────
            IconButton(
                onClick  = {},
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.BookmarkBorder, contentDescription = "Guardar", tint = Slate400, modifier = Modifier.size(20.dp))
            }
        }
    }
}