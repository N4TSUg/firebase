package com.cean.firebase.repository

import com.cean.firebase.model.Tarea
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TareaRepository {

    private val database = FirebaseDatabase.getInstance().getReference("tareas")

    // Leer: Sincronización en tiempo real
    fun obtenerTareas(): Flow<List<Tarea>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tareas = snapshot.children.mapNotNull { it.getValue(Tarea::class.java) }
                trySend(tareas).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        database.addValueEventListener(listener)
        awaitClose { database.removeEventListener(listener) }
    }

    // Crear
    suspend fun agregarTarea(titulo: String, descripcion: String) {
        val nuevaRef = database.push()
        val id = nuevaRef.key ?: return
        val tarea = Tarea(id = id, titulo = titulo, descripcion = descripcion)
        nuevaRef.setValue(tarea).await()
    }

    // Actualizar
    suspend fun actualizarTarea(tarea: Tarea) {
        database.child(tarea.id).setValue(tarea).await()
    }

    // Eliminar
    suspend fun eliminarTarea(id: String) {
        database.child(id).removeValue().await()
    }
}