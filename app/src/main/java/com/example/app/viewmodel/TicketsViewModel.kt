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
import com.example.app.util.GoogleCalendarHelper
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TicketsViewModel : ViewModel() {
    // Estados para la vista
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var ticketsList by mutableStateOf<List<TicketCompra>>(emptyList())
    var calendarHelper: GoogleCalendarHelper? = null
    var googleAccount: GoogleSignInAccount? = null
    
    // Cargar tickets al iniciar
    init {
        loadTickets()
    }
    
    // Función para añadir evento al calendario
    suspend fun addEventToCalendar(ticket: TicketCompra) {
        try {
            val account = googleAccount
            if (account == null) {
                setError("No hay una cuenta de Google conectada")
                return
            }

            val calendarService = calendarHelper?.getCalendarService(account)
            if (calendarService == null) {
                setError("Error al inicializar el servicio de calendario")
                return
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            
            val startDate = dateFormat.parse(ticket.evento.fecha)
            val startTime = timeFormat.parse(ticket.evento.hora)
            
            // Combinar fecha y hora
            val calendar = Calendar.getInstance()
            calendar.time = startDate
            val timeCalendar = Calendar.getInstance()
            timeCalendar.time = startTime
            calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
            calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
            calendar.set(Calendar.SECOND, timeCalendar.get(Calendar.SECOND))

            val event = Event()
                .setSummary(ticket.evento.nombre)
                .setDescription("Entrada para ${ticket.evento.nombre}")
                .setStart(EventDateTime().setDateTime(com.google.api.client.util.DateTime(calendar.time)))
                .setEnd(EventDateTime().setDateTime(com.google.api.client.util.DateTime(calendar.time)))

            val createdEvent = calendarService.events()
                .insert("primary", event)
                .execute()

            Log.d("TicketsViewModel", "Evento creado: ${createdEvent.htmlLink}")
        } catch (e: Exception) {
            Log.e("TicketsViewModel", "Error al añadir evento al calendario: ${e.message}", e)
            setError("Error al añadir evento al calendario: ${e.message}")
        }
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