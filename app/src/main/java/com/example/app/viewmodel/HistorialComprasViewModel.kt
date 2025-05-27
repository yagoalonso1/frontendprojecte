package com.example.app.viewmodel

import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.R
import com.example.app.api.RetrofitClient
import com.example.app.util.SessionManager
import com.example.app.view.MENSAJE_FACTURA_DESCARGADA
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response

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
    
    fun resetShouldNavigateToLogin() {
        _shouldNavigateToLogin.value = false
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
                    setError(application.getString(R.string.favoritos_error_login))
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
                    setError(application.getString(R.string.historial_compras_error_servidor))
                    return@launch
                }
                
                if (response.isSuccessful) {
                    val historialResponse = response.body()
                    Log.d(TAG, "Respuesta del servidor: $historialResponse")
                    
                    if (historialResponse?.status?.equals("success", ignoreCase = true) == true) {
                        val comprasLista = historialResponse.compras ?: emptyList()
                        _compras.value = comprasLista
                        
                        if (comprasLista.isEmpty()) {
                            Log.d(TAG, "El historial de compras está vacío")
                        } else {
                            Log.d(TAG, "Se han cargado ${comprasLista.size} compras")
                        }
                    } else {
                        Log.e(TAG, "Error en la respuesta: ${historialResponse?.message}")
                        setError(historialResponse?.message ?: application.getString(R.string.historial_compras_error_servidor))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Error en la respuesta HTTP: ${response.code()}, $errorBody")
                    
                    when (response.code()) {
                        401 -> {
                            setError(application.getString(R.string.historial_compras_sesion_expirada))
                            _shouldNavigateToLogin.value = true
                        }
                        403 -> setError(application.getString(R.string.historial_compras_sin_permiso))
                        404 -> setError("No se encontró el historial")
                        500 -> setError(application.getString(R.string.historial_compras_error_servidor))
                        else -> setError("Error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar historial: ${e.message}")
                setError("Error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
    
    private fun setError(message: String) {
        Log.e(TAG, "Error: $message")
        errorMessage = message
    }
    
    private fun clearError() {
        errorMessage = null
    }
    
    fun downloadFactura(idCompra: Int) {
        viewModelScope.launch {
            try {
                _isDownloadingPdf.value = true
                _downloadMessage.value = application.getString(R.string.iniciando_descarga)
                
                val token = SessionManager.getToken()
                if (token.isNullOrEmpty()) {
                    _downloadMessage.value = application.getString(R.string.historial_compras_sesion_expirada)
                    _shouldNavigateToLogin.value = true
                    return@launch
                }
                
                _downloadMessage.value = application.getString(R.string.conectando_servidor)
                val response = withContext(Dispatchers.IO) {
                    try {
                        RetrofitClient.apiService.downloadFactura("Bearer $token", idCompra)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al realizar petición HTTP para descargar factura: ${e.message}", e)
                        return@withContext null
                    }
                }
                
                if (response == null) {
                    _downloadMessage.value = application.getString(R.string.historial_compras_error_descarga)
                    return@launch
                }
                
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        _downloadMessage.value = application.getString(R.string.descargando_factura)
                        
                        // Verificar el tipo de contenido
                        val contentType = response.headers()["Content-Type"]
                        if (contentType?.contains("application/pdf") != true) {
                            _downloadMessage.value = application.getString(R.string.error_no_pdf)
                            return@launch
                        }
                        
                        // Guardar el PDF en el almacenamiento
                        val fileName = "factura_$idCompra.pdf"
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
                                                _downloadMessage.value = "${application.getString(R.string.descargando)} $progress%"
                                            }
                                        }
                                    }
                                }
                                
                                // Verificar que el archivo se creó correctamente
                                if (!file.exists() || file.length() == 0L) {
                                    throw Exception(application.getString(R.string.error_guardar_archivo))
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
                                    _downloadMessage.value = application.getString(R.string.factura_descargada_instalar_lector)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error al abrir el PDF", e)
                                _downloadMessage.value = application.getString(R.string.factura_descargada_no_abrir)
                            }
                            
                            _downloadMessage.value = MENSAJE_FACTURA_DESCARGADA
                            Log.d(TAG, "Factura descargada correctamente")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al guardar el archivo", e)
                            _downloadMessage.value = "${application.getString(R.string.error_guardar_factura)}: ${e.message}"
                            // Intentar eliminar el archivo si se creó
                            file.delete()
                        }
                    } else {
                        _downloadMessage.value = application.getString(R.string.error_no_datos_servidor)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Error en la respuesta: $errorBody")
                    when (response.code()) {
                        401 -> {
                            _downloadMessage.value = application.getString(R.string.historial_compras_sesion_expirada)
                            _shouldNavigateToLogin.value = true
                        }
                        403 -> _downloadMessage.value = application.getString(R.string.historial_compras_sin_permiso)
                        404 -> _downloadMessage.value = application.getString(R.string.historial_compras_factura_no_encontrada)
                        500 -> _downloadMessage.value = application.getString(R.string.historial_compras_error_servidor)
                        else -> _downloadMessage.value = "Error: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al descargar factura: ${e.message}", e)
                _downloadMessage.value = "${application.getString(R.string.historial_compras_error_descarga)}: ${e.message}"
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