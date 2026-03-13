package com.Arcentales.eventhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Arcentales.eventhub.data.models.User
import com.Arcentales.eventhub.data.models.Event
import com.Arcentales.eventhub.ui.theme.*
import com.Arcentales.eventhub.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onLogout: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administración", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab, containerColor = Color.White, contentColor = Blue500) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Solicitudes", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Moderar Eventos", modifier = Modifier.padding(16.dp))
                }
            }

            if (selectedTab == 0) {
                // --- Pestaña: Solicitudes de Organizador ---
                RequestsTab(uiState, viewModel)
            } else {
                // --- Pestaña: Moderación de Eventos ---
                EventsModerationTab(uiState, viewModel)
            }
        }
    }
}

@Composable
fun RequestsTab(uiState: com.Arcentales.eventhub.viewmodel.AdminUiState, viewModel: AdminViewModel) {
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard("Usuarios", uiState.allUsers.size.toString(), Icons.Default.People, Modifier.weight(1f))
            StatCard("Pendientes", uiState.pendingRequests.size.toString(), Icons.Default.Check, Modifier.weight(1f))
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Blue500)
            }
        } else if (uiState.pendingRequests.isEmpty()) {
            EmptyState("No hay solicitudes pendientes", "👥")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.pendingRequests) { request ->
                    OrganizerRequestItem(request, { viewModel.approveOrganizer(request.uid) }, { viewModel.rejectOrganizer(request.uid) })
                }
            }
        }
    }
}

@Composable
fun EventsModerationTab(uiState: com.Arcentales.eventhub.viewmodel.AdminUiState, viewModel: AdminViewModel) {
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            placeholder = { Text("Buscar eventos...") },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Search, null) }
        )

        if (uiState.filteredEvents.isEmpty()) {
            EmptyState("No se encontraron eventos", "🎭")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.filteredEvents) { event ->
                    EventModerationItem(event, onDelete = { viewModel.deleteEvent(event.id) })
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = Blue500, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text(label, fontSize = 12.sp, color = Slate400)
        }
    }
}

@Composable
fun OrganizerRequestItem(user: User, onApprove: () -> Unit, onReject: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(user.email, fontWeight = FontWeight.Bold)
                Text("UID: ${user.uid.take(8)}...", fontSize = 11.sp, color = Slate400)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onReject, colors = IconButtonDefaults.iconButtonColors(containerColor = Red500.copy(alpha = 0.1f))) {
                    Icon(Icons.Default.Close, contentDescription = "Rechazar", tint = Red500)
                }
                IconButton(onClick = onApprove, colors = IconButtonDefaults.iconButtonColors(containerColor = Green500.copy(alpha = 0.1f))) {
                    Icon(Icons.Default.Check, contentDescription = "Aprobar", tint = Green500)
                }
            }
        }
    }
}

@Composable
fun EventModerationItem(event: Event, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(event.title, fontWeight = FontWeight.Bold)
                Text(event.venueName, fontSize = 12.sp, color = Slate400)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Green500.copy(alpha = 0.1f),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(event.category, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = Green500, fontWeight = FontWeight.Bold)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Red500)
            }
        }
    }
}

@Composable
fun EmptyState(text: String, emoji: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 48.sp)
            Text(text, color = Slate400, fontWeight = FontWeight.Medium)
        }
    }
}
