package com.Arcentales.eventhub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Arcentales.eventhub.data.models.Event
import com.Arcentales.eventhub.data.models.TicketType
import com.Arcentales.eventhub.ui.theme.Blue500
import com.Arcentales.eventhub.utils.FirestoreCollections
import com.Arcentales.eventhub.viewmodel.AdminViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEventEditScreen(
    eventId: String,
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var venueName by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Ticket Types State
    var ticketTypes by remember { mutableStateOf(listOf<TicketType>()) }
    var showTicketDialog by remember { mutableStateOf(false) }
    var editingTicketType by remember { mutableStateOf<TicketType?>(null) }

    LaunchedEffect(eventId) {
        if (eventId != "new") {
            isLoading = true
            try {
                val doc = db.collection(FirestoreCollections.EVENTS).document(eventId).get().await()
                val event = doc.toObject(Event::class.java)
                event?.let {
                    title = it.title
                    description = it.description
                    venueName = it.venueName
                    imageUrl = it.imageUrl
                    category = it.category
                }
                ticketTypes = viewModel.getTicketTypes(eventId)
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (eventId == "new") "Nuevo Evento" else "Editar Evento") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Blue500)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Información General", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                OutlinedTextField(value = venueName, onValueChange = { venueName = it }, label = { Text("Lugar / Venue") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("URL de Imagen") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Categoría (Concerts, Sports...)") }, modifier = Modifier.fillMaxWidth())
                
                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tipos de Entradas", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (eventId != "new") {
                        IconButton(onClick = { 
                            editingTicketType = null
                            showTicketDialog = true 
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Añadir Ticket", tint = Blue500)
                        }
                    } else {
                        Text("(Guarda primero para añadir tickets)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                ticketTypes.forEach { type ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(type.name, fontWeight = FontWeight.Bold)
                                Text("${type.price} ${type.currency} - Cap: ${type.capacity}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row {
                                IconButton(onClick = { 
                                    editingTicketType = type
                                    showTicketDialog = true 
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar Ticket", modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { 
                                    scope.launch {
                                        viewModel.deleteTicketType(eventId, type.id)
                                        ticketTypes = viewModel.getTicketTypes(eventId)
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Borrar Ticket", tint = Color.Red, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        scope.launch {
                            val id = if (eventId == "new") db.collection(FirestoreCollections.EVENTS).document().id else eventId
                            val event = Event(id = id, title = title, description = description, venueName = venueName, imageUrl = imageUrl, category = category)
                            db.collection(FirestoreCollections.EVENTS).document(id).set(event).await()
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue500)
                ) {
                    Text("Guardar Evento Completo", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showTicketDialog) {
        TicketTypeDialog(
            ticketType = editingTicketType,
            onDismiss = { showTicketDialog = false },
            onSave = { name, price, capacity ->
                scope.launch {
                    val newType = editingTicketType?.copy(name = name, price = price, capacity = capacity) 
                        ?: TicketType(name = name, price = price, capacity = capacity)
                    viewModel.saveTicketType(eventId, newType)
                    ticketTypes = viewModel.getTicketTypes(eventId)
                    showTicketDialog = false
                }
            }
        )
    }
}

@Composable
fun TicketTypeDialog(
    ticketType: TicketType?,
    onDismiss: () -> Unit,
    onSave: (String, Double, Int) -> Unit
) {
    var name by remember { mutableStateOf(ticketType?.name ?: "") }
    var price by remember { mutableStateOf(ticketType?.price?.toString() ?: "") }
    var capacity by remember { mutableStateOf(ticketType?.capacity?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (ticketType == null) "Nuevo Tipo de Entrada" else "Editar Entrada") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre (Ej: General, VIP)") })
                OutlinedTextField(
                    value = price, 
                    onValueChange = { price = it }, 
                    label = { Text("Precio") }, 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = capacity, 
                    onValueChange = { capacity = it }, 
                    label = { Text("Capacidad Total") }, 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { 
                val p = price.toDoubleOrNull() ?: 0.0
                val c = capacity.toIntOrNull() ?: 0
                onSave(name, p, c)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
