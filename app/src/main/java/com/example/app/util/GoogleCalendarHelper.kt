package com.example.app.util

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import com.example.app.model.tickets.TicketCompra
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class GoogleCalendarHelper(private val context: Context) {
    private val SCOPES = Collections.singleton(CalendarScopes.CALENDAR)

    suspend fun getCalendarService(account: GoogleSignInAccount): Calendar {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("GoogleCalendarHelper", "Iniciando la creación del servicio de Calendar para cuenta: ${account.email}")
                
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
                throw e
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
} 