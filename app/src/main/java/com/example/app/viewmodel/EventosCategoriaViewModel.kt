package com.example.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.api.RetrofitClient
import com.example.app.model.Evento
import kotlinx.coroutines.launch

class EventosCategoriaViewModel : ViewModel() {
    var eventos by mutableStateOf<List<Evento>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadEventosByCategoria(categoria: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val categoriaNormalizada = categoria.lowercase().replaceFirstChar { it.uppercase() }
                val response = RetrofitClient.apiService.getEventosByCategoria(categoriaNormalizada)
                if (response.isSuccessful) {
                    eventos = response.body()?.eventos ?: emptyList()
                    if (eventos.isEmpty()) {
                        errorMessage = "No hay eventos en esta categoría"
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    errorMessage = errorBody ?: "Error al cargar eventos"
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
}
