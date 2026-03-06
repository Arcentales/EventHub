package com.Arcentales.eventhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Arcentales.eventhub.data.models.Event
import com.Arcentales.eventhub.ui.components.BottomNavBar
import com.Arcentales.eventhub.ui.components.EventCard
import com.Arcentales.eventhub.ui.theme.*
import com.Arcentales.eventhub.viewmodel.EventsViewModel

val categories = listOf("All", "Concerts", "Sports", "Workshops")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onEventClick: (String) -> Unit,
    onNavigateToTickets: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: EventsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute     = "home",
                onHomeClick      = {},
                onTicketsClick   = onNavigateToTickets,
                onScanClick      = onNavigateToScanner,
                onProfileClick   = onNavigateToProfile
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // ── Header ────────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EventHub",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {},
                            modifier = Modifier
                                .size(38.dp)
                                .background(Slate100, CircleShape)
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Slate600)
                        }
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Blue500, Cyan500)), shape = CircleShape)
                                .clickable { onNavigateToProfile() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("C", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // ── Search ────────────────────────────────────────────────────
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    placeholder = { Text("Search events, venues...", color = Slate400) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Slate100,
                        focusedContainerColor   = Slate100,
                        unfocusedBorderColor    = Color.Transparent,
                        focusedBorderColor      = Blue500
                    ),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
            }

            // ── Categorías ────────────────────────────────────────────────
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val selected = uiState.selectedCategory == cat
                        FilterChip(
                            selected = selected,
                            onClick  = { viewModel.onCategoryChange(cat) },
                            label    = { Text(cat, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor     = Blue500,
                                selectedLabelColor         = Color.White,
                                containerColor             = Slate100,
                                labelColor                 = Slate600
                            ),
                            border = null
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // ── Estado de carga ───────────────────────────────────────────
            if (uiState.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Blue500)
                    }
                }
                return@LazyColumn
            }

            // ── Lista de eventos ──────────────────────────────────────────
            if (uiState.filteredEvents.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎭", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("No se encontraron eventos", style = MaterialTheme.typography.titleMedium, color = Slate400)
                        }
                    }
                }
            } else {
                items(uiState.filteredEvents, key = { it.id }) { event ->
                    EventCard(
                        event    = event,
                        onClick  = { onEventClick(event.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}
