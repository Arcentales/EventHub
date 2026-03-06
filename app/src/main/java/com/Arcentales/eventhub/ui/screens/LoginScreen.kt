package com.Arcentales.eventhub.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Arcentales.eventhub.R
import com.Arcentales.eventhub.ui.theme.*
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// LOGIN SCREEN
//
// Estructura visual (de arriba hacia abajo):
//   [Logo 96dp]
//   "EventHub"        ← headlineLarge
//   "Inicia sesión…"  ← bodyMedium
//   ┌─────────────────────────────────────┐
//   │  Correo electrónico  [OutlinedTF]   │
//   │  Contraseña          [OutlinedTF]   │
//   │              ¿Olvidaste tu…? [Btn]  │
//   │  [    Iniciar sesión / spinner   ]  │
//   │  ──────── o continua con ────────   │
//   │  [  G   Continuar con Google    ]   │
//   │  ¿No tienes cuenta?  [Registrate]   │
//   └─────────────────────────────────────┘
//
// Errores → Snackbar (no Text fijo rojo)
// Navegación → LaunchedEffect(state.isLoginSuccess)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    // ── Variables de estado locales ───────────────────────────────────────
    val state             = viewModel.uiState          // Estado del ViewModel
    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()   // Para lanzar Credential Manager
    val context           = LocalContext.current

    // passwordVisible es LOCAL porque solo afecta la visualización, no la lógica
    var passwordVisible by remember { mutableStateOf(false) }
    var isRegisterMode  by remember { mutableStateOf(false) }

    // ── Efectos secundarios (LaunchedEffect) ──────────────────────────────
    // LaunchedEffect(key): se ejecuta cuando el valor de `key` cambia.
    // Es la forma segura de ejecutar efectos en Compose (navegación, snackbars).

    // Cuando el login es exitoso → navegar al Home
    LaunchedEffect(state.isLoginSuccess) {
        if (state.isLoginSuccess) {
            viewModel.onLoginHandled()  // reset del flag para no disparar dos veces
            onLoginSuccess()
        }
    }

    // Cuando hay un error → mostrar Snackbar (mejor UX que un Text rojo fijo)
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { mensaje ->
            snackbarHostState.showSnackbar(
                message  = mensaje,
                duration = SnackbarDuration.Short
            )
        }
    }

    // ── Scaffold con SnackbarHost ─────────────────────────────────────────
    Scaffold(
        snackbarHost   = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent
    ) { scaffoldPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(listOf(Navy900, Navy800, Color(0xFF1a3a5f)))
                )
                .padding(scaffoldPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // verticalScroll: el teclado empuja el contenido sin cortar campos
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.height(64.dp))

                // ── Logo ──────────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(
                            Brush.linearGradient(listOf(Blue500, Cyan500)),
                            RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Reemplazar con Image(painterResource(R.drawable.isotipo))
                    // cuando agregues el logo en res/drawable/
                    Text("🎫", fontSize = 44.sp)
                }

                Spacer(Modifier.height(20.dp))

                // ── Título y subtítulo ────────────────────────────────────
                // Usar MaterialTheme.typography en vez de tamaños hardcodeados
                Text(
                    text       = "EventHub",
                    style      = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color      = Color.White,
                    letterSpacing = (-1).sp
                )
                Text(
                    text  = "Inicia sesión para continuar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(36.dp))

                // ── Card del formulario ───────────────────────────────────
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(24.dp),
                    colors    = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        Text(
                            text       = if (isRegisterMode) "Crear cuenta" else "Iniciar sesión",
                            color      = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 20.sp
                        )

                        // ── Campo Email ───────────────────────────────────
                        // onValueChange = viewModel::onEmailChange → State Hoisting correcto
                        // ImeAction.Next → botón "Siguiente" en el teclado
                        OutlinedTextField(
                            value           = state.email,
                            onValueChange   = viewModel::onEmailChange,
                            label           = { Text("Correo electrónico") },
                            leadingIcon     = { Icon(Icons.Default.Email, null) },
                            singleLine      = true,
                            enabled         = !state.isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction    = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(14.dp),
                            colors   = loginTextFieldColors()
                        )

                        // ── Campo Password ────────────────────────────────
                        // passwordVisible = estado LOCAL (solo afecta la vista)
                        // ImeAction.Done → botón "Listo" en el teclado
                        OutlinedTextField(
                            value           = state.password,
                            onValueChange   = viewModel::onPasswordChange,
                            label           = { Text("Contraseña") },
                            leadingIcon     = { Icon(Icons.Default.Lock, null) },
                            trailingIcon    = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector        = if (passwordVisible)
                                            Icons.Default.VisibilityOff
                                        else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible)
                                            "Ocultar contraseña"
                                        else "Mostrar contraseña",
                                        tint = Slate400
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible)
                                VisualTransformation.None
                            else PasswordVisualTransformation(),
                            singleLine      = true,
                            enabled         = !state.isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction    = ImeAction.Done
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(14.dp),
                            colors   = loginTextFieldColors()
                        )

                        // ── ¿Olvidaste tu contraseña? ─────────────────────
                        if (!isRegisterMode) {
                            Box(
                                modifier         = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                TextButton(onClick = { /* TODO: pantalla de recuperación */ }) {
                                    Text(
                                        "¿Olvidaste tu contraseña?",
                                        color    = Blue500,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        // ── Botón principal ───────────────────────────────
                        // Muestra CircularProgressIndicator durante la carga.
                        // enabled = !state.isLoading → evita doble click.
                        Button(
                            onClick  = {
                                if (isRegisterMode) viewModel.registerWithEmail()
                                else viewModel.loginWithEmail()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled  = !state.isLoading,
                            shape    = RoundedCornerShape(14.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = Blue500)
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(24.dp),
                                    color       = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    if (isRegisterMode) "Crear cuenta" else "Iniciar sesión",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // ── Divisor "o continua con" ──────────────────────
                        // Modifier.weight(1f) → las dos líneas ocupan el espacio
                        // restante equitativamente a los lados del texto.
                        Row(
                            modifier          = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Navy700)
                            Text(
                                text     = "o continua con",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style    = MaterialTheme.typography.bodySmall,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Navy700)
                        }

                        // ── Botón Google Sign-In (Credential Manager) ─────
                        // Credential Manager es la API MODERNA recomendada por Google.
                        // Reemplaza al viejo GoogleSignInClient (deprecated).
                        // Muestra un bottom sheet nativo del sistema para elegir cuenta.
                        OutlinedButton(
                            onClick  = {
                                scope.launch {
                                    try {
                                        // 1. Configurar la opción de Google ID Token
                                        val googleIdOption = GetGoogleIdOption.Builder()
                                            .setFilterByAuthorizedAccounts(false) // Mostrar TODAS las cuentas
                                            .setServerClientId(
                                                // default_web_client_id es generado automáticamente
                                                // por el plugin google-services a partir del
                                                // google-services.json. NO editar strings.xml.
                                                context.getString(R.string.default_web_client_id)
                                            )
                                            .build()

                                        // 2. Empaquetar en una solicitud
                                        val request = GetCredentialRequest.Builder()
                                            .addCredentialOption(googleIdOption)
                                            .build()

                                        // 3. Mostrar el bottom sheet del sistema
                                        val credentialManager = CredentialManager.create(context)
                                        val result = credentialManager.getCredential(
                                            context = context,
                                            request = request
                                        )

                                        // 4. Enviar la credencial al ViewModel para autenticar en Firebase
                                        viewModel.handleGoogleSignInResult(result.credential)

                                    } catch (e: GetCredentialCancellationException) {
                                        // El usuario cerró el selector → no hacer nada
                                    } catch (e: Exception) {
                                        viewModel.onGoogleSignInError(
                                            e.localizedMessage ?: "Error al iniciar con Google"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled  = !state.isLoading,
                            shape    = RoundedCornerShape(14.dp),
                            border   = androidx.compose.foundation.BorderStroke(1.dp, Navy700)
                        ) {
                            Text(
                                text       = "G",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize   = 16.sp,
                                color      = Color(0xFF4285F4)   // Azul oficial de Google
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text       = "Continuar con Google",
                                color      = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // ── Toggle Login / Registro ───────────────────────
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                text  = if (isRegisterMode) "¿Ya tienes cuenta?" else "¿No tienes cuenta?",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            TextButton(
                                onClick = {
                                    isRegisterMode = !isRegisterMode
                                    // onNavigateToRegister() ← usar esto si hay pantalla separada
                                }
                            ) {
                                Text(
                                    text       = if (isRegisterMode) "Inicia sesión" else "Regístrate",
                                    color      = Blue500,
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 13.sp
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HELPER — Colores reutilizables para los campos del formulario
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun loginTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor     = Blue500,
    unfocusedBorderColor   = Navy700,
    focusedLabelColor      = Blue500,
    unfocusedLabelColor    = Slate400,
    focusedTextColor       = Color.White,
    unfocusedTextColor     = Color.White,
    disabledTextColor      = Color.White.copy(alpha = 0.4f),
    focusedLeadingIconColor   = Blue500,
    unfocusedLeadingIconColor = Slate400
)