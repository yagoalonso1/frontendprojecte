package com.example.app.viewmodel.favoritos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.api.RetrofitClient
import com.example.app.model.ErrorResponse
import com.example.app.model.Evento
import com.example.app.model.favoritos.FavoritoRequest
import com.example.app.utils.TokenManager
import com.google.gson.Gson
import kotlinx.coroutines.launch

class FavoritosViewModel : ViewModel() {
    var favoritos by mutableStateOf<List<Evento>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    var isError by mutableStateOf(false)
        private set

    private val token: String
        get() = "Bearer ${TokenManager.getStoredToken()}"

    init {
        loadFavoritos()
    }

    fun loadFavoritos() {
        viewModelScope.launch {
            isLoading = true
            isError = false
            errorMessage = null
            
            try {
                val response = RetrofitClient.apiService.getFavoritos(token)
                if (response.isSuccessful) {
                    response.body()?.let { favoritosResponse ->
                        favoritos = favoritosResponse.eventos ?: emptyList()
                    } ?: run {
                        favoritos = emptyList()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                    errorMessage = errorResponse?.message ?: "Error desconocido"
                    isError = true
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error desconocido"
                isError = true
            } finally {
                isLoading = false
            }
        }
    }

    fun toggleFavorito(eventoId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.checkFavorito(token, eventoId)
                if (response.isSuccessful) {
                    val isFavorito = response.body()?.isFavorito ?: false
                    if (isFavorito) {
                        removeFavorito(eventoId)
                    } else {
                        addFavorito(eventoId)
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.message
                isError = true
            }
        }
    }

    private suspend fun addFavorito(eventoId: Int) {
        try {
            val request = FavoritoRequest(eventoId)
            val response = RetrofitClient.apiService.addFavorito(token, request)
            if (response.isSuccessful) {
                loadFavoritos()
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                errorMessage = errorResponse?.message
                isError = true
            }
        } catch (e: Exception) {
            errorMessage = e.message
            isError = true
        }
    }

    private suspend fun removeFavorito(eventoId: Int) {
        try {
            val response = RetrofitClient.apiService.removeFavorito(token, eventoId)
            if (response.isSuccessful) {
                loadFavoritos()
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                errorMessage = errorResponse?.message
                isError = true
            }
        } catch (e: Exception) {
            errorMessage = e.message
            isError = true
        }
    }
} 