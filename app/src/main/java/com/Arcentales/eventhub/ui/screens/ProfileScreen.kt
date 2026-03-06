package com.Arcentales.eventhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Arcentales.eventhub.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val auth    = remember { FirebaseAuth.getInstance() }
    val user    = auth.currentUser
    val email   = user?.email ?: "usuario@email.com"
    val initial = email.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

    Scaffold(
        topBar = {
            TopAppBar(
                title          = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors         = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // ── Avatar / header ───────────────────────────────────────────
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Brush.linearGradient(listOf(Navy900, Navy800))),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.padding(bottom = 20.dp)
                ) {
                    Box(
                        modifier         = Modifier
                            .size(72.dp)
                            .background(Brush.linearGradient(listOf(Blue500, Cyan500)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initial, color = Color.White, fontWeight = FontWeight.Black, fontSize = 28.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(email, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(user?.uid?.take(12) ?: "", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Menu items ────────────────────────────────────────────────
            val menuItems = listOf(
                Triple(Icons.Default.ConfirmationNumber, "Mis Tickets",      "3 tickets activos"),
                Triple(Icons.Default.BookmarkBorder,     "Eventos Guardados","5 eventos"),
                Triple(Icons.Default.Notifications,      "Notificaciones",   "Activadas"),
                Triple(Icons.Default.Security,           "Seguridad",        "Verificado"),
                Triple(Icons.Default.Help,               "Ayuda & Soporte",  ""),
            )
            menuItems.forEach { (icon, label, sub) ->
                Card(
                    modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier              = Modifier.padding(16.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(icon, contentDescription = null, tint = Blue500, modifier = Modifier.size(22.dp))
                        Column(Modifier.weight(1f)) {
                            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            if (sub.isNotBlank()) Text(sub, style = MaterialTheme.typography.bodySmall, color = Slate400)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Logout ────────────────────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape     = RoundedCornerShape(14.dp),
                colors    = CardDefaults.cardColors(containerColor = Red500.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(0.dp),
                onClick   = {
                    auth.signOut()
                    onLogout()
                }
            ) {
                Row(
                    modifier              = Modifier.padding(16.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = Red500, modifier = Modifier.size(22.dp))
                    Text("Cerrar Sesión", color = Red500, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}