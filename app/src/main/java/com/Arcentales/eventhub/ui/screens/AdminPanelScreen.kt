package com.Arcentales.eventhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Arcentales.eventhub.data.models.User
import com.Arcentales.eventhub.ui.theme.Blue500
import com.Arcentales.eventhub.ui.theme.Green500
import com.Arcentales.eventhub.ui.theme.Red500
import com.Arcentales.eventhub.ui.theme.Slate400
import com.Arcentales.eventhub.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onLogout: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
            // Stats summary
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard("Usuarios", uiState.allUsers.size.toString(), Icons.Default.People, Modifier.weight(1f))
                StatCard("Pendientes", uiState.pendingRequests.size.toString(), Icons.Default.Check, Modifier.weight(1f))
            }

            Text(
                "Solicitudes de Organizador",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Blue500)
                }
            } else if (uiState.pendingRequests.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay solicitudes pendientes", color = Slate400)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.pendingRequests) { request ->
                        OrganizerRequestItem(
                            user = request,
                            onApprove = { viewModel.approveOrganizer(request.uid) },
                            onReject = { viewModel.rejectOrganizer(request.uid) }
                        )
                    }
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
