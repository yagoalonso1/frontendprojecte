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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.FileOutputStream
import java.io.InputStream

// Método de extensión para convertir Boolean a RequestBody
fun Boolean.toRequestBody(mediaType: okhttp3.MediaType?): RequestBody {
    return (if (this) "1" else "0").toRequestBody(mediaType)
}

class CrearEventoViewModel : ViewModel() {
    // Datos básicos del evento
    var nombreEvento by mutableStateOf("")
    var descripcion by mutableStateOf("")
    var fechaEvento by mutableStateOf("")
    var hora by mutableStateOf("")
    var ubicacion by mutableStateOf("")
    var categoria by mutableStateOf("")
    var esOnline by mutableStateOf(false)
    
    // URI de la imagen original y la que se toma con la cámara
    var imagen by mutableStateOf<Uri?>(null)
    var imageUri by mutableStateOf<Uri?>(null)
    
    // Datos del formulario
    var titulo by mutableStateOf("")
    var imagenUri by mutableStateOf<Uri?>(null)
    
    // Tipos de entrada
    var tiposEntrada by mutableStateOf<List<TipoEntradaRequest>>(listOf())
    
    // Estados de UI
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var success by mutableStateOf(false)
    
    // Estado para las categorías disponibles
    var categorias by mutableStateOf<List<String>>(emptyList())
    var isLoadingCategorias by mutableStateOf(false)
    var errorCategorias by mutableStateOf<String?>(null)

    // Estado de éxito
    private val _isCreationSuccessful = MutableStateFlow(false)
    val isCreationSuccessful: StateFlow<Boolean> = _isCreationSuccessful
    
    // TAG para logs
    private val TAG = "CrearEventoViewModel"

    init {
        cargarCategorias()
        inicializarTiposEntrada()
    }

    /**
     * Crea un URI para guardar la imagen capturada con la cámara
     */
    fun createImageUri(context: Context): Uri? {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "IMG_$timeStamp.jpg"
            val file = File(context.cacheDir, fileName)
            
            // Usar FileProvider en lugar de Uri.fromFile
            val authority = "${context.packageName}.provider"
            imageUri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)
            
            return imageUri
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear URI para imagen", e)
            return null
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

    // Manejo de imagen
    fun onImageSelected(uri: Uri) {
        imagen = uri
        imagenUri = uri // Actualizamos ambas variables para mantener consistencia
    }
    
    // Función para actualizar la URI de la imagen
    fun updateImagenUri(uri: Uri?) {
        imagenUri = uri
        imagen = uri // Actualizamos ambas variables para mantener consistencia
    }
    
    // Funciones para manejar tipos de entrada
    fun addTipoEntrada() {
        val tipoEntrada = TipoEntradaRequest(
            nombre = "General",
            precio = 25.0,
            cantidadDisponible = 100,
            descripcion = "Acceso General Evento",
            esIlimitado = false
        )
        tiposEntrada = tiposEntrada + tipoEntrada
    }
    
    fun removeTipoEntrada(index: Int) {
        if (index >= 0 && index < tiposEntrada.size && tiposEntrada.size > 1) {
            tiposEntrada = tiposEntrada.toMutableList().apply {
                removeAt(index)
            }
        } else if (tiposEntrada.size <= 1) {
            updateError("Debe haber al menos un tipo de entrada")
        }
    }
    
    fun updateTipoEntrada(index: Int, tipoEntrada: TipoEntradaRequest) {
        if (index >= 0 && index < tiposEntrada.size) {
            tiposEntrada = tiposEntrada.toMutableList().apply {
                set(index, tipoEntrada)
            }
        }
    }
    
    // Funciones para actualizar los campos del formulario
    fun updateTitulo(newTitulo: String) {
        titulo = newTitulo
        nombreEvento = newTitulo // Actualizar ambas propiedades
    }
    
    fun updateDescripcion(newDescripcion: String) {
        descripcion = newDescripcion
    }
    
    fun updateFecha(newFecha: String) {
        fechaEvento = newFecha
    }
    
    fun updateHora(newHora: String) {
        hora = newHora
    }
    
    fun updateUbicacion(newUbicacion: String) {
        ubicacion = newUbicacion
    }
    
    fun updateCategoria(newCategoria: String) {
        categoria = newCategoria
    }
    
    fun updateEsOnline(newEsOnline: Boolean) {
        esOnline = newEsOnline
    }

    // Cambiar a private para evitar conflictos de firma JVM con el setter automático
    private fun updateError(errorMsg: String?) {
        error = errorMsg
        if (errorMsg != null) {
            Log.e(TAG, "ERROR: $errorMsg")
            isLoading = false
        }
    }

    // Función para validar los campos del formulario
    private fun validarCampos(): Boolean {
        val errores = mutableListOf<String>()

        // Validación de título
        when {
            titulo.isBlank() -> {
                errores.add("• El título del evento es obligatorio")
            }
            titulo.length < 3 -> {
                errores.add("• El título debe tener al menos 3 caracteres")
            }
            titulo.length > 100 -> {
                errores.add("• El título no puede exceder los 100 caracteres")
            }
        }

        // Validación de descripción
        when {
            descripcion.isBlank() -> {
                errores.add("• La descripción del evento es obligatoria")
            }
            descripcion.length < 10 -> {
                errores.add("• La descripción debe tener al menos 10 caracteres")
            }
            descripcion.length > 1000 -> {
                errores.add("• La descripción no puede exceder los 1000 caracteres")
            }
        }

        // Validación de fecha
        if (fechaEvento.isBlank()) {
            errores.add("• La fecha del evento es obligatoria")
        }

        // Validación de hora
        if (hora.isBlank()) {
            errores.add("• La hora del evento es obligatoria")
        }

        // Validación de ubicación
        when {
            ubicacion.isBlank() -> {
                errores.add("• La ubicación del evento es obligatoria")
            }
            ubicacion.length < 3 -> {
                errores.add("• La ubicación debe tener al menos 3 caracteres")
            }
            ubicacion.length > 200 -> {
                errores.add("• La ubicación no puede exceder los 200 caracteres")
            }
        }

        // Validación de categoría
        when {
            categoria.isBlank() -> {
                errores.add("• La categoría del evento es obligatoria")
            }
            !categorias.contains(categoria) -> {
                errores.add("• La categoría seleccionada no es válida")
            }
        }

        // Eventos online no requieren tipos de entrada
        if (!esOnline) {
            // Validación de tipos de entrada solo si no es online
            if (tiposEntrada.isEmpty()) {
                errores.add("• Debes añadir al menos un tipo de entrada")
            } else {
                tiposEntrada.forEachIndexed { index, tipo ->
                    val numTipo = index + 1
                    
                    // Validar nombre
                    when {
                        tipo.nombre.isBlank() -> {
                            errores.add("• El nombre del tipo de entrada #$numTipo es obligatorio")
                        }
                        tipo.nombre.length < 3 -> {
                            errores.add("• El nombre del tipo de entrada #$numTipo debe tener al menos 3 caracteres")
                        }
                        tipo.nombre.length > 50 -> {
                            errores.add("• El nombre del tipo de entrada #$numTipo no puede exceder los 50 caracteres")
                        }
                    }
                    
                    // Validar precio
                    if (tipo.precio < 0) {
                        errores.add("• El precio del tipo de entrada #$numTipo no puede ser negativo")
                    }
                    
                    // Validar cantidad si no es ilimitado
                    if (!tipo.esIlimitado && (tipo.cantidadDisponible == null || tipo.cantidadDisponible <= 0)) {
                        errores.add("• La cantidad de entradas disponibles #$numTipo debe ser mayor a 0")
                    }
                }
            }
        }

        if (errores.isNotEmpty()) {
            val mensajeError = buildString {
                appendLine("Por favor, corrige los siguientes errores:")
                errores.forEach { error ->
                    appendLine(error)
                }
            }
            updateError(mensajeError.trim())
            Log.e(TAG, "Errores de validación: $errores")
            return false
        }

        return true
    }

    // Función para crear el evento con imagen
    fun crearEventoConImagen(context: Context) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Iniciando creación de evento")
                // Validar que los campos obligatorios estén completos
                if (!validarCampos()) {
                    Log.e(TAG, "Validación de campos fallida")
                    return@launch
                }
                Log.d(TAG, "Validación de campos exitosa")
                
                // Obtener token
                val token = SessionManager.getToken()
                Log.d(TAG, "Token obtenido: ${token?.take(10)}...")
                if (token.isNullOrEmpty()) {
                    Log.e(TAG, "Error: Token no disponible")
                    updateError("No hay sesión activa. Por favor, inicia sesión.")
                    return@launch
                }

                // Iniciar carga
                isLoading = true
                error = null
                
                // Añadir log para verificar todos los datos
                Log.d(TAG, """
                    Datos que se enviarán al servidor:
                    - titulo: $titulo
                    - descripcion: $descripcion
                    - fecha: $fechaEvento
                    - hora: $hora
                    - ubicacion: $ubicacion
                    - categoria: $categoria
                    - es_online: $esOnline
                    - tipos_entrada: $tiposEntrada
                """.trimIndent())
                
                // Crear las partes del formulario
                Log.d(TAG, "Preparando datos del formulario")
                val tituloBody = titulo.toRequestBody("text/plain".toMediaTypeOrNull())
                val descripcionBody = descripcion.toRequestBody("text/plain".toMediaTypeOrNull())
                val fechaBody = fechaEvento.toRequestBody("text/plain".toMediaTypeOrNull())
                val horaBody = hora.toRequestBody("text/plain".toMediaTypeOrNull())
                val ubicacionBody = ubicacion.toRequestBody("text/plain".toMediaTypeOrNull())
                val categoriaBody = categoria.toRequestBody("text/plain".toMediaTypeOrNull())
                val esOnlineBody = (if (esOnline) "1" else "0").toRequestBody("text/plain".toMediaTypeOrNull())
                
                // Preparar el archivo de imagen si existe
                var imagenPart: MultipartBody.Part? = null
                if (imagenUri != null) {
                    try {
                        Log.d(TAG, "Iniciando procesamiento de imagen")
                        Log.d(TAG, "URI de imagen: ${imagenUri.toString()}")
                        
                        // Obtener información del archivo
                        context.contentResolver.query(imagenUri!!, null, null, null, null)?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                                if (sizeIndex != -1) {
                                    val size = cursor.getLong(sizeIndex)
                                    if (size > 2 * 1024 * 1024) { // 2MB
                                        throw Exception("La imagen no puede pesar más de 2MB")
                                    }
                                    Log.d(TAG, "Tamaño de imagen: ${size / 1024} KB")
                                }
                                
                                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                                val fileName = if (nameIndex != -1) {
                                    cursor.getString(nameIndex)
                                } else {
                                    "imagen_${System.currentTimeMillis()}.jpg"
                                }
                                Log.d(TAG, "Nombre de archivo: $fileName")
                            }
                        }

                        // Obtener el tipo MIME
                        val mimeType = context.contentResolver.getType(imagenUri!!) ?: "image/jpeg"
                        if (mimeType !in listOf("image/jpeg", "image/png", "image/gif")) {
                            throw Exception("El tipo de archivo no es una imagen válida")
                        }
                        Log.d(TAG, "Tipo MIME: $mimeType")

                        // Crear un archivo temporal real en el sistema de archivos
                        val tempFile = File(context.cacheDir, "temp_img_${System.currentTimeMillis()}.jpg")
                        
                        val inputStream = context.contentResolver.openInputStream(imagenUri!!)
                        val outputStream = FileOutputStream(tempFile)
                        
                        inputStream?.use { input ->
                            outputStream.use { output ->
                                input.copyTo(output)
                            }
                        }
                        
                        Log.d(TAG, "Archivo temporal creado: ${tempFile.absolutePath}, tamaño: ${tempFile.length() / 1024} KB")
                        
                        // Crear el RequestBody a partir del archivo real
                        val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
                        imagenPart = MultipartBody.Part.createFormData("imagen", tempFile.name, requestFile)
                        
                        Log.d(TAG, "Imagen procesada correctamente")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al procesar la imagen", e)
                        throw Exception("Error al procesar la imagen: ${e.message}")
                    }
                }

                // Realizar la petición al API
                Log.d(TAG, "Iniciando petición al servidor...")
                val response = withContext(Dispatchers.IO) {
                    try {
                        if (imagenPart != null) {
                            // Preparar tipos de entrada según si es evento online o no
                            val tiposEntradaFinal = if (esOnline) {
                                emptyList()
                            } else {
                                // Asegurarnos de que todos los tipos de entrada tengan valores válidos
                                tiposEntrada.map { tipo ->
                                    // Validamos y corregimos cada tipo de entrada
                                    tipo.copy(
                                        nombre = if (tipo.nombre.isBlank()) "General" else tipo.nombre,
                                        precio = if (tipo.precio <= 0) 0.01 else tipo.precio,
                                        cantidadDisponible = if (tipo.esIlimitado) null else 
                                                          (tipo.cantidadDisponible ?: 100).takeIf { it > 0 } ?: 100,
                                        descripcion = tipo.descripcion.takeIf { !it.isNullOrBlank() } ?: "Entrada estándar",
                                        esIlimitado = tipo.esIlimitado
                                    )
                                }
                            }
                            
                            if (tiposEntradaFinal.isEmpty() && !esOnline) {
                                throw Exception("Debe especificar al menos un tipo de entrada para eventos presenciales")
                            }
                            
                            Log.d(TAG, "Enviando multipart con tipos de entrada")
                            
                            // Para eventos online, no enviamos tipos de entrada
                            if (esOnline) {
                                // Para eventos online no es necesario enviar tipos de entrada
                                val emptyList = emptyList<MultipartBody.Part>()
                                
                                RetrofitClient.apiService.crearEventoConImagen(
                                    token = "Bearer $token",
                                    titulo = tituloBody,
                                    descripcion = descripcionBody,
                                    fecha = fechaBody,
                                    hora = horaBody,
                                    ubicacion = ubicacionBody,
                                    categoria = categoriaBody,
                                    esOnline = esOnlineBody,
                                    tiposEntradas = emptyList,
                                    imagen = imagenPart
                                )
                            } else {
                                // Para eventos presenciales, enviamos todos los tipos de entrada
                                val partesTiposEntradas = mutableListOf<MultipartBody.Part>()
                                
                                // Procesamos cada tipo de entrada
                                tiposEntradaFinal.forEachIndexed { index, tipo ->
                                    // Aseguramos que todos los campos tengan valores válidos
                                    val nombre = if (tipo.nombre.isBlank()) "General" else tipo.nombre
                                    val precio = if (tipo.precio <= 0) 0.01 else tipo.precio
                                    val cantidadDisponible = if (tipo.esIlimitado) null else 
                                            (tipo.cantidadDisponible ?: 100).takeIf { it > 0 } ?: 100
                                    val descripcion = tipo.descripcion ?: "Entrada estándar"
                                    
                                    // Creamos cada parte para este tipo de entrada
                                    partesTiposEntradas.add(
                                        MultipartBody.Part.createFormData(
                                            "tipos_entrada[$index][nombre]", 
                                            nombre
                                        )
                                    )
                                    partesTiposEntradas.add(
                                        MultipartBody.Part.createFormData(
                                            "tipos_entrada[$index][precio]", 
                                            precio.toString()
                                        )
                                    )
                                    partesTiposEntradas.add(
                                        MultipartBody.Part.createFormData(
                                            "tipos_entrada[$index][cantidad_disponible]", 
                                            (cantidadDisponible ?: "").toString()
                                        )
                                    )
                                    partesTiposEntradas.add(
                                        MultipartBody.Part.createFormData(
                                            "tipos_entrada[$index][descripcion]", 
                                            descripcion
                                        )
                                    )
                                    partesTiposEntradas.add(
                                        MultipartBody.Part.createFormData(
                                            "tipos_entrada[$index][es_ilimitado]", 
                                            if (tipo.esIlimitado) "1" else "0"
                                        )
                                    )
                                }
                                
                                Log.d(TAG, "Enviando ${tiposEntradaFinal.size} tipos de entrada")
                                
                                // Llamada a la API con todos los tipos de entrada
                                RetrofitClient.apiService.crearEventoConImagen(
                                    token = "Bearer $token",
                                    titulo = tituloBody,
                                    descripcion = descripcionBody,
                                    fecha = fechaBody,
                                    hora = horaBody,
                                    ubicacion = ubicacionBody,
                                    categoria = categoriaBody,
                                    esOnline = esOnlineBody,
                                    tiposEntradas = partesTiposEntradas,
                                    imagen = imagenPart
                                )
                            }
                        } else {
                            // Preparar tipos de entrada según si es evento online o no
                            val tiposEntradaFinal = if (esOnline) {
                                emptyList()
                            } else {
                                // Asegurarnos de que todos los tipos de entrada tengan valores válidos
                                tiposEntrada.map { tipo ->
                                    // Validamos y corregimos cada tipo de entrada
                                    tipo.copy(
                                        nombre = if (tipo.nombre.isBlank()) "General" else tipo.nombre,
                                        precio = if (tipo.precio <= 0) 0.01 else tipo.precio,
                                        cantidadDisponible = if (tipo.esIlimitado) null else 
                                                          (tipo.cantidadDisponible ?: 100).takeIf { it > 0 } ?: 100,
                                        descripcion = tipo.descripcion.takeIf { !it.isNullOrBlank() } ?: "Entrada estándar",
                                        esIlimitado = tipo.esIlimitado
                                    )
                                }
                            }
                            
                            if (tiposEntradaFinal.isEmpty() && !esOnline) {
                                throw Exception("Debe especificar al menos un tipo de entrada para eventos presenciales")
                            }
                            
                        val eventoRequest = EventoRequest(
                                titulo = titulo,
                            descripcion = descripcion,
                                fecha = fechaEvento,
                            hora = hora,
                            ubicacion = ubicacion,
                            categoria = categoria,
                            esOnline = esOnline,
                                tiposEntrada = tiposEntradaFinal
                        )
                        
                            Log.d(TAG, "Enviando objeto: ${Gson().toJson(eventoRequest)}")
                            
                            // Llamada a la API sin imagen
                            RetrofitClient.apiService.crearEvento(
                                token = "Bearer $token",
                                request = eventoRequest
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al hacer la petición al servidor", e)
                        throw e
                    }
                }
                
                Log.d(TAG, "Código de respuesta: ${response.code()}")
                Log.d(TAG, "Headers de respuesta: ${response.headers()}")
                
                if (response.isSuccessful) {
                    val eventoResponse = response.body()
                    Log.d(TAG, "Evento creado exitosamente: ${eventoResponse?.message}")
                    Log.d(TAG, "Datos del evento creado: ${Gson().toJson(eventoResponse?.evento)}")
                    _isCreationSuccessful.value = true
                    limpiarFormulario()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, """
                        Error detallado al crear evento:
                        - Código: ${response.code()}
                        - Cuerpo del error: $errorBody
                        - Headers: ${response.headers()}
                    """.trimIndent())
                    
                    try {
                        val errorResponse = Gson().fromJson(errorBody, Map::class.java) as Map<String, Any>
                        Log.e(TAG, "Error response parseado: $errorResponse")
                        
                        val errorMessage = when {
                            errorResponse.containsKey("message") -> errorResponse["message"] as String
                            errorResponse.containsKey("error") -> errorResponse["error"] as String
                            else -> "Error al crear el evento (${response.code()})"
                        }
                        
                        // Intentar extraer detalles de validación
                        if (errorResponse.containsKey("messages")) {
                            val errors = errorResponse["messages"] as? Map<*, *>
                            if (errors != null) {
                                Log.e(TAG, "Errores de validación encontrados: $errors")
                                val errorDetails = StringBuilder("Se encontraron los siguientes errores:\n")
                                for ((campo, mensajes) in errors) {
                                    if (mensajes is List<*>) {
                                        for (mensaje in mensajes) {
                                            errorDetails.append("• $campo: $mensaje\n")
                                        }
                                    } else {
                                        errorDetails.append("• $campo: $mensajes\n")
                                    }
                                }
                                updateError(errorDetails.toString().trim())
                            } else {
                                updateError(errorMessage)
                            }
                        } else {
                            updateError(errorMessage)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al parsear respuesta de error", e)
                        updateError("Error al crear el evento: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción al crear evento", e)
                updateError("Error inesperado al crear el evento: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // Función para limpiar el formulario después de crear el evento
    private fun limpiarFormulario() {
        titulo = ""
        nombreEvento = ""
        descripcion = ""
        fechaEvento = ""
        hora = ""
        ubicacion = ""
        categoria = ""
        esOnline = false
        imagenUri = null
        imagen = null
        inicializarTiposEntrada()
    }
    
    // Función para resetear el estado de creación exitosa
    fun resetCreationState() {
        _isCreationSuccessful.value = false
    }
    
    // Función para limpiar el mensaje de error
    fun clearError() {
        error = null
    }

    private fun inicializarTiposEntrada() {
        // Crear un tipo de entrada por defecto
        tiposEntrada = listOf(
            TipoEntradaRequest(
                nombre = "General",
                precio = 25.0,
                cantidadDisponible = 100,
                descripcion = "Acceso General Evento",
                esIlimitado = false
            )
        )
    }
}