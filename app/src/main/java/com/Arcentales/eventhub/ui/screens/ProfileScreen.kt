package com.Arcentales.eventhub.ui.screens

// ─────────────────────────────────────────────────────────────────────────────
// ProfileScreen.kt
//
// El rol se lee desde Firestore (users/{uid}/role) — igual que LoginViewModel.
// Si el usuario es nuevo y no tiene rol asignado, por defecto es "user".
//
// Roles:
//   • "user"    → cliente (se registra en la app)
//   • "scanner" → escaner@gmail.com  — personal que escanea tickets
//   • "admin"   → evento@gmail.com   — organizador de eventos
//
// Ubicación: users/{uid}/lat  y  users/{uid}/lng
//
// AndroidManifest.xml:
//   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
//   <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
// ─────────────────────────────────────────────────────────────────────────────

import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Arcentales.eventhub.ui.theme.*
import com.Arcentales.eventhub.utils.FirestoreCollections
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context      = LocalContext.current
    val auth         = remember { FirebaseAuth.getInstance() }
    val db           = remember { FirebaseFirestore.getInstance() }
    val scope        = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }

    val user    = auth.currentUser
    val email   = user?.email ?: "usuario@email.com"
    val initial = email.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

    // ── Rol desde Firestore (igual que LoginViewModel) ────────────────────
    var role by remember { mutableStateOf("user") }

    // ── Ubicación ─────────────────────────────────────────────────────────
    var locationText  by remember { mutableStateOf("Sin ubicación registrada") }
    var isUpdatingLoc by remember { mutableStateOf(false) }

    val locationPermission = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Cargar rol y ubicación desde Firestore
    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            try {
                val doc = db.collection(FirestoreCollections.USERS).document(uid).get().await()
                role = doc.getString("role") ?: "user"
                val lat = doc.getDouble("lat")
                val lng = doc.getDouble("lng")
                if (lat != null && lng != null) {
                    locationText = "%.5f, %.5f".format(lat, lng)
                }
            } catch (_: Exception) {}
        }
    }

    fun updateLocation() {
        if (!locationPermission.status.isGranted) {
            locationPermission.launchPermissionRequest()
            return
        }
        isUpdatingLoc = true
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        fusedClient.lastLocation
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    locationText = "%.5f, %.5f".format(loc.latitude, loc.longitude)
                    user?.uid?.let { uid ->
                        scope.launch {
                            try {
                                db.collection(FirestoreCollections.USERS).document(uid)
                                    .update("lat", loc.latitude, "lng", loc.longitude)
                                    .await()
                                snackbarHost.showSnackbar("Ubicación actualizada ✓")
                            } catch (_: Exception) {
                                snackbarHost.showSnackbar("Error al guardar ubicación")
                            }
                            isUpdatingLoc = false
                        }
                    }
                } else {
                    isUpdatingLoc = false
                    scope.launch { snackbarHost.showSnackbar("No se pudo obtener la ubicación") }
                }
            }
            .addOnFailureListener {
                isUpdatingLoc = false
                scope.launch { snackbarHost.showSnackbar("Error de GPS: ${it.message}") }
            }
    }

    // ── Metadata por rol ──────────────────────────────────────────────────
    val roleLabel: String = when (role) {
        "scanner" -> "Staff · Escáner"
        "admin"   -> "Organizador de Eventos"
        else      -> "Cliente"
    }
    val roleIcon: ImageVector = when (role) {
        "scanner" -> Icons.Default.QrCodeScanner
        "admin"   -> Icons.Default.Event
        else      -> Icons.Default.Person
    }
    val roleGradient: List<Color> = when (role) {
        "scanner" -> listOf(Color(0xFF0F766E), Color(0xFF0D9488))
        "admin"   -> listOf(Navy900, Navy800)
        else      -> listOf(Blue500, Cyan500)
    }
    val accentColor: Color = when (role) {
        "scanner" -> Color(0xFF0D9488)
        else      -> Blue500
    }

    val menuItems: List<Triple<ImageVector, String, String>> = when (role) {
        "scanner" -> listOf(
            Triple(Icons.Default.QrCodeScanner,   "Mis Escaneos Hoy",  "Ver historial de scans"),
            Triple(Icons.Default.Event,            "Eventos Asignados", "Ver mis eventos activos"),
            Triple(Icons.Default.Notifications,    "Notificaciones",    "Activadas"),
            Triple(Icons.Default.Security,         "Seguridad",         "Verificado"),
        )
        "admin" -> listOf(
            Triple(Icons.Default.AddCircleOutline, "Crear Evento",      "Nuevo evento"),
            Triple(Icons.Default.BarChart,         "Mis Estadísticas",  "Tickets vendidos y asistencia"),
            Triple(Icons.Default.People,           "Gestionar Staff",   "Asignar escáneres"),
            Triple(Icons.Default.Notifications,    "Notificaciones",    "Activadas"),
            Triple(Icons.Default.Security,         "Seguridad",         "Verificado"),
        )
        else -> listOf(
            Triple(Icons.Default.ConfirmationNumber, "Mis Tickets",      "Ver tickets comprados"),
            Triple(Icons.Default.BookmarkBorder,     "Eventos Guardados","5 eventos guardados"),
            Triple(Icons.Default.Notifications,      "Notificaciones",   "Activadas"),
            Triple(Icons.Default.Security,           "Seguridad",        "Verificado"),
            Triple(Icons.Default.Help,               "Ayuda & Soporte",  ""),
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Hero ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Brush.linearGradient(roleGradient)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initial, color = Color.White, fontWeight = FontWeight.Black, fontSize = 32.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(email, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(roleIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Text(roleLabel, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Tarjeta Ubicación ─────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.07f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Mi Ubicación", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(locationText, style = MaterialTheme.typography.bodySmall, color = Slate400)
                        }
                        if (isUpdatingLoc) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = accentColor)
                        } else {
                            TextButton(
                                onClick = { updateLocation() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Actualizar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentColor)
                            }
                        }
                    }
                    if (!locationPermission.status.isGranted) {
                        Spacer(Modifier.height(8.dp))
                        Surface(shape = RoundedCornerShape(8.dp), color = Amber500.copy(alpha = 0.12f)) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Amber500, modifier = Modifier.size(14.dp))
                                Text("Permiso de ubicación requerido", style = MaterialTheme.typography.bodySmall, color = Amber500)
                            }
                        }
                    }
                    if (role == "scanner") {
                        Spacer(Modifier.height(4.dp))
                        Text("📍 Tu ubicación se comparte con el organizador del evento", style = MaterialTheme.typography.bodySmall, color = Slate400, fontSize = 11.sp)
                    } else if (role == "admin") {
                        Spacer(Modifier.height(4.dp))
                        Text("📍 Coordenadas del punto de acceso del evento", style = MaterialTheme.typography.bodySmall, color = Slate400, fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Panel rápido Admin ────────────────────────────────────────
            if (role == "admin") {
                SectionLabel("Panel de Administrador")
                QuickActionsRow(accentColor = accentColor, actions = listOf(
                    Pair(Icons.Default.AddBox,   "Nuevo\nEvento"),
                    Pair(Icons.Default.BarChart, "Estadís-\nticas"),
                    Pair(Icons.Default.People,   "Staff"),
                    Pair(Icons.Default.Payments, "Ingresos"),
                ))
                Spacer(Modifier.height(8.dp))
            }

            // ── Panel rápido Scanner ──────────────────────────────────────
            if (role == "scanner") {
                SectionLabel("Panel de Escáner")
                QuickActionsRow(accentColor = accentColor, actions = listOf(
                    Pair(Icons.Default.QrCodeScanner,  "Escanear\nAhora"),
                    Pair(Icons.Default.History,        "Historial"),
                    Pair(Icons.Default.EventAvailable, "Eventos\nActivos"),
                ))
                Spacer(Modifier.height(8.dp))
            }

            // ── Menú ──────────────────────────────────────────────────────
            SectionLabel("Configuración")
            menuItems.forEach { (icon, label, sub) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
                        Column(Modifier.weight(1f)) {
                            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            if (sub.isNotBlank()) Text(sub, style = MaterialTheme.typography.bodySmall, color = Slate400)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            user?.uid?.let { uid ->
                Text(
                    text = "ID: ${uid.take(16)}…",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }
            Spacer(Modifier.height(8.dp))

            // ── Logout ────────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Red500.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(0.dp),
                onClick = { auth.signOut(); onLogout() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = Red500, modifier = Modifier.size(22.dp))
                    Text("Cerrar Sesión", color = Red500, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = Slate400,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun QuickActionsRow(accentColor: Color, actions: List<Pair<ImageVector, String>>) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        actions.forEach { (icon, label) ->
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}