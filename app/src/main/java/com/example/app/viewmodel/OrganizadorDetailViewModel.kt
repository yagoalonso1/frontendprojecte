package com.example.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.api.RetrofitClient
import com.example.app.model.Evento
import kotlinx.coroutines.launch

class OrganizadorDetailViewModel : ViewModel() {
    var eventos by mutableStateOf<List<Evento>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var isError by mutableStateOf(false)
        private set

    fun loadEventos(organizadorId: Int) {
        isLoading = true
        isError = false
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getAllEventos()
                if (response.isSuccessful) {
                    val eventosResponse = response.body()?.eventos
                    eventos = eventosResponse?.filter { it.organizador?.id == organizadorId } ?: emptyList()
                } else {
                    isError = true
                }
            } catch (e: Exception) {
                isError = true
            } finally {
                isLoading = false
            }
        }
    }
} 