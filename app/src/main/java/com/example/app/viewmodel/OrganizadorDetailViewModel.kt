package com.example.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.api.RetrofitClient
import com.example.app.model.Evento
import com.example.app.model.Organizador
import com.example.app.api.OrganizadorDetalle
import kotlinx.coroutines.launch
import android.util.Log

class OrganizadorDetailViewModel : ViewModel() {
    var eventos by mutableStateOf<List<Evento>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var isError by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var avatarUrl by mutableStateOf<String?>(null)
        private set
    var organizadorDetalle by mutableStateOf<OrganizadorDetalle?>(null)
        private set

    fun loadEventos(organizadorId: Int) {
        isLoading = true
        isError = false
        viewModelScope.launch {
            try {
                // Cargar eventos
                val response = RetrofitClient.apiService.getAllEventos()
                if (response.isSuccessful) {
                    val eventosResponse = response.body()?.eventos
                    eventos = eventosResponse?.filter { it.organizador?.id == organizadorId } ?: emptyList()
                    Log.d("OrganizadorVM", "Eventos cargados: ${eventos.size}")
                } else {
                    Log.e("OrganizadorVM", "Error al cargar eventos: ${response.code()}")
                }
                
                // Intentar cargar el organizador para obtener todos los datos
                try {
                    Log.d("OrganizadorVM", "Solicitando detalles del organizador ID: $organizadorId")
                    val organizadorResponse = RetrofitClient.apiService.getOrganizadorById(organizadorId.toString())
                    if (organizadorResponse.isSuccessful) {
                        val orgDetalle = organizadorResponse.body()?.organizador
                        if (orgDetalle != null) {
                            organizadorDetalle = orgDetalle
                            avatarUrl = orgDetalle.avatarUrl
                            Log.d("OrganizadorVM", "Datos cargados correctamente: ${orgDetalle.nombre}")
                            Log.d("OrganizadorVM", "Avatar URL: $avatarUrl")
                            Log.d("OrganizadorVM", "Teléfono: ${orgDetalle.telefonoContacto}")
                            Log.d("OrganizadorVM", "CIF: ${orgDetalle.cif}")
                            Log.d("OrganizadorVM", "Dirección: ${orgDetalle.direccionFiscal}")
                            
                            orgDetalle.user?.let { user ->
                                Log.d("OrganizadorVM", "Usuario: ${user.nombre}, Email: ${user.email}")
                            } ?: Log.d("OrganizadorVM", "No hay datos de usuario")
                        } else {
                            Log.e("OrganizadorVM", "Respuesta exitosa pero organizador nulo")
                            errorMessage = "No se encontraron datos del organizador"
                        }
                    } else {
                        Log.e("OrganizadorVM", "Error al cargar organizador: ${organizadorResponse.code()}")
                        errorMessage = "Error ${organizadorResponse.code()}: No se pudo obtener información del organizador"
                    }
                } catch (e: Exception) {
                    Log.e("OrganizadorVM", "Excepción al cargar organizador: ${e.message}")
                    errorMessage = "Error de conexión: ${e.message}"
                }
            } catch (e: Exception) {
                isError = true
                errorMessage = "Error: ${e.message}"
                Log.e("OrganizadorVM", "Excepción general: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
    
    // Función para convertir el OrganizadorDetalle a Organizador si es necesario
    fun getOrganizador(): Organizador? {
        return organizadorDetalle?.let {
            Organizador(
                id = it.id,
                nombre = it.nombre,
                telefonoContacto = it.telefonoContacto,
                direccionFiscal = it.direccionFiscal,
                cif = it.cif,
                nombreUsuario = it.nombreUsuario,
                user = it.user,
                avatarUrl = it.avatarUrl
            )
        }
    }
} 