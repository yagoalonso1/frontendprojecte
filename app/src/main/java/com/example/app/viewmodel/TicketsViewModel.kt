package com.example.app.viewmodel

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import android.app.Activity
import androidx.activity.ComponentActivity

class TicketsViewModel(
    private val application: android.app.Application
) : ViewModel() {
    private val TAG = "TicketsViewModel"
    
    // Estados para la descarga de PDF
    private val _isDownloadingPdf = MutableStateFlow(false)
    val isDownloadingPdf: StateFlow<Boolean> = _isDownloadingPdf.asStateFlow()
    
    private val _downloadMessage = MutableStateFlow<String?>(null)
    val downloadMessage: StateFlow<String?> = _downloadMessage.asStateFlow()
    
    // Estado para los tickets
    private val _tickets = MutableStateFlow<List<TicketCompra>>(emptyList())
    val tickets: StateFlow<List<TicketCompra>> = _tickets.asStateFlow()
    
    // Estado de carga
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Estado de error
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    var calendarHelper: GoogleCalendarHelper? = null
    var googleAccount: GoogleSignInAccount? = null
    
    // Cargar tickets al iniciar
    init {
        loadTickets()
        calendarHelper = GoogleCalendarHelper(application)
    }
    
    // Función para añadir evento al calendario desde la Activity
    fun addEventToCalendar(ticket: TicketCompra, activity: ComponentActivity) {
        try {
            // Usar el helper actualizado que maneja todos los casos y errores
            calendarHelper?.addEventToCalendarFromActivity(activity, ticket)
            
            // No necesitamos setear un mensaje de éxito aquí porque el helper ya muestra un Toast
        } catch (e: Exception) {
            Log.e("TicketsViewModel", "Error al añadir evento al calendario: ${e.message}", e)
            setError("Error al añadir evento al calendario: ${e.message}")
        }
    }
    
    // Función simplificada para compatibilidad con código existente
    fun addEventToCalendar(ticket: TicketCompra) {
        // Ya no usamos coroutines aquí porque el método helper lo maneja internamente
        try {
            // Simplemente usamos el método para contextos no-Activity
            calendarHelper?.addEventToCalendarFromAnyContext(ticket)
        } catch (e: Exception) {
            Log.e("TicketsViewModel", "Error al añadir evento al calendario: ${e.message}", e)
            setError("Error al añadir evento al calendario: ${e.message}")
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
                _isLoading.value = true
                _error.value = null
                
                val token = SessionManager.getToken()
                if (token.isNullOrEmpty()) {
                    _error.value = "No hay sesión activa"
                    return@launch
                }
                
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getMisTickets("Bearer $token")
                }
                
                if (response.isSuccessful) {
                    val ticketsResponse = response.body()
                    if (ticketsResponse?.status == "success") {
                        _tickets.value = ticketsResponse.compras
                    } else {
                        _error.value = ticketsResponse?.message ?: "Error al cargar los tickets"
                    }
                } else {
                    _error.value = "Error al cargar los tickets: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar tickets", e)
                _error.value = "Error al cargar los tickets: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Función para establecer error
    fun setError(errorMessage: String) {
        _error.value = errorMessage
        viewModelScope.launch {
            kotlinx.coroutines.delay(5000)
            _error.value = null
        }
    }
    
    // Función para limpiar el mensaje de error
    private fun clearError() {
        _error.value = null
    }

    fun downloadEntrada(ticketId: Int) {
        viewModelScope.launch {
            try {
                _isDownloadingPdf.value = true
                _downloadMessage.value = "Iniciando descarga..."
                
                val token = SessionManager.getToken()
                if (token.isNullOrEmpty()) {
                    _downloadMessage.value = "No hay sesión activa"
                    return@launch
                }
                
                _downloadMessage.value = "Conectando con el servidor..."
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.downloadEntrada("Bearer $token", ticketId)
                }
                
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        _downloadMessage.value = "Descargando entrada..."
                        
                        // Verificar el tipo de contenido
                        val contentType = response.headers()["Content-Type"]
                        if (contentType?.contains("application/pdf") != true) {
                            _downloadMessage.value = "Error: El servidor no devolvió un PDF válido"
                            return@launch
                        }
                        
                        // Guardar el PDF en el almacenamiento
                        val fileName = "entrada_$ticketId.pdf"
                        val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                            android.os.Environment.DIRECTORY_DOWNLOADS
                        )
                        downloadsDir.mkdirs()
                        
                        val file = java.io.File(downloadsDir, fileName)
                        
                        try {
                            withContext(Dispatchers.IO) {
                                file.outputStream().use { outputStream ->
                                    responseBody.byteStream().use { inputStream ->
                                        val buffer = ByteArray(4096)
                                        var bytesRead: Int
                                        var totalBytesRead: Long = 0
                                        val contentLength = responseBody.contentLength()
                                        
                                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                            outputStream.write(buffer, 0, bytesRead)
                                            totalBytesRead += bytesRead
                                            
                                            // Actualizar progreso
                                            if (contentLength > 0) {
                                                val progress = (totalBytesRead * 100 / contentLength).toInt()
                                                _downloadMessage.value = "Descargando... $progress%"
                                            }
                                        }
                                    }
                                }
                                
                                // Verificar que el archivo se creó correctamente
                                if (!file.exists() || file.length() == 0L) {
                                    throw Exception("Error al guardar el archivo")
                                }
                            }
                            
                            // Notificar al sistema del nuevo archivo
                            val contentValues = android.content.ContentValues().apply {
                                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                                put(MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                            }
                            
                            val contentUri = MediaStore.Files.getContentUri("external")
                            application.contentResolver.insert(contentUri, contentValues)
                            
                            // Abrir el PDF automáticamente
                            try {
                                val fileUri = androidx.core.content.FileProvider.getUriForFile(
                                    application,
                                    "${application.packageName}.provider",
                                    file
                                )
                                
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(fileUri, "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                
                                // Verificar si hay alguna aplicación que pueda abrir PDFs
                                val packageManager = application.packageManager
                                if (intent.resolveActivity(packageManager) != null) {
                                    application.startActivity(intent)
                                } else {
                                    _downloadMessage.value = "¡Entrada descargada! Instala un lector de PDF para verla"
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error al abrir el PDF", e)
                                _downloadMessage.value = "¡Entrada descargada! No se pudo abrir automáticamente"
                            }
                            
                            _downloadMessage.value = "¡Entrada descargada correctamente!"
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al guardar el archivo", e)
                            _downloadMessage.value = "Error al guardar la entrada: ${e.message}"
                            // Intentar eliminar el archivo si se creó
                            file.delete()
                        }
                    } else {
                        _downloadMessage.value = "Error: No se recibieron datos del servidor"
                    }
                } else {
                    when (response.code()) {
                        401 -> _downloadMessage.value = "Sesión expirada. Por favor, vuelve a iniciar sesión"
                        403 -> _downloadMessage.value = "No tienes permiso para descargar esta entrada"
                        404 -> _downloadMessage.value = "Entrada no encontrada"
                        else -> _downloadMessage.value = "Error del servidor: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al descargar entrada", e)
                _downloadMessage.value = "Error de conexión: ${e.message}"
            } finally {
                _isDownloadingPdf.value = false
            }
        }
    }
} 