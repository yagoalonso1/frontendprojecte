package com.example.app.viewmodel.favoritos

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.api.RetrofitClient
import com.example.app.model.ErrorResponse
import com.example.app.model.Evento
import com.example.app.model.favoritos.FavoritoRequest
import com.example.app.util.SessionManager
import com.google.gson.Gson
import kotlinx.coroutines.launch

class FavoritosViewModel : ViewModel() {
    // Lista de favoritos
    var favoritos by mutableStateOf<List<Evento>>(emptyList())
        private set
    
    // UI
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    var isError by mutableStateOf(false)
        private set

    // Token
    private val token: String
        get() = "Bearer ${SessionManager.getToken() ?: ""}"

    init {
        loadFavoritos()
    }

    fun loadFavoritos() {
        viewModelScope.launch {
            isLoading = true
            isError = false
            errorMessage = null
            
            if (!SessionManager.isLoggedIn()) {
                isLoading = false
                isError = true
                errorMessage = "Debes iniciar sesión para ver tus favoritos"
                favoritos = emptyList()
                return@launch
            }
            
            try {
                Log.d("FavoritosViewModel", "Cargando favoritos...")
                val response = RetrofitClient.apiService.getFavoritos(token)
                if (response.isSuccessful) {
                    response.body()?.let { favoritosResponse ->
                        val eventosFromResponse = when {
                            // Primera opción: eventos en el campo "eventos"
                            favoritosResponse.eventos != null && favoritosResponse.eventos.isNotEmpty() -> {
                                Log.d("FavoritosViewModel", "Obteniendo eventos del campo 'eventos'")
                                favoritosResponse.eventos
                            }
                            // Segunda opción: favoritos con campo "evento" anidado
                            favoritosResponse.favoritos != null && favoritosResponse.favoritos.isNotEmpty() -> {
                                Log.d("FavoritosViewModel", "Obteniendo eventos del campo 'favoritos'")
                                // Mapear cada favorito a su evento
                                favoritosResponse.favoritos.mapNotNull { it.evento }
                            }
                            // No se encontraron datos
                            else -> {
                                Log.d("FavoritosViewModel", "No se encontraron favoritos en la respuesta")
                                emptyList()
                            }
                        }
                        
                        // Actualizar el estado
                        favoritos = eventosFromResponse
                        Log.d("FavoritosViewModel", "Favoritos cargados: ${favoritos.size}")
                    } ?: run {
                        favoritos = emptyList()
                        Log.d("FavoritosViewModel", "Respuesta exitosa pero body nulo")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                    errorMessage = errorResponse?.message ?: "Error desconocido"
                    isError = true
                    Log.e("FavoritosViewModel", "Error al cargar favoritos: $errorMessage")
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error desconocido"
                isError = true
                Log.e("FavoritosViewModel", "Excepción al cargar favoritos", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun toggleFavorito(eventoId: Int) {
        viewModelScope.launch {
            if (!SessionManager.isLoggedIn()) {
                errorMessage = "Debes iniciar sesión para gestionar favoritos"
                isError = true
                return@launch
            }
            
            try {
                Log.d("FavoritosViewModel", "Verificando estado de favorito para evento $eventoId")
                val response = RetrofitClient.apiService.checkFavorito(token, eventoId)
                if (response.isSuccessful) {
                    val isFavorito = response.body()?.isFavorito ?: false
                    Log.d("FavoritosViewModel", "Estado actual: isFavorito=$isFavorito")
                    
                    if (isFavorito) {
                        removeFavorito(eventoId)
                    } else {
                        addFavorito(eventoId)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                    errorMessage = errorResponse?.message ?: "Error desconocido"
                    isError = true
                    Log.e("FavoritosViewModel", "Error al verificar favorito: $errorMessage")
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error desconocido"
                isError = true
                Log.e("FavoritosViewModel", "Excepción al cambiar favorito", e)
            }
        }
    }

    private suspend fun addFavorito(eventoId: Int) {
        try {
            Log.d("FavoritosViewModel", "Añadiendo evento $eventoId a favoritos")
            val request = FavoritoRequest(eventoId)
            val response = RetrofitClient.apiService.addFavorito(token, request)
            if (response.isSuccessful) {
                Log.d("FavoritosViewModel", "Evento añadido a favoritos correctamente")
                loadFavoritos()
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                errorMessage = errorResponse?.message ?: "Error desconocido"
                isError = true
                Log.e("FavoritosViewModel", "Error al añadir favorito: $errorMessage")
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Error desconocido"
            isError = true
            Log.e("FavoritosViewModel", "Excepción al añadir favorito", e)
        }
    }

    private suspend fun removeFavorito(eventoId: Int) {
        try {
            Log.d("FavoritosViewModel", "Eliminando evento $eventoId de favoritos")
            val response = RetrofitClient.apiService.removeFavorito(token, eventoId)
            if (response.isSuccessful) {
                Log.d("FavoritosViewModel", "Evento eliminado de favoritos correctamente")
                
                // Actualizar el estado local inmediatamente antes de recargar
                favoritos = favoritos.filter { it.getEventoId() != eventoId }
                
                // Luego recargamos para mantener sincronía con el servidor
                loadFavoritos()
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                errorMessage = errorResponse?.message ?: "Error desconocido"
                isError = true
                Log.e("FavoritosViewModel", "Error al eliminar favorito: $errorMessage")
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Error desconocido"
            isError = true
            Log.e("FavoritosViewModel", "Excepción al eliminar favorito", e)
        }
    }
} 