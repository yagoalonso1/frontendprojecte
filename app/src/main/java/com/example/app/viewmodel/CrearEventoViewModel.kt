package com.example.app.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.api.RetrofitClient
import com.example.app.util.SessionManager
import com.example.app.model.evento.EventoRequest
import com.example.app.model.evento.TipoEntradaRequest
import com.example.app.model.evento.CategoriaEvento
import com.example.app.model.evento.CrearEventoResponse
import kotlinx.coroutines.launch
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.content.Context
import androidx.core.content.FileProvider
import com.example.app.util.FileUtil
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.File
import java.io.IOException
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType

// Método de extensión para convertir Boolean a RequestBody
fun Boolean.toRequestBody(mediaType: okhttp3.MediaType?): RequestBody {
    return this.toString().toRequestBody(mediaType)
}

class CrearEventoViewModel : ViewModel() {
    // Datos básicos del evento
    var nombreEvento by mutableStateOf("")
    var descripcion by mutableStateOf("")
    var fechaEvento by mutableStateOf("")
    var hora by mutableStateOf("")
    var ubicacion by mutableStateOf("")
    var categoria by mutableStateOf("")
    var imagen by mutableStateOf<Uri?>(null)
    var imageUri by mutableStateOf<Uri?>(null) // URI para imágenes tomadas con la cámara
    var esOnline by mutableStateOf(false)
    
    // Tipos de entrada
    var tiposEntrada by mutableStateOf<List<TipoEntradaState>>(listOf(createDefaultTipoEntrada()))
    
    // Estados de UI
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var success by mutableStateOf(false)
    
    // Estado para las categorías disponibles
    var categorias by mutableStateOf<List<String>>(emptyList())
    var isLoadingCategorias by mutableStateOf(false)
    var errorCategorias by mutableStateOf<String?>(null)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    init {
        cargarCategorias()
    }

    // Clase para manejar el estado de los tipos de entrada en la UI
    data class TipoEntradaState(
        var nombre: String = "",
        var precio: String = "0.0",
        var cantidadDisponible: String = "",
        var descripcion: String = "",
        var esIlimitado: Boolean = false
    )

    private fun createDefaultTipoEntrada(): TipoEntradaState {
        return TipoEntradaState(
            nombre = "General",
            precio = "0.0",
            cantidadDisponible = "100",
            descripcion = "Entrada general para el evento",
            esIlimitado = false
        )
    }

    /**
     * Crea un URI para guardar la imagen capturada con la cámara
     */
    fun createImageUri(context: Context): Uri? {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_" + timeStamp + "_"
            val storageDir = context.getExternalFilesDir("Pictures")
            val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
            
            imageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                imageFile
            )
            return imageUri
        } catch (e: IOException) {
            Log.e("CrearEventoViewModel", "Error creando el archivo para la cámara", e)
            error = "Error al crear el archivo para la cámara: ${e.localizedMessage}"
            return null
        }
    }

    fun addTipoEntrada() {
        tiposEntrada = tiposEntrada + createDefaultTipoEntrada()
    }

    fun removeTipoEntrada(index: Int) {
        if (tiposEntrada.size > 1) {
            tiposEntrada = tiposEntrada.filterIndexed { i, _ -> i != index }
        } else {
            error = "Debe haber al menos un tipo de entrada"
        }
    }

    fun updateTipoEntrada(index: Int, updatedTipo: TipoEntradaState) {
        tiposEntrada = tiposEntrada.mapIndexed { i, tipo ->
            if (i == index) updatedTipo else tipo
        }
    }

    private fun cargarCategorias() {
        viewModelScope.launch {
            isLoadingCategorias = true
            errorCategorias = null
            try {
                // Usar categorías estáticas en lugar de cargarlas de la API
                categorias = listOf(
                    "Festival",
                    "Concierto",
                    "Teatro",
                    "Deportes",
                    "Conferencia",
                    "Exposición",
                    "Taller",
                    "Otro"
                )
                Log.d("CrearEventoViewModel", "Categorías cargadas: $categorias")
            } catch (e: Exception) {
                errorCategorias = "Error al cargar las categorías: ${e.message}"
                Log.e("CrearEventoViewModel", "Excepción al cargar categorías", e)
            } finally {
                isLoadingCategorias = false
            }
        }
    }

    fun onImageSelected(uri: Uri) {
        imagen = uri
    }

    fun validarFormulario(): Boolean {
        // Validar datos básicos
        if (nombreEvento.isBlank() || descripcion.isBlank() || 
            fechaEvento.isBlank() || hora.isBlank() || 
            ubicacion.isBlank() || categoria.isBlank()) {
            error = "Por favor, completa todos los campos obligatorios del evento"
            return false
        }

        try {
            // Validar que la fecha sea posterior a hoy
            val fechaEvt = dateFormat.parse(fechaEvento)
            val manana = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            
            if (fechaEvt != null && fechaEvt.before(manana)) {
                error = "La fecha del evento debe ser posterior a hoy (mínimo mañana)"
                return false
            }

            // Validar formato de hora
            timeFormat.parse(hora)
        } catch (e: Exception) {
            error = "Formato de fecha u hora inválido"
            Log.e("CrearEventoViewModel", "Error validando formato: ${e.message}")
            return false
        }

        // Validar tipos de entrada solo si el evento no es online
        if (!esOnline) {
            if (tiposEntrada.isEmpty()) {
                error = "Debe agregar al menos un tipo de entrada"
                return false
            }

            for ((index, tipo) in tiposEntrada.withIndex()) {
                if (tipo.nombre.isBlank()) {
                    error = "El nombre del tipo de entrada #${index + 1} no puede estar vacío"
                    return false
                }

                try {
                    val precio = tipo.precio.toDouble()
                    if (precio < 0) {
                        error = "El precio del tipo de entrada #${index + 1} no puede ser negativo"
                        return false
                    }
                } catch (e: NumberFormatException) {
                    error = "El precio del tipo de entrada #${index + 1} debe ser un número válido"
                    return false
                }

                if (!tipo.esIlimitado) {
                    try {
                        val cantidad = tipo.cantidadDisponible.toInt()
                        if (cantidad <= 0) {
                            error = "La cantidad de entradas disponibles #${index + 1} debe ser mayor a 0"
                            return false
                        }
                    } catch (e: NumberFormatException) {
                        error = "La cantidad de entradas disponibles #${index + 1} debe ser un número entero válido"
                        return false
                    }
                }
            }
        }

        return true
    }

    fun crearEvento(context: Context) {
        if (!validarFormulario()) {
            if (error == null) {
                error = "Por favor, completa todos los campos obligatorios"
            }
            return
        }

        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val token = SessionManager.getToken()
                if (token == null) {
                    error = "No se encontró el token de autenticación"
                    return@launch
                }

                // Validar que la categoría sea válida
                if (!categorias.contains(categoria)) {
                    error = "La categoría seleccionada no es válida"
                    return@launch
                }

                if (imagen != null) {
                    // Si hay imagen, enviar datos con imagen usando multipart
                    try {
                        // Convertir la imagen Uri a MultipartBody.Part
                        Log.d("CrearEventoViewModel", "Iniciando procesamiento de imagen: $imagen")
                        val imagenPart = FileUtil.uriToMultipartImage(context, imagen!!, "imagen")
                        
                        if (imagenPart == null) {
                            error = "Error al procesar la imagen. La imagen puede ser demasiado grande (máximo 5MB) o tener un formato no compatible."
                            Log.e("CrearEventoViewModel", "Error: imagen null después de procesarla")
                            isLoading = false
                            return@launch
                        }
                        
                        Log.d("CrearEventoViewModel", "Imagen procesada correctamente")
                        
                        // Para el campo es_online, intentar con valores numéricos (1/0) que a veces funcionan mejor
                        Log.d("CrearEventoViewModel", "Creando es_online body")
                        val esOnlineValue = if (esOnline) "1" else "0"
                        Log.d("CrearEventoViewModel", "Valor de es_online: $esOnlineValue")
                        val esOnlineBody = esOnlineValue.toRequestBody("text/plain".toMediaTypeOrNull())
                        
                        // Para tipos_entrada, formatear como array JSON en formato string
                        Log.d("CrearEventoViewModel", "Creando tipos_entrada body")
                        val tiposEntradaArray = if (esOnline) {
                            "[]" // Array vacío en JSON
                        } else {
                            val tiposJson = tiposEntrada.map { tipoState ->
                                "{" +
                                "\"nombre\":\"${tipoState.nombre}\"," +
                                "\"precio\":${tipoState.precio.toDoubleOrNull() ?: 0.0}," +
                                "\"es_ilimitado\":${tipoState.esIlimitado}," +
                                "\"cantidad_disponible\":${if (tipoState.esIlimitado) 0 else (tipoState.cantidadDisponible.toIntOrNull() ?: 0)}" +
                                "}"
                            }.joinToString(",")
                            "[$tiposJson]"
                        }
                        
                        // Log del JSON resultante
                        Log.d("CrearEventoViewModel", "JSON tipos_entrada: $tiposEntradaArray")
                        
                        // Usar la API moderna de Kotlin para crear RequestBody - cambiando el tipo MIME
                        val tiposEntradaBody = tiposEntradaArray.toRequestBody("text/plain".toMediaTypeOrNull())
                        
                        // Log detallado para depuración
                        Log.d("CrearEventoViewModel", "Enviando petición con imagen")
                        Log.d("CrearEventoViewModel", "- Es online: $esOnline (java type: ${esOnline.javaClass.name})")
                        Log.d("CrearEventoViewModel", "- Valor de es_online enviado: $esOnlineValue")
                        Log.d("CrearEventoViewModel", "- Tipos entrada (array): $tiposEntradaArray")
                        
                        try {
                            val response = withContext(Dispatchers.IO) {
                                RetrofitClient.apiService.crearEventoConImagen(
                                    "Bearer $token",
                                    FileUtil.createPartFromString(nombreEvento),
                                    FileUtil.createPartFromString(descripcion),
                                    FileUtil.createPartFromString(fechaEvento),
                                    FileUtil.createPartFromString(hora),
                                    FileUtil.createPartFromString(ubicacion),
                                    FileUtil.createPartFromString(categoria),
                                    esOnlineBody,
                                    tiposEntradaBody,
                                    imagenPart
                                )
                            }
                            
                            val responseCode = response.code()
                            val responseMessage = response.message()
                            Log.d("CrearEventoViewModel", "Respuesta HTTP: $responseCode - $responseMessage")
                            
                            processResponse(response)
                        } catch (e: Exception) {
                            Log.e("CrearEventoViewModel", "Error en la petición", e)
                            error = "Error de conexión: ${e.message}"
                            isLoading = false
                        }
                    } catch (e: Exception) {
                        error = "Error al procesar la imagen: ${e.message}"
                        Log.e("CrearEventoViewModel", "Error al procesar la imagen", e)
                        isLoading = false
                    }
                } else {
                    // Si no hay imagen, enviar datos sin imagen
                    try {
                        // Preparar tipos de entrada
                        val tiposEntradaRequest = if (esOnline) {
                            emptyList<TipoEntradaRequest>()
                        } else {
                            tiposEntrada.map { tipoState ->
                                TipoEntradaRequest(
                                    nombre = tipoState.nombre,
                                    precio = tipoState.precio.toDoubleOrNull() ?: 0.0,
                                    cantidadDisponible = if (tipoState.esIlimitado) null else tipoState.cantidadDisponible.toIntOrNull(),
                                    descripcion = if (tipoState.descripcion.isBlank()) null else tipoState.descripcion,
                                    esIlimitado = tipoState.esIlimitado
                                )
                            }
                        }
                        
                        Log.d("CrearEventoViewModel", "Enviando evento sin imagen. esOnline: $esOnline")
                        Log.d("CrearEventoViewModel", "Tipos de entrada: ${Gson().toJson(tiposEntradaRequest)}")
                        
                        // Crear el objeto EventoRequest
                        val eventoRequest = EventoRequest(
                            nombreEvento = nombreEvento,
                            descripcion = descripcion,
                            fechaEvento = fechaEvento,
                            hora = hora,
                            ubicacion = ubicacion,
                            categoria = categoria,
                            esOnline = esOnline,
                            tiposEntrada = tiposEntradaRequest
                        )
                        
                        val response = withContext(Dispatchers.IO) {
                            RetrofitClient.apiService.crearEvento(
                                "Bearer $token",
                                eventoRequest
                            )
                        }
                        
                        processResponse(response)
                    } catch (e: Exception) {
                        error = "Error de conexión: ${e.message}"
                        Log.e("CrearEventoViewModel", "Error al crear evento sin imagen", e)
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                error = "Error de conexión: ${e.message}"
                Log.e("CrearEventoViewModel", "Excepción al crear evento", e)
                isLoading = false
            }
        }
    }
    
    private fun processResponse(response: Response<CrearEventoResponse>) {
        viewModelScope.launch {
            try {
                Log.d("CrearEventoViewModel", "Código de respuesta: ${response.code()}")
                Log.d("CrearEventoViewModel", "Headers: ${response.headers()}")
                
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.status == "success") {
                        success = true
                        resetForm()
                        Log.d("CrearEventoViewModel", "Evento creado exitosamente: ${responseBody.message}")
                    } else {
                        error = responseBody?.message ?: "Error desconocido al crear el evento"
                        Log.d("CrearEventoViewModel", "Respuesta exitosa pero con error: ${Gson().toJson(responseBody)}")
                        Log.e("CrearEventoViewModel", "Error en la respuesta: $error")
                    }
                } else {
                    // Registro detallado de errores
                    Log.e("CrearEventoViewModel", "==== ERROR EN LA RESPUESTA ====")
                    Log.e("CrearEventoViewModel", "Código de respuesta HTTP: ${response.code()}")
                    Log.e("CrearEventoViewModel", "Mensaje: ${response.message()}")
                    
                    // Guardar el error body en una variable para reutilizarlo
                    val errorBodyString = response.errorBody()?.string() ?: ""
                    Log.e("CrearEventoViewModel", "Error Body completo: $errorBodyString")
                    
                    // Si es un error 422 (Unprocessable Content)
                    if (response.code() == 422) {
                        Log.e("CrearEventoViewModel", "UNPROCESSABLE CONTENT: Problema de validación de datos")
                        
                        try {
                            val errorJson = com.google.gson.JsonParser.parseString(errorBodyString).asJsonObject
                            
                            // Adaptamos para manejar la estructura actual del error
                            if (errorJson.has("messages")) {
                                val messagesObj = errorJson.getAsJsonObject("messages")
                                val errorMessages = StringBuilder()
                                
                                for (fieldName in messagesObj.keySet()) {
                                    val fieldErrors = messagesObj.getAsJsonArray(fieldName)
                                    Log.e("CrearEventoViewModel", "Campo con error: $fieldName - Errores: $fieldErrors")
                                    
                                    for (i in 0 until fieldErrors.size()) {
                                        errorMessages.append("• ${fieldErrors.get(i).asString}\n")
                                    }
                                }
                                
                                if (errorMessages.isNotEmpty()) {
                                    error = "Errores de validación:\n$errorMessages"
                                } else {
                                    error = errorJson.get("error")?.asString ?: "Error de validación desconocido"
                                }
                                return@launch
                            }
                            
                            // Manejar también la estructura con "errors" para compatibilidad
                            if (errorJson.has("errors")) {
                                val errorsObj = errorJson.getAsJsonObject("errors")
                                for (fieldName in errorsObj.keySet()) {
                                    val fieldErrors = errorsObj.getAsJsonArray(fieldName)
                                    Log.e("CrearEventoViewModel", "Campo con error: $fieldName - Errores: $fieldErrors")
                                }
                            }
                            
                            // Si llegamos aquí y hay un mensaje general de error
                            if (errorJson.has("error")) {
                                error = errorJson.get("error").asString
                                return@launch
                            }
                        } catch (e: Exception) {
                            Log.e("CrearEventoViewModel", "Error al parsear JSON de error", e)
                            error = "Error de validación: ${e.message}"
                            return@launch
                        }
                    }
                    
                    // Si no se ha establecido un error específico hasta ahora
                    val currentError = error // Guardamos el valor actual en una variable local
                    if (currentError == null || currentError.isEmpty()) {
                        error = try {
                            val errorJson = com.google.gson.JsonParser.parseString(errorBodyString)
                            
                            if (errorJson.isJsonObject) {
                                val jsonObject = errorJson.asJsonObject
                                
                                when {
                                    jsonObject.has("message") -> "Error: ${jsonObject.get("message").asString}"
                                    jsonObject.has("error") -> "Error: ${jsonObject.get("error").asString}"
                                    else -> "Error HTTP: ${response.code()}"
                                }
                            } else {
                                "Error HTTP: ${response.code()}"
                            }
                        } catch (e: Exception) {
                            Log.e("CrearEventoViewModel", "Error al parsear respuesta de error", e)
                            "Error al crear el evento: ${response.message() ?: errorBodyString ?: "Error desconocido"}"
                        }
                    }
                }
            } catch (e: Exception) {
                error = "Error al procesar la respuesta: ${e.message}"
                Log.e("CrearEventoViewModel", "Error al procesar la respuesta", e)
            } finally {
                isLoading = false
            }
        }
    }

    private fun resetForm() {
        nombreEvento = ""
        descripcion = ""
        fechaEvento = ""
        hora = ""
        ubicacion = ""
        categoria = ""
        imagen = null
        esOnline = false
        tiposEntrada = listOf(createDefaultTipoEntrada())
    }
}