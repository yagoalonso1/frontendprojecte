package com.example.app.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.api.RetrofitClient
import com.example.app.model.Evento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventoDetailViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    // Obtener el ID del evento de los argumentos de navegación
    private val eventoId: Int = checkNotNull(savedStateHandle["eventoId"])
    
    // Estado para el evento
    var evento by mutableStateOf<Evento?>(null)
        private set
    
    // Estado para indicar carga
    var isLoading by mutableStateOf(false)
        private set
    
    // Estado para errores
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isError by mutableStateOf(false)
        private set
    
    // Cargar evento al iniciar
    init {
        loadEvento()
    }
    
    // Función para cargar el evento
    fun loadEvento() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null
                isError = false
                
                Log.d("EVENTO_DETAIL", "Cargando evento con ID: $eventoId")
                
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getEventoById(eventoId)
                }
                
                if (response.isSuccessful) {
                    val eventoResponse = response.body()
                    if (eventoResponse != null) {
                        evento = eventoResponse.evento
                        Log.d("EVENTO_DETAIL", "Evento cargado: ${evento?.titulo}")
                    } else {
                        setError("No se pudo cargar el evento: Respuesta vacía")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("EVENTO_DETAIL", "Error: ${response.code()} - $errorBody")
                    
                    try {
                        val errorResponse = com.google.gson.Gson().fromJson(errorBody, Map::class.java)
                        val message = errorResponse?.get("message") as? String ?: errorResponse?.get("error") as? String
                        setError(message ?: "No se pudo cargar el evento (${response.code()})")
                    } catch (e: Exception) {
                        setError("No se pudo cargar el evento: ${response.message()} (${response.code()})")
                    }
                }
                
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                Log.e("EVENTO_DETAIL", "Excepción: ${e.message}", e)
                setError("Error de conexión: ${e.message}")
            }
        }
    }
    
    // Función para establecer error
    private fun setError(error: String?) {
        isError = error != null
        errorMessage = error
        Log.e("EVENTO_DETAIL", "ERROR: $error")
    }
} 