package com.cean.firebase.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cean.firebase.model.Tarea
import com.cean.firebase.viewmodel.TareasState
import com.cean.firebase.viewmodel.TareasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasScreen(viewModel: TareasViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var tareaSeleccionada by remember { mutableStateOf<Tarea?>(null) }

    // Manejo de eventos (Snackbar)
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestor de Tareas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                tareaSeleccionada = null
                mostrarDialogo = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar Tarea")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is TareasState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is TareasState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is TareasState.Success -> {
                    if (state.tareas.isEmpty()) {
                        Text("No hay tareas. ¡Agrega una!", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.tareas, key = { it.id }) { tarea ->
                                TareaItem(
                                    tarea = tarea,
                                    onEditar = {
                                        tareaSeleccionada = tarea
                                        mostrarDialogo = true
                                    },
                                    onEliminar = { viewModel.eliminarTarea(tarea.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (mostrarDialogo) {
            TareaDialog(
                tareaInicial = tareaSeleccionada,
                onDismiss = { mostrarDialogo = false },
                onGuardar = { titulo, descripcion ->
                    if (tareaSeleccionada == null) {
                        viewModel.agregarTarea(titulo, descripcion)
                    } else {
                        viewModel.actualizarTarea(tareaSeleccionada!!.copy(titulo = titulo, descripcion = descripcion))
                    }
                    mostrarDialogo = false
                }
            )
        }
    }
}

@Composable
fun TareaItem(
    tarea: Tarea,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tarea.titulo, style = MaterialTheme.typography.titleMedium)
                if (tarea.descripcion.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = tarea.descripcion, style = MaterialTheme.typography.bodyMedium)
                }
            }
            IconButton(onClick = onEditar) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun TareaDialog(
    tareaInicial: Tarea?,
    onDismiss: () -> Unit,
    onGuardar: (String, String) -> Unit
) {
    var titulo by remember { mutableStateOf(tareaInicial?.titulo ?: "") }
    var descripcion by remember { mutableStateOf(tareaInicial?.descripcion ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (tareaInicial == null) "Nueva Tarea" else "Editar Tarea") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onGuardar(titulo, descripcion) },
                enabled = titulo.isNotBlank() // Validación básica
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}