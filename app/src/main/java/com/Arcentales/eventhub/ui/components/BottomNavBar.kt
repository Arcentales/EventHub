package com.Arcentales.eventhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Arcentales.eventhub.ui.theme.Blue500
import com.Arcentales.eventhub.ui.theme.Navy900
import com.Arcentales.eventhub.ui.theme.Slate400

@Composable
fun BottomNavBar(
    currentRoute: String,
    onHomeClick: () -> Unit,
    onTicketsClick: () -> Unit,
    onScanClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier        = modifier.fillMaxWidth(),
        color           = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
        tonalElevation  = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // ── Tabs ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                NavItem(
                    icon      = Icons.Default.Home,
                    label     = "Events",
                    isActive  = currentRoute == "home",
                    onClick   = onHomeClick
                )
                NavItem(
                    icon      = Icons.Default.Explore,
                    label     = "Explore",
                    isActive  = currentRoute == "explore",
                    onClick   = {}
                )
                // Espacio central para el FAB
                Spacer(Modifier.width(56.dp))

                NavItem(
                    icon      = Icons.Default.ConfirmationNumber,
                    label     = "My Tickets",
                    isActive  = currentRoute == "tickets",
                    onClick   = onTicketsClick
                )
                NavItem(
                    icon      = Icons.Default.Person,
                    label     = "Profile",
                    isActive  = currentRoute == "profile",
                    onClick   = onProfileClick
                )
            }

            // ── FAB Escáner centrado ──────────────────────────────────────
            FloatingActionButton(
                onClick        = onScanClick,
                modifier       = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-20).dp)
                    .size(52.dp),
                containerColor = Navy900,
                shape          = CircleShape,
                elevation      = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(
                    imageVector        = Icons.Default.QrCodeScanner,
                    contentDescription = "Escanear QR",
                    tint               = Color.White,
                    modifier           = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = label,
            tint               = if (isActive) Blue500 else Slate400,
            modifier           = Modifier.size(22.dp)
        )
        Text(
            text       = label,
            fontSize   = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color      = if (isActive) Blue500 else Slate400
        )
        // Indicador punto activo
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(Blue500, CircleShape)
            )
        }
    }
}