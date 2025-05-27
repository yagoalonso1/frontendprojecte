package com.example.app.util

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.app.model.tickets.TicketCompra
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class GoogleCalendarHelper(private val context: Context) {
    private val SCOPES = Collections.singleton(CalendarScopes.CALENDAR)

    suspend fun getCalendarService(account: GoogleSignInAccount): Calendar? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("GoogleCalendarHelper", "Iniciando la creación del servicio de Calendar para cuenta: ${account.email}")
                
                if (account.account == null) {
                    Log.e("GoogleCalendarHelper", "La cuenta no tiene un objeto Account válido")
                    return@withContext null
                }
                
                val credential = GoogleAccountCredential.usingOAuth2(
                    context,
                    SCOPES
                ).setSelectedAccount(account.account)

                Log.d("GoogleCalendarHelper", "Credencial obtenida, construyendo servicio Calendar")
                
                val calendarService = Calendar.Builder(
                    NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
                )
                    .setApplicationName("Tickets App")
                    .build()
                
                Log.d("GoogleCalendarHelper", "Servicio Calendar construido exitosamente")
                
                calendarService
            } catch (e: Exception) {
                Log.e("GoogleCalendarHelper", "Error al crear servicio Calendar: ${e.message}", e)
                null
            }
        }
    }
    
    /**
     * Método alternativo para añadir eventos al calendario usando ContentResolver
     * Este método no requiere la API de Google Calendar
     */
    suspend fun addEventToCalendarUsingIntent(ticket: TicketCompra): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("GoogleCalendarHelper", "Usando método alternativo para añadir evento al calendario")
                
                // Parsear la fecha y hora
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                
                val startDate = dateFormat.parse(ticket.evento.fecha) ?: Date()
                val startTime = timeFormat.parse(ticket.evento.hora) ?: Date()
                
                // Combinar fecha y hora
                val calendar = java.util.Calendar.getInstance()
                calendar.time = startDate
                val timeCalendar = java.util.Calendar.getInstance()
                timeCalendar.time = startTime
                calendar.set(java.util.Calendar.HOUR_OF_DAY, timeCalendar.get(java.util.Calendar.HOUR_OF_DAY))
                calendar.set(java.util.Calendar.MINUTE, timeCalendar.get(java.util.Calendar.MINUTE))
                calendar.set(java.util.Calendar.SECOND, timeCalendar.get(java.util.Calendar.SECOND))
                
                // Hora de inicio
                val beginTime = calendar.timeInMillis
                
                // Hora de fin (2 horas después)
                calendar.add(java.util.Calendar.HOUR_OF_DAY, 2)
                val endTime = calendar.timeInMillis
                
                // Crear intent para añadir evento
                val intent = Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime)
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
                    .putExtra(CalendarContract.Events.TITLE, ticket.evento.nombre)
                    .putExtra(CalendarContract.Events.DESCRIPTION, "Entrada para ${ticket.evento.nombre}")
                    .putExtra(CalendarContract.Events.EVENT_LOCATION, "Ubicación del evento")
                    .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                
                // Este intent debe ser lanzado desde la UI
                withContext(Dispatchers.Main) {
                    context.startActivity(intent)
                }
                
                Log.d("GoogleCalendarHelper", "Intent para añadir evento lanzado correctamente")
                return@withContext true
            } catch (e: Exception) {
                Log.e("GoogleCalendarHelper", "Error al añadir evento con intent: ${e.message}", e)
                return@withContext false
            }
        }
    }

    suspend fun addEventToGoogleCalendar(
        account: GoogleSignInAccount,
        ticket: TicketCompra
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val calendarService = getCalendarService(account)
                if (calendarService == null) {
                    Log.e("GoogleCalendarHelper", "No se pudo obtener el servicio de Calendar")
                    // Si falla la API, intentamos con el método alternativo
                    return@withContext addEventToCalendarUsingIntent(ticket)
                }

                // Parsear fecha y hora
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val startDate = dateFormat.parse(ticket.evento.fecha) ?: Date()
                val startTime = timeFormat.parse(ticket.evento.hora) ?: Date()

                // Combinar fecha y hora
                val calendar = java.util.Calendar.getInstance()
                calendar.time = startDate
                val timeCalendar = java.util.Calendar.getInstance()
                timeCalendar.time = startTime
                calendar.set(java.util.Calendar.HOUR_OF_DAY, timeCalendar.get(java.util.Calendar.HOUR_OF_DAY))
                calendar.set(java.util.Calendar.MINUTE, timeCalendar.get(java.util.Calendar.MINUTE))
                calendar.set(java.util.Calendar.SECOND, timeCalendar.get(java.util.Calendar.SECOND))

                val startMillis = calendar.timeInMillis
                calendar.add(java.util.Calendar.HOUR_OF_DAY, 2)
                val endMillis = calendar.timeInMillis

                // Crear evento
                val event = com.google.api.services.calendar.model.Event()
                    .setSummary(ticket.evento.nombre)
                    .setDescription("Entrada para ${ticket.evento.nombre}")
                    .setLocation("Ubicación del evento")

                val start = com.google.api.services.calendar.model.EventDateTime()
                    .setDateTime(com.google.api.client.util.DateTime(startMillis))
                    .setTimeZone(TimeZone.getDefault().id)
                val end = com.google.api.services.calendar.model.EventDateTime()
                    .setDateTime(com.google.api.client.util.DateTime(endMillis))
                    .setTimeZone(TimeZone.getDefault().id)

                event.start = start
                event.end = end

                try {
                    // Insertar evento en el calendario principal
                    calendarService.events().insert("primary", event).execute()
                    Log.d("GoogleCalendarHelper", "Evento añadido a Google Calendar correctamente")
                    return@withContext true
                } catch (e: Exception) {
                    Log.e("GoogleCalendarHelper", "Error al insertar evento: ${e.message}", e)
                    // Si falla la inserción, intentamos con el método alternativo
                    return@withContext addEventToCalendarUsingIntent(ticket)
                }
            } catch (e: Exception) {
                Log.e("GoogleCalendarHelper", "Error al añadir evento a Google Calendar: ${e.message}", e)
                // Si falla por cualquier motivo, intentamos con el método alternativo
                return@withContext addEventToCalendarUsingIntent(ticket)
            }
        }
    }
    
    /**
     * Método para facilitar la adición de eventos al calendario
     * Se puede llamar desde una Activity o Fragment
     */
    fun addEventToCalendarFromActivity(activity: ComponentActivity, ticket: TicketCompra) {
        // Obtener la última cuenta con la que el usuario ha iniciado sesión
        val account = GoogleSignIn.getLastSignedInAccount(context)
        
        activity.lifecycleScope.launch {
            try {
                val resultado: Boolean
                
                if (account != null) {
                    // El usuario tiene una cuenta de Google, intentamos con la API primero
                    resultado = addEventToGoogleCalendar(account, ticket)
                } else {
                    // Si no hay cuenta de Google, usamos el método alternativo con Intent
                    resultado = addEventToCalendarUsingIntent(ticket)
                }
                
                withContext(Dispatchers.Main) {
                    if (resultado) {
                        Toast.makeText(context, "Evento añadido al calendario", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error al añadir evento al calendario", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                // Si algo falla, mostramos el error
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al inicializar el servicio de calendario", Toast.LENGTH_SHORT).show()
                    Log.e("GoogleCalendarHelper", "Error general: ${e.message}", e)
                }
            }
        }
    }
    
    /**
     * Método para usar directamente desde un contexto que no sea una Activity
     * Siempre utiliza el método de Intent con la bandera NEW_TASK
     */
    fun addEventToCalendarFromAnyContext(ticket: TicketCompra) {
        kotlinx.coroutines.GlobalScope.launch {
            try {
                addEventToCalendarUsingIntent(ticket)
            } catch (e: Exception) {
                Log.e("GoogleCalendarHelper", "Error al añadir evento desde contexto no-Activity: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al añadir evento al calendario", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
} 