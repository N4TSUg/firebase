package com.cean.firebase.viewmodel

import com.cean.firebase.model.Tarea

sealed class TareasState {
    object Loading : TareasState()
    data class Success(val tareas: List<Tarea>) : TareasState()
    data class Error(val message: String) : TareasState()
}