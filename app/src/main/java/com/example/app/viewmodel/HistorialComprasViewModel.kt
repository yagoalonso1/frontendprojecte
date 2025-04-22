package com.example.app.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.api.RetrofitClient
import com.example.app.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistorialComprasViewModel : ViewModel() {
    private val TAG = "HistorialComprasViewModel"
    
    // Estado para las compras
    private val _compras = MutableStateFlow<List<CompraItem>>(emptyList())
    val compras = _compras.asStateFlow()
    
    // Estados de UI
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    
    // Navegación al login si la sesión expira
    private val _shouldNavigateToLogin = MutableStateFlow(false)
    val shouldNavigateToLogin = _shouldNavigateToLogin.asStateFlow()
    
    // Inicializar cargando el historial de compras
    init {
        loadHistorialCompras()
    }
    
    fun loadHistorialCompras() {
        viewModelScope.launch {
            try {
                isLoading = true
                clearError()
                
                // Obtener token
                val token = SessionManager.getToken()
                Log.d(TAG, "Token obtenido del SessionManager: $token")
                
                if (token.isNullOrEmpty()) {
                    Log.e(TAG, "Error: No hay token disponible")
                    setError("No se ha iniciado sesión")
                    _shouldNavigateToLogin.value = true
                    return@launch
                }
                
                // Realizar la petición para obtener el historial de compras
                val response = withContext(Dispatchers.IO) {
                    try {
                        RetrofitClient.apiService.getHistorialCompras("Bearer $token")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al realizar petición HTTP: ${e.message}", e)
                        return@withContext null
                    }
                }
                
                // Procesar la respuesta
                if (response == null) {
                    setError("Error de conexión al servidor")
                    return@launch
                }
                
                if (response.isSuccessful) {
                    val historialResponse = response.body()
                    Log.d(TAG, "Respuesta exitosa: ${historialResponse?.message}")
                    
                    if (historialResponse != null && historialResponse.status == "success") {
                        _compras.value = historialResponse.compras ?: emptyList()
                        Log.d(TAG, "Compras cargadas: ${_compras.value.size}")
                    } else {
                        Log.e(TAG, "Respuesta vacía o sin datos")
                        setError("No se pudieron obtener las compras")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Error ${response.code()}: $errorBody")
                    
                    when (response.code()) {
                        401 -> {
                            setError("Tu sesión ha expirado. Por favor, inicia sesión nuevamente")
                            _shouldNavigateToLogin.value = true
                        }
                        403 -> setError("No tienes permiso para acceder a esta información")
                        404 -> setError("No se encontraron compras")
                        500 -> setError("Error interno del servidor")
                        else -> setError("Error al cargar el historial: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en loadHistorialCompras: ${e.message}", e)
                setError("Error de conexión: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
    
    fun resetShouldNavigateToLogin() {
        _shouldNavigateToLogin.value = false
    }
    
    private fun setError(message: String) {
        errorMessage = message
    }
    
    private fun clearError() {
        errorMessage = null
    }
}

// Modelos de datos para el historial de compras
data class CompraItem(
    val id_compra: Int = 0,
    val evento: EventoCompra? = null,
    val entradas: List<EntradaCompra> = emptyList(),
    val total: Double = 0.0,
    val fecha_compra: String = "",
    val estado: String = ""
)

data class EventoCompra(
    val id: Int = 0,
    val nombre: String = "",
    val fecha: String = "",
    val hora: String = "",
    val imagen: String? = null
)

data class EntradaCompra(
    val id: Int = 0,
    val precio: Double = 0.0,
    val estado: String = "",
    val nombre_persona: String = "",
    val tipo_entrada: String = ""
) 