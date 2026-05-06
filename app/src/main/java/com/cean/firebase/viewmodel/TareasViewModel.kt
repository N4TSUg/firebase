package com.cean.firebase.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cean.firebase.model.Tarea
import com.cean.firebase.repository.TareaRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class TareasViewModel : ViewModel() {

    private val repository = TareaRepository()

    private val _uiState = MutableStateFlow<TareasState>(TareasState.Loading)
    val uiState: StateFlow<TareasState> = _uiState.asStateFlow()

    // Usamos SharedFlow para emitir eventos de UI de una sola vez (como Toast o Snackbar)
    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        cargarTareas()
    }

    private fun cargarTareas() {
        viewModelScope.launch {
            _uiState.value = TareasState.Loading
            repository.obtenerTareas()
                .catch { error ->
                    _uiState.value = TareasState.Error(error.message ?: "Error desconocido")
                }
                .collect { tareasLista ->
                    _uiState.value = TareasState.Success(tareasLista)
                }
        }
    }

    fun agregarTarea(titulo: String, descripcion: String) {
        viewModelScope.launch {
            try {
                repository.agregarTarea(titulo, descripcion)
                _uiEvent.emit("Tarea agregada correctamente")
            } catch (e: Exception) {
                _uiEvent.emit("Error al agregar tarea: ${e.message}")
            }
        }
    }

    fun actualizarTarea(tarea: Tarea) {
        viewModelScope.launch {
            try {
                repository.actualizarTarea(tarea)
                _uiEvent.emit("Tarea actualizada")
            } catch (e: Exception) {
                _uiEvent.emit("Error al actualizar: ${e.message}")
            }
        }
    }

    fun eliminarTarea(id: String) {
        viewModelScope.launch {
            try {
                repository.eliminarTarea(id)
                _uiEvent.emit("Tarea eliminada")
            } catch (e: Exception) {
                _uiEvent.emit("Error al eliminar: ${e.message}")
            }
        }
    }
}