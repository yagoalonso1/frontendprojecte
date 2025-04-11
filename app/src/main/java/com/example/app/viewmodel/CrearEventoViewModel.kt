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
        Log.e(TAG, "ERROR: $errorMsg")
    }

    // Función para validar los campos del formulario
    private fun validarCampos(): Boolean {
        val errores = mutableListOf<String>()

        // Validación de longitud y contenido del título
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

        // Validación de fecha y hora
        if (fechaEvento.isBlank()) {
            errores.add("• La fecha del evento es obligatoria")
        } else {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdf.isLenient = false // Hace que la validación de fecha sea más estricta
                val fechaSeleccionada = sdf.parse(fechaEvento)
                val hoy = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                
                if (fechaSeleccionada != null) {
                    if (fechaSeleccionada.before(hoy)) {
                        errores.add("• La fecha del evento debe ser igual o posterior a hoy")
                    }
                    
                    // Validar que la fecha no esté muy lejos en el futuro (por ejemplo, 2 años)
                    val dosAnosDespues = Calendar.getInstance().apply {
                        add(Calendar.YEAR, 2)
                    }.time
                    if (fechaSeleccionada.after(dosAnosDespues)) {
                        errores.add("• La fecha del evento no puede ser más de 2 años en el futuro")
                    }
                }
            } catch (e: Exception) {
                errores.add("• El formato de fecha es inválido. Debe ser YYYY-MM-DD")
            }
        }

        // Validación de hora
        if (hora.isBlank()) {
            errores.add("• La hora del evento es obligatoria")
        } else {
            try {
                val horaPattern = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$"
                if (!hora.matches(Regex(horaPattern))) {
                    errores.add("• El formato de hora es inválido. Debe ser HH:MM (24h)")
                }
            } catch (e: Exception) {
                errores.add("• El formato de hora es inválido")
            }
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

        // Validación de tipos de entrada
        if (!esOnline) {
            if (tiposEntrada.isEmpty()) {
                errores.add("• Debes añadir al menos un tipo de entrada")
            } else {
                for ((index, tipo) in tiposEntrada.withIndex()) {
                    val numTipo = index + 1
                    when {
                        tipo.nombre.isBlank() -> {
                            errores.add("• El nombre del tipo de entrada #$numTipo no puede estar vacío")
                        }
                        tipo.nombre.length < 3 -> {
                            errores.add("• El nombre del tipo de entrada #$numTipo debe tener al menos 3 caracteres")
                        }
                        tipo.nombre.length > 50 -> {
                            errores.add("• El nombre del tipo de entrada #$numTipo no puede exceder los 50 caracteres")
                        }
                    }
                    
                    try {
                        val precio = tipo.precio
                        when {
                            precio < 0 -> {
                                errores.add("• El precio del tipo de entrada #$numTipo debe ser un número no negativo")
                            }
                            precio > 10000 -> {
                                errores.add("• El precio del tipo de entrada #$numTipo no puede exceder los 10.000")
                            }
                        }
                    } catch (e: Exception) {
                        errores.add("• El precio del tipo de entrada #$numTipo debe ser un número válido")
                    }
                    
                    if (!tipo.esIlimitado && tipo.cantidadDisponible != null) {
                        try {
                            val cantidad = tipo.cantidadDisponible
                            when {
                                cantidad == null || cantidad <= 0 -> {
                                    errores.add("• La cantidad de entradas disponibles #$numTipo debe ser mayor a 0")
                                }
                                cantidad > 100000 -> {
                                    errores.add("• La cantidad de entradas disponibles #$numTipo no puede exceder las 100.000")
                                }
                            }
                        } catch (e: Exception) {
                            errores.add("• La cantidad de entradas disponibles #$numTipo debe ser un número entero válido")
                        }
                    }

                    // Validar descripción del tipo de entrada
                    if (tipo.descripcion?.isNotBlank() == true) {
                        if (tipo.descripcion.length > 200) {
                            errores.add("• La descripción del tipo de entrada #$numTipo no puede exceder los 200 caracteres")
                        }
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
                if (errores.size > 1) {
                    appendLine("\nPor favor, revisa todos los campos y asegúrate de que cumplan con los requisitos.")
                }
            }
            updateError(mensajeError.trim())
            return false
        }
        
        return true
    }
    
    // Función para crear el evento con imagen
    fun crearEventoConImagen(context: Context) {
        viewModelScope.launch {
            try {
                // Validar que los campos obligatorios estén completos
                if (!validarCampos()) {
                    return@launch
                }
                
                // Obtener token
                val token = SessionManager.getToken()
                if (token.isNullOrEmpty()) {
                    updateError("No hay sesión activa. Por favor, inicia sesión.")
                    return@launch
                }
                
                // Iniciar carga
                isLoading = true
                error = null
                
                // Crear las partes del formulario
                val tituloBody = titulo.toRequestBody("text/plain".toMediaTypeOrNull())
                val descripcionBody = descripcion.toRequestBody("text/plain".toMediaTypeOrNull())
                val fechaBody = fechaEvento.toRequestBody("text/plain".toMediaTypeOrNull())
                val horaBody = hora.toRequestBody("text/plain".toMediaTypeOrNull())
                val ubicacionBody = ubicacion.toRequestBody("text/plain".toMediaTypeOrNull())
                val categoriaBody = categoria.toRequestBody("text/plain".toMediaTypeOrNull())
                val esOnlineBody = esOnline.toRequestBody("text/plain".toMediaTypeOrNull())
                
                // Convertir la lista de tipos de entrada a JSON
                val tiposEntradaJson = if (esOnline) {
                    "[]" // Array vacío para eventos online
                } else {
                    // Usar directamente los objetos TipoEntradaRequest con Gson
                    Gson().toJson(tiposEntrada)
                }
                
                Log.d(TAG, "Tipos de entrada JSON: $tiposEntradaJson")
                // Asegurarnos de usar el tipo MIME application/json para el JSON
                val tiposEntradaBody = tiposEntradaJson.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                
                // Preparar el archivo de imagen si existe
                var imagenPart: MultipartBody.Part? = null
                if (imagenUri != null) {
                    // Intentar obtener la imagen mediante FileUtil
                    imagenPart = FileUtil.uriToMultipartImage(context, imagenUri!!, "imagen")
                    
                    if (imagenPart == null) {
                        updateError("Error al procesar la imagen. Por favor, selecciona otra imagen o intenta más tarde.")
                        isLoading = false
                        return@launch
                    }
                    
                    Log.d(TAG, "Imagen preparada correctamente para subida")
                }
                
                // Log adicional
                Log.d(TAG, "JSON de tipos de entrada enviados: $tiposEntradaJson")
                Log.d(TAG, "Es online: $esOnline")
                
                Log.d(TAG, "Enviando datos para crear evento:")
                Log.d(TAG, "- Título: $titulo")
                Log.d(TAG, "- Fecha: $fechaEvento, Hora: $hora")
                Log.d(TAG, "- Ubicación: $ubicacion")
                Log.d(TAG, "- Categoría: $categoria")
                Log.d(TAG, "- Es online: $esOnline")
                Log.d(TAG, "- Tipos de entrada: ${tiposEntrada.size}")
                Log.d(TAG, "- Imagen incluida: ${imagenPart != null}")
                
                // Realizar la petición al API
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.crearEventoConImagen(
                        token = "Bearer $token",
                        titulo = tituloBody,
                        descripcion = descripcionBody,
                        fecha = fechaBody,
                        hora = horaBody,
                        ubicacion = ubicacionBody,
                        categoria = categoriaBody,
                        esOnline = esOnlineBody,
                        tiposEntrada = tiposEntradaBody,
                        imagen = imagenPart
                    )
                }
                
                Log.d(TAG, "Respuesta del servidor: ${response.code()}")
                
                if (response.isSuccessful) {
                    val eventoResponse = response.body()
                    Log.d(TAG, "Evento creado exitosamente: ${eventoResponse?.message}")
                    _isCreationSuccessful.value = true
                    limpiarFormulario()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e(TAG, "Error al crear evento: ${response.code()} - $errorBody")
                    Log.e(TAG, "Headers de respuesta: ${response.headers()}")
                    
                    try {
                        Log.e(TAG, "Respuesta de error completa: $errorBody")
                        val errorResponse = Gson().fromJson(errorBody, Map::class.java)
                        val errorMessage = errorResponse["message"] as? String 
                            ?: errorResponse["error"] as? String
                            ?: "Error al crear el evento (${response.code()})"
                            
                        // Intentar extraer detalles de validación
                        if (errorResponse.containsKey("errors")) {
                            val errors = errorResponse["errors"] as? Map<*, *>
                            if (errors != null) {
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
                                return@launch
                            }
                        }
                        
                        updateError(errorMessage)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al procesar la respuesta de error", e)
                        updateError("Error al crear el evento: ${response.message()} (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción al crear el evento", e)
                updateError("Error de conexión: ${e.message}")
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