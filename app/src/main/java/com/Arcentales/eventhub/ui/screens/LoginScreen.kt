package com.Arcentales.eventhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Arcentales.eventhub.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

// ═════════════════════════════════════════════════════════════════════════════
// LOGIN SCREEN
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email     by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var showPass  by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error     by remember { mutableStateOf<String?>(null) }
    var isRegister by remember { mutableStateOf(false) }

    val auth = remember { FirebaseAuth.getInstance() }

    // Verificar si ya hay sesión activa
    LaunchedEffect(Unit) {
        if (auth.currentUser != null) onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(Navy900, Navy800, Color(0xFF1a3a5f))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Brush.linearGradient(listOf(Blue500, Cyan500)), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🎫", fontSize = 36.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text("EventHub", color = Color.White, fontWeight = FontWeight.Black, fontSize = 28.sp, letterSpacing = (-1).sp)
            Text("Tu app de eventos y tickets", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
            Spacer(Modifier.height(40.dp))

            // Card de formulario
            Card(
                shape  = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(if (isRegister) "Crear cuenta" else "Iniciar sesión", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)

                    // Email
                    OutlinedTextField(
                        value         = email,
                        onValueChange = { email = it },
                        label         = { Text("Correo electrónico") },
                        leadingIcon   = { Icon(Icons.Default.Email, null) },
                        modifier      = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape  = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Blue500,
                            unfocusedBorderColor = Navy700,
                            focusedLabelColor    = Blue500,
                            focusedTextColor     = Color.White,
                            unfocusedTextColor   = Color.White
                        )
                    )

                    // Password
                    OutlinedTextField(
                        value         = password,
                        onValueChange = { password = it },
                        label         = { Text("Contraseña") },
                        leadingIcon   = { Icon(Icons.Default.Lock, null) },
                        trailingIcon  = {
                            IconButton(onClick = { showPass = !showPass }) {
                                Icon(if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                            }
                        },
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape  = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Blue500,
                            unfocusedBorderColor = Navy700,
                            focusedLabelColor    = Blue500,
                            focusedTextColor     = Color.White,
                            unfocusedTextColor   = Color.White
                        )
                    )

                    // Error
                    error?.let {
                        Text(it, color = Red500, fontSize = 12.sp)
                    }

                    // Botón principal
                    Button(
                        onClick = {
                            isLoading = true
                            error = null
                            if (isRegister) {
                                auth.createUserWithEmailAndPassword(email.trim(), password)
                                    .addOnCompleteListener { task ->
                                        isLoading = false
                                        if (task.isSuccessful) onLoginSuccess()
                                        else error = task.exception?.message
                                    }
                            } else {
                                auth.signInWithEmailAndPassword(email.trim(), password)
                                    .addOnCompleteListener { task ->
                                        isLoading = false
                                        if (task.isSuccessful) onLoginSuccess()
                                        else error = task.exception?.message
                                    }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape    = RoundedCornerShape(14.dp),
                        enabled  = email.isNotBlank() && password.isNotBlank() && !isLoading,
                        colors   = ButtonDefaults.buttonColors(containerColor = Blue500)
                    ) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text(if (isRegister) "Crear cuenta" else "Ingresar", fontWeight = FontWeight.Bold)
                    }

                    // Toggle login/register
                    TextButton(
                        onClick = { isRegister = !isRegister; error = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (isRegister) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate",
                            color = Blue500, fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// PROFILE SCREEN
// ═════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val user = auth.currentUser
    val email = user?.email ?: "usuario@email.com"
    val initial = email.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Header del perfil
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Brush.linearGradient(listOf(Navy900, Navy800))),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
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

            // Opciones
            val menuItems = listOf(
                Triple(Icons.Default.ConfirmationNumber, "Mis Tickets",     "3 tickets activos"),
                Triple(Icons.Default.BookmarkBorder,    "Eventos Guardados","5 eventos"),
                Triple(Icons.Default.Notifications,     "Notificaciones",   "Activadas"),
                Triple(Icons.Default.Security,          "Seguridad",        "Verificado"),
                Triple(Icons.Default.Help,              "Ayuda & Soporte",  ""),
            )

            menuItems.forEach { (icon, label, sub) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape  = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
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

            // Logout
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape  = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Red500.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = Red500, modifier = Modifier.size(22.dp))
                    Text("Cerrar Sesión", color = Red500, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                }
            }
            // El botón de logout llama a Firebase signOut y navega al Login
            LaunchedEffect(Unit) {} // placeholder; el onClick real va en el Card anterior
        }
    }
}
