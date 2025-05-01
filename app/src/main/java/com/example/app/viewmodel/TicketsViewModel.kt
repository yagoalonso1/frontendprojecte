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
            // Usar el método alternativo que usa intents en lugar de la API de Calendar
            val result = calendarHelper?.addEventToCalendarUsingIntent(ticket)
            
            if (result == true) {
                setSuccessMessage("Evento en curso de añadirse a tu calendario")
            } else {
                // Si falló, intentar con el método tradicional
                addEventToCalendarUsingAPI(ticket)
            }
        } catch (e: Exception) {
            Log.e("TicketsViewModel", "Error global: ${e.message}", e)
            setError("Error inesperado: ${e.message}")
        }
    }
    
    // Método original que usa la API de Google Calendar
    private suspend fun addEventToCalendarUsingAPI(ticket: TicketCompra) {
        // Todas las operaciones de Google Calendar deben ejecutarse en un hilo secundario
        withContext(Dispatchers.IO) {
            try {
                // Verificar si tenemos una cuenta Google
                if (googleAccount == null) {
                    // Intentar obtener la cuenta Google del dispositivo
                    googleAccount = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(
                        com.example.app.MyApplication.appContext
                    )
                }
                
                val account = googleAccount
                if (account == null) {
                    withContext(Dispatchers.Main) {
                        setError("No hay una cuenta de Google conectada. Por favor, inicie sesión con Google primero.")
                    }
                    return@withContext
                }

                Log.d("TicketsViewModel", "Añadiendo evento al calendario con cuenta: ${account.email}")
                
                // Verificar que la cuenta tenga los permisos necesarios
                try {
                    val calendarService = calendarHelper?.getCalendarService(account)
                    if (calendarService == null) {
                        withContext(Dispatchers.Main) {
                            setError("Error al inicializar el servicio de calendario")
                        }
                        return@withContext
                    }

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    
                    val startDate = dateFormat.parse(ticket.evento.fecha) ?: Date()
                    val startTime = timeFormat.parse(ticket.evento.hora) ?: Date()
                    
                    // Combinar fecha y hora
                    val calendar = Calendar.getInstance()
                    calendar.time = startDate
                    val timeCalendar = Calendar.getInstance()
                    timeCalendar.time = startTime
                    calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                    calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                    calendar.set(Calendar.SECOND, timeCalendar.get(Calendar.SECOND))
                    
                    // Calcular la hora de fin (2 horas después)
                    val endCalendar = Calendar.getInstance()
                    endCalendar.timeInMillis = calendar.timeInMillis
                    endCalendar.add(Calendar.HOUR_OF_DAY, 2)
                    
                    Log.d("TicketsViewModel", "Creando evento: ${ticket.evento.nombre} - Fecha: ${dateFormat.format(calendar.time)} ${timeFormat.format(calendar.time)}")

                    val event = Event()
                        .setSummary(ticket.evento.nombre)
                        .setDescription("Entrada para ${ticket.evento.nombre}")
                        .setLocation("Ubicación del evento")
                        .setStart(EventDateTime().setDateTime(com.google.api.client.util.DateTime(calendar.time)))
                        .setEnd(EventDateTime().setDateTime(com.google.api.client.util.DateTime(endCalendar.time)))
                    
                    // Agregar recordatorios
                    val reminderOverrides = listOf(
                        com.google.api.services.calendar.model.EventReminder().setMethod("email").setMinutes(24 * 60), // 1 día antes
                        com.google.api.services.calendar.model.EventReminder().setMethod("popup").setMinutes(60) // 1 hora antes
                    )
                    val reminders = com.google.api.services.calendar.model.Event.Reminders()
                        .setUseDefault(false)
                        .setOverrides(reminderOverrides)
                    event.reminders = reminders

                    // Ejecutar en un contexto de IO para evitar bloquear el hilo principal
                    val createdEvent = calendarService.events()
                        .insert("primary", event)
                        .execute()

                    Log.d("TicketsViewModel", "Evento creado: ${createdEvent.htmlLink}")
                    withContext(Dispatchers.Main) {
                        setSuccessMessage("Evento '${ticket.evento.nombre}' añadido a tu Google Calendar")
                    }
                } catch (e: Exception) {
                    Log.e("TicketsViewModel", "Error específico en la API de Calendar: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        if (e.message?.contains("DEAD_OBJECT") == true || e.message?.contains("DeadObject") == true) {
                            setError("Error de conexión con Google Calendar. Intente nuevamente iniciando sesión con Google.")
                        } else if (e.message?.contains("PERMISSION_DENIED") == true) {
                            setError("Permiso denegado. Verifica que hayas iniciado sesión con tu cuenta Google.")
                        } else if (e.message?.contains("NetworkError") == true || e.message?.contains("Failed to connect") == true) {
                            setError("Error de red. Verifica tu conexión a internet.")
                        } else if (e.message?.contains("deadlock") == true || e.message?.contains("Calling this from your main thread") == true) {
                            setError("Error interno. Por favor, intenta de nuevo más tarde.")
                        } else {
                            setError("Error al añadir evento al calendario: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("TicketsViewModel", "Error general: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    setError("Error al añadir evento al calendario: ${e.message}")
                }
            }
        }
    }
    
    // Agregar un mensaje de éxito
    private var _successMessage by mutableStateOf<String?>(null)
    val successMessage get() = _successMessage
    
    private fun setSuccessMessage(message: String) {
        _successMessage = message
        // Eliminamos el mensaje después de 3 segundos
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _successMessage = null
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
    
    // Función para establecer un mensaje de error
    fun setError(error: String) {
        errorMessage = error
        isLoading = false
        Log.e("TicketsViewModel", "Error: $error")
    }
    
    // Función para limpiar el mensaje de error
    private fun clearError() {
        errorMessage = null
    }
} 