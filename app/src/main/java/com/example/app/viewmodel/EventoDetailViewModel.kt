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
    // ID evento
    private val eventoId: String = checkNotNull(savedStateHandle["eventoId"])
    
    // Evento
    var evento by mutableStateOf<Evento?>(null)
        private set
    
    // UI
    var isLoading by mutableStateOf(true)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isError by mutableStateOf(false)
        private set
    
    init {
        Log.d("EventoDetailViewModel", "Inicializando con eventoId: $eventoId")
        loadEvento()
    }
    
    fun loadEvento() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null
                isError = false
                
                Log.d("EventoDetailViewModel", "Cargando evento con ID: $eventoId")
                
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getEventoById(eventoId)
                }
                
                if (response.isSuccessful && response.body() != null) {
                    evento = response.body()?.evento
                    Log.d("EventoDetailViewModel", "Evento cargado exitosamente: ${evento?.titulo}")
                } else {
                    val error = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("EventoDetailViewModel", "Error al cargar evento: $error")
                    setError("No se pudo cargar el evento: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("EventoDetailViewModel", "Excepción al cargar evento", e)
                setError("Error de conexión: ${e.localizedMessage}")
            } finally {
                isLoading = false
            }
        }
    }
    
    private fun setError(error: String) {
        errorMessage = error
        isError = true
        Log.e("EventoDetailViewModel", "Error establecido: $error")
    }
} 