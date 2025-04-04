package com.example.app.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.api.RetrofitClient
import com.example.app.model.tickets.TicketCompra
import com.example.app.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TicketsViewModel : ViewModel() {
    // Estados para la vista
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var ticketsList by mutableStateOf<List<TicketCompra>>(emptyList())
    
    // Cargar tickets al iniciar
    init {
        loadTickets()
    }
    
    // Función para cargar tickets
    fun loadTickets() {
        viewModelScope.launch {
            try {
                isLoading = true
                clearError()
                
                // Obtener token de sesión
                val token = SessionManager.getToken()
                if (token.isNullOrEmpty()) {
                    setError("No se ha iniciado sesión")
                    return@launch
                }
                
                Log.d("TicketsViewModel", "Solicitando tickets con token: ${token.take(10)}...")
                
                // Hacer la petición
                val response = withContext(Dispatchers.IO) {
                    try {
                        RetrofitClient.apiService.getMisTickets("Bearer $token")
                    } catch (e: Exception) {
                        Log.e("TicketsViewModel", "Error al realizar petición HTTP: ${e.message}", e)
                        return@withContext null
                    }
                }
                
                // Procesar respuesta
                if (response == null) {
                    setError("Error de conexión al servidor")
                    return@launch
                }
                
                if (response.isSuccessful) {
                    val ticketsResponse = response.body()
                    Log.d("TicketsViewModel", "Respuesta exitosa: ${ticketsResponse?.message}")
                    
                    if (ticketsResponse != null && ticketsResponse.compras.isNotEmpty()) {
                        ticketsList = ticketsResponse.compras
                        Log.d("TicketsViewModel", "Se obtuvieron ${ticketsList.size} tickets")
                    } else {
                        ticketsList = emptyList()
                        Log.d("TicketsViewModel", "No se encontraron tickets")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("TicketsViewModel", "Error ${response.code()}: $errorBody")
                    
                    when (response.code()) {
                        401 -> setError("Tu sesión ha expirado. Por favor, inicia sesión nuevamente")
                        403 -> setError("No tienes permiso para acceder a esta información")
                        404 -> setError("No se encontraron tickets")
                        500 -> setError("Error interno del servidor")
                        else -> setError("Error al cargar los tickets: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("TicketsViewModel", "Error al cargar tickets: ${e.message}", e)
                setError("Error de conexión: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
    
    // Función para gestionar errores
    private fun setError(message: String) {
        errorMessage = message
    }
    
    private fun clearError() {
        errorMessage = null
    }
} 