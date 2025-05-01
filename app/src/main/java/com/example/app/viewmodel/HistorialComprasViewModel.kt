package com.example.app.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.api.RetrofitClient
import com.example.app.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistorialComprasViewModel(
    private val application: android.app.Application
) : ViewModel() {
    private val TAG = "HistorialComprasViewModel"
    
    // Estado para las compras
    private val _compras = MutableStateFlow<List<CompraItem>>(emptyList())
    val compras = _compras.asStateFlow()
    
    // Estados de UI
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    
    // Estado para la descarga de factura
    private val _isDownloadingPdf = MutableStateFlow(false)
    val isDownloadingPdf = _isDownloadingPdf.asStateFlow()
    
    // Mensaje de estado de la descarga
    private val _downloadMessage = MutableStateFlow<String?>(null)
    val downloadMessage = _downloadMessage.asStateFlow()
    
    // Navegación al login si la sesión expira
    private val _shouldNavigateToLogin = MutableStateFlow(false)
    val shouldNavigateToLogin = _shouldNavigateToLogin.asStateFlow()
    
    // Inicializar cargando el historial de compras
    init {
        loadHistorialCompras()
    }
    
    fun loadHistorialCompras() {
        viewModelScope.launch {
            try {
                isLoading = true
                clearError()
                
                // Obtener token
                val token = SessionManager.getToken()
                Log.d(TAG, "Token obtenido del SessionManager: $token")
                
                if (token.isNullOrEmpty()) {
                    Log.e(TAG, "Error: No hay token disponible")
                    setError("No se ha iniciado sesión")
                    _shouldNavigateToLogin.value = true
                    return@launch
                }
                
                // Realizar la petición para obtener el historial de compras
                val response = withContext(Dispatchers.IO) {
                    try {
                        RetrofitClient.apiService.getHistorialCompras("Bearer $token")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al realizar petición HTTP: ${e.message}", e)
                        return@withContext null
                    }
                }
                
                // Procesar la respuesta
                if (response == null) {
                    setError("Error de conexión al servidor")
                    return@launch
                }
                
                if (response.isSuccessful) {
                    val historialResponse = response.body()
                    Log.d(TAG, "Respuesta exitosa: ${historialResponse?.message}")
                    
                    if (historialResponse != null && historialResponse.status == "success") {
                        _compras.value = historialResponse.compras ?: emptyList()
                        Log.d(TAG, "Compras cargadas: ${_compras.value.size}")
                    } else {
                        Log.e(TAG, "Respuesta vacía o sin datos")
                        setError("No se pudieron obtener las compras")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Error ${response.code()}: $errorBody")
                    
                    when (response.code()) {
                        401 -> {
                            setError("Tu sesión ha expirado. Por favor, inicia sesión nuevamente")
                            _shouldNavigateToLogin.value = true
                        }
                        403 -> setError("No tienes permiso para acceder a esta información")
                        404 -> setError("No se encontraron compras")
                        500 -> setError("Error interno del servidor")
                        else -> setError("Error al cargar el historial: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en loadHistorialCompras: ${e.message}", e)
                setError("Error de conexión: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
    
    fun resetShouldNavigateToLogin() {
        _shouldNavigateToLogin.value = false
    }
    
    private fun setError(message: String) {
        errorMessage = message
    }
    
    private fun clearError() {
        errorMessage = null
    }
    
    @RequiresApi(Build.VERSION_CODES.Q)
    fun downloadFactura(idCompra: Int) {
        viewModelScope.launch {
            try {
                _isDownloadingPdf.value = true
                _downloadMessage.value = null
                
                val token = SessionManager.getToken()
                if (token.isNullOrEmpty()) {
                    _downloadMessage.value = "No se ha iniciado sesión"
                    _shouldNavigateToLogin.value = true
                    return@launch
                }
                
                val response = withContext(Dispatchers.IO) {
                    try {
                        RetrofitClient.apiService.downloadFactura("Bearer $token", idCompra)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al realizar petición HTTP: ${e.message}", e)
                        return@withContext null
                    }
                }
                
                if (response == null) {
                    _downloadMessage.value = "Error de conexión al servidor"
                    return@launch
                }
                
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        try {
                            // Verificar el tipo de contenido
                            val contentType = response.headers()["Content-Type"]
                            if (contentType?.contains("application/pdf") != true) {
                                _downloadMessage.value = "Error: El servidor no devolvió un PDF válido"
                                return@launch
                            }
                            
                            // Obtener el nombre del archivo del header Content-Disposition si existe
                            val contentDisposition = response.headers()["Content-Disposition"]
                            val fileName = contentDisposition?.let {
                                it.split("filename=").getOrNull(1)?.trim('"')
                            } ?: "factura_$idCompra.pdf"
                            
                            // Usar el contexto de la aplicación
                            val context = application.applicationContext
                            
                            // Crear el directorio de descargas si no existe
                            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                                android.os.Environment.DIRECTORY_DOWNLOADS
                            )
                            downloadsDir.mkdirs()
                            
                            // Crear el archivo
                            val file = java.io.File(downloadsDir, fileName)
                            
                            // Escribir el contenido
                            withContext(Dispatchers.IO) {
                                try {
                                    file.outputStream().use { fileOut ->
                                        responseBody.byteStream().use { inputStream ->
                                            val buffer = ByteArray(8192)
                                            var bytesRead: Int
                                            var totalBytes: Long = 0
                                            
                                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                                fileOut.write(buffer, 0, bytesRead)
                                                totalBytes += bytesRead
                                            }
                                            
                                            // Verificar que se escribieron bytes
                                            if (totalBytes == 0L) {
                                                throw Exception("El archivo descargado está vacío")
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Si hay un error, eliminar el archivo si existe
                                    file.delete()
                                    throw e
                                }
                            }
                            
                            // Notificar al sistema del nuevo archivo
                            val contentValues = android.content.ContentValues().apply {
                                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                            }
                            
                            context.contentResolver.insert(
                                android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                                contentValues
                            )
                            
                            // Notificar al usuario
                            _downloadMessage.value = "Factura descargada correctamente"
                            
                            // Abrir el PDF automáticamente
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                setDataAndType(android.net.Uri.fromFile(file), "application/pdf")
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                            
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al guardar el archivo: ${e.message}", e)
                            _downloadMessage.value = "Error al guardar la factura: ${e.message}"
                        }
                    } else {
                        _downloadMessage.value = "Error: Respuesta vacía del servidor"
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Error en la respuesta: $errorBody")
                    when (response.code()) {
                        401 -> {
                            _downloadMessage.value = "Sesión expirada"
                            _shouldNavigateToLogin.value = true
                        }
                        403 -> _downloadMessage.value = "Sin permiso"
                        404 -> _downloadMessage.value = "Factura no encontrada"
                        500 -> _downloadMessage.value = "Error del servidor"
                        else -> _downloadMessage.value = "Error: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al descargar factura: ${e.message}", e)
                _downloadMessage.value = "Error al descargar"
            } finally {
                _isDownloadingPdf.value = false
            }
        }
    }
}

// Modelos de datos para el historial de compras
data class CompraItem(
    val id_compra: Int = 0,
    val evento: EventoCompra? = null,
    val entradas: List<EntradaCompra> = emptyList(),
    val total: Double = 0.0,
    val fecha_compra: String = "",
    val estado: String = ""
)

data class EventoCompra(
    val id: Int = 0,
    val nombre: String = "",
    val fecha: String = "",
    val hora: String = "",
    val imagen: String? = null
)

data class EntradaCompra(
    val id: Int = 0,
    val precio: Double = 0.0,
    val estado: String = "",
    val nombre_persona: String = "",
    val tipo_entrada: String = ""
) 