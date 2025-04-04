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
import com.example.app.util.FileUtil
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class CrearEventoViewModel : ViewModel() {
    // Datos básicos del evento
    var nombreEvento by mutableStateOf("")
    var descripcion by mutableStateOf("")
    var fechaEvento by mutableStateOf("")
    var hora by mutableStateOf("")
    var ubicacion by mutableStateOf("")
    var categoria by mutableStateOf("")
    var imagen by mutableStateOf<Uri?>(null)
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
                val token = SessionManager.getToken()
                if (token == null) {
                    errorCategorias = "No se encontró el token de autenticación"
                    return@launch
                }

                val response = RetrofitClient.apiService.getCategorias("Bearer $token")
                if (response.isSuccessful) {
                    categorias = response.body()?.categorias ?: emptyList()
                    Log.d("CrearEventoViewModel", "Categorías cargadas: $categorias")
                } else {
                    errorCategorias = "Error al cargar las categorías: ${response.message()}"
                    Log.e("CrearEventoViewModel", "Error al cargar categorías: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                errorCategorias = "Error de conexión: ${e.message}"
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

        try {
            // Validar que la fecha sea posterior a hoy
            val fechaEvt = dateFormat.parse(fechaEvento)
            val hoy = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            
            if (fechaEvt != null && fechaEvt.before(hoy)) {
                error = "La fecha del evento debe ser posterior a hoy"
                return false
            }

            // Validar formato de hora
            timeFormat.parse(hora)
            return true
        } catch (e: Exception) {
            error = "Formato de fecha u hora inválido"
            Log.e("CrearEventoViewModel", "Error validando formato: ${e.message}")
            return false
        }
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

                // Convertir los tipos de entrada al formato para la API
                val tiposEntradaRequest = tiposEntrada.map { tipoState ->
                    TipoEntradaRequest(
                        nombre = tipoState.nombre,
                        precio = tipoState.precio.toDoubleOrNull() ?: 0.0,
                        cantidadDisponible = if (tipoState.esIlimitado) null else tipoState.cantidadDisponible.toIntOrNull(),
                        descripcion = if (tipoState.descripcion.isBlank()) null else tipoState.descripcion,
                        esIlimitado = tipoState.esIlimitado
                    )
                }

                // Si hay una imagen seleccionada, usar el método multipart
                if (imagen != null) {
                    try {
                        // Convertir los datos a RequestBody
                        val tituloBody = FileUtil.createPartFromString(nombreEvento)
                        val descripcionBody = FileUtil.createPartFromString(descripcion)
                        val fechaBody = FileUtil.createPartFromString(fechaEvento)
                        val horaBody = FileUtil.createPartFromString(hora)
                        val ubicacionBody = FileUtil.createPartFromString(ubicacion)
                        val categoriaBody = FileUtil.createPartFromString(categoria)
                        val esOnlineBody = FileUtil.createPartFromString(esOnline.toString())
                        
                        // Convertir la lista de tipos de entrada a JSON y luego a RequestBody
                        val tiposEntradaJson = Gson().toJson(tiposEntradaRequest)
                        val tiposEntradaBody = FileUtil.createPartFromString(tiposEntradaJson)
                        
                        // Convertir la imagen Uri a MultipartBody.Part
                        val imagenPart = FileUtil.uriToMultipartImage(context, imagen!!, "imagen")
                        
                        Log.d("CrearEventoViewModel", "Enviando evento con imagen. Tipos de entrada: $tiposEntradaJson")
                        
                        val response = withContext(Dispatchers.IO) {
                            RetrofitClient.apiService.crearEventoConImagen(
                                "Bearer $token",
                                tituloBody,
                                descripcionBody,
                                fechaBody,
                                horaBody,
                                ubicacionBody,
                                categoriaBody,
                                esOnlineBody,
                                tiposEntradaBody,
                                imagenPart
                            )
                        }
                        
                        processResponse(response)
                    } catch (e: Exception) {
                        Log.e("CrearEventoViewModel", "Error al procesar imagen: ${e.message}", e)
                        error = "Error al procesar la imagen: ${e.message}"
                        isLoading = false
                    }
                } else {
                    // Si no hay imagen, usar el método regular
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
                    
                    Log.d("CrearEventoViewModel", "Enviando request sin imagen: ${Gson().toJson(eventoRequest)}")
                    
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.apiService.crearEvento("Bearer $token", eventoRequest)
                    }
                    
                    processResponse(response)
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
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.status == "success") {
                        success = true
                        resetForm()
                        Log.d("CrearEventoViewModel", "Evento creado exitosamente: ${responseBody.message}")
                    } else {
                        error = responseBody?.message ?: "Error desconocido al crear el evento"
                        Log.e("CrearEventoViewModel", "Error en la respuesta: $error")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CrearEventoViewModel", "Error HTTP: ${response.code()}, Body: $errorBody")
                    error = try {
                        val errorJson = com.google.gson.JsonParser.parseString(errorBody).asJsonObject
                        errorJson.get("message")?.asString ?: "Error al crear el evento"
                    } catch (e: Exception) {
                        "Error al crear el evento: ${response.message()}"
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