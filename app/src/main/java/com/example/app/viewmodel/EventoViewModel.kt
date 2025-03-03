package com.example.app.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.api.RetrofitClient
import com.example.app.model.Evento
import com.example.app.model.evento.EventoResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class EventoViewModel : ViewModel() {
    // Estado para la lista de eventos
    var eventos by mutableStateOf<List<Evento>>(emptyList())
        private set
    
    // Estado para indicar carga
    var isLoading by mutableStateOf(false)
        private set
    
    // Estado para errores
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isError by mutableStateOf(false)
        private set
    
    // Cargar eventos al iniciar
    init {
        loadEventos()
    }
    
    // Función para cargar eventos
    fun loadEventos() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null
                isError = false
                
                Log.d("EVENTOS", "Cargando eventos...")
                
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getAllEventos()
                }
                
                if (response.isSuccessful) {
                    val eventosResponse = response.body()
                    if (eventosResponse != null) {
                        eventos = eventosResponse.eventos
                        Log.d("EVENTOS", "Eventos cargados: ${eventos.size}")
                    } else {
                        setError("No se pudieron cargar los eventos: Respuesta vacía")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("EVENTOS", "Error: ${response.code()} - $errorBody")
                    
                    try {
                        val errorResponse = com.google.gson.Gson().fromJson(errorBody, Map::class.java)
                        val message = errorResponse?.get("message") as? String ?: errorResponse?.get("error") as? String
                        setError(message ?: "No se pudieron cargar los eventos (${response.code()})")
                    } catch (e: Exception) {
                        setError("No se pudieron cargar los eventos: ${response.message()} (${response.code()})")
                    }
                }
                
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                Log.e("EVENTOS", "Excepción: ${e.message}", e)
                setError("Error de conexión: ${e.message}")
            }
        }
    }
    
    // Función para establecer error
    private fun setError(error: String?) {
        isError = error != null
        errorMessage = error
        Log.e("EVENTOS", "ERROR: $error")
    }
    
    // Función para parsear la fecha
    private fun parseDate(dateString: String): LocalDate {
        return try {
            LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } catch (e: Exception) {
            // Si hay un error al parsear, devolvemos una fecha muy lejana para que aparezca al final
            LocalDate.of(9999, 12, 31)
        }
    }
    
    // Función para parsear la hora
    private fun parseTime(timeString: String): LocalTime {
        return try {
            LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm:ss"))
        } catch (e: Exception) {
            // Si hay un error al parsear, devolvemos una hora muy tardía para que aparezca al final
            LocalTime.of(23, 59, 59)
        }
    }
} 