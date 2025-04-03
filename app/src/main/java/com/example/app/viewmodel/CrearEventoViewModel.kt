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
import com.example.app.model.evento.CategoriaEvento
import kotlinx.coroutines.launch
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CrearEventoViewModel : ViewModel() {
    var nombreEvento by mutableStateOf("")
    var descripcion by mutableStateOf("")
    var fechaEvento by mutableStateOf("")
    var hora by mutableStateOf("")
    var ubicacion by mutableStateOf("")
    var lugar by mutableStateOf("")
    var categoria by mutableStateOf("")
    var imagen by mutableStateOf<Uri?>(null)
    
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
        if (nombreEvento.isBlank() || descripcion.isBlank() || 
            fechaEvento.isBlank() || hora.isBlank() || 
            ubicacion.isBlank() || lugar.isBlank() || 
            categoria.isBlank()) {
            return false
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

    fun crearEvento() {
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

                // La fecha y hora ya están en el formato correcto gracias a la validación
                val eventoRequest = EventoRequest(
                    nombreEvento = nombreEvento,
                    descripcion = descripcion,
                    fechaEvento = fechaEvento,
                    hora = hora,
                    ubicacion = ubicacion,
                    lugar = lugar,
                    categoria = categoria,
                    imagen = imagen?.toString()
                )

                Log.d("CrearEventoViewModel", "Enviando request: $eventoRequest")
                val response = RetrofitClient.apiService.crearEvento("Bearer $token", eventoRequest)
                
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
                error = "Error de conexión: ${e.message}"
                Log.e("CrearEventoViewModel", "Excepción al crear evento", e)
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
        lugar = ""
        categoria = ""
        imagen = null
    }
}