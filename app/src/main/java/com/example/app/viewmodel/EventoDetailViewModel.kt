package com.example.app.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.api.RetrofitClient
import com.example.app.model.Evento
import com.example.app.model.TipoEntradaDetalle
import com.example.app.model.CompraRequest
import com.example.app.model.EntradaCompra
import com.example.app.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventoDetailViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    // ID evento
    private val eventoId: String = checkNotNull(savedStateHandle["eventoId"])
    
    // Evento
    var evento by mutableStateOf<Evento?>(null)
        private set
    
    // Tipos de entrada detallados
    var tiposEntrada by mutableStateOf<List<TipoEntradaDetalle>>(emptyList())
        private set
    
    // Cantidades seleccionadas por tipo de entrada
    var cantidadesSeleccionadas by mutableStateOf<Map<Int, Int>>(emptyMap())
        private set
    
    // UI
    var isLoading by mutableStateOf(true)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isError by mutableStateOf(false)
        private set
    
    // Estados para el proceso de compra
    private val _showPaymentDialog = MutableStateFlow(false)
    val showPaymentDialog: StateFlow<Boolean> = _showPaymentDialog
    
    private val _compraProcesando = MutableStateFlow(false)
    val compraProcesando: StateFlow<Boolean> = _compraProcesando
    
    private val _compraExitosa = MutableStateFlow(false)
    val compraExitosa: StateFlow<Boolean> = _compraExitosa
    
    private val _mensajeCompra = MutableStateFlow("")
    val mensajeCompra: StateFlow<String> = _mensajeCompra
    
    init {
        Log.d("EventoDetailViewModel", "Inicializando con eventoId: $eventoId")
        loadEvento()
    }
    
    fun loadEvento() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null
                isError = false
                
                Log.d("EventoDetailViewModel", "Cargando evento con ID: $eventoId")
                
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getEventoById(eventoId)
                }
                
                if (response.isSuccessful && response.body() != null) {
                    evento = response.body()?.evento
                    Log.d("EventoDetailViewModel", "Evento cargado exitosamente: ${evento?.titulo}")
                    
                    // Cargar tipos de entrada detallados
                    loadTiposEntrada()
                } else {
                    val error = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("EventoDetailViewModel", "Error al cargar evento: $error")
                    setError("No se pudo cargar el evento: Error en la respuesta")
                }
            } catch (e: Exception) {
                Log.e("EventoDetailViewModel", "Excepción al cargar evento", e)
                setError("Error de conexión: ${e.localizedMessage}")
            } finally {
                isLoading = false
            }
        }
    }
    
    private fun loadTiposEntrada() {
        viewModelScope.launch {
            try {
                Log.d("EventoDetailViewModel", "Cargando tipos de entrada para evento ID: $eventoId")
                
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getTiposEntrada(eventoId)
                }
                
                if (response.isSuccessful && response.body() != null) {
                    tiposEntrada = response.body()?.tiposEntrada ?: emptyList()
                    Log.d("EventoDetailViewModel", "Tipos de entrada cargados exitosamente: ${tiposEntrada.size}")
                    
                    // Inicializar cantidades seleccionadas
                    val cantidadesIniciales = tiposEntrada.associate { it.id to 0 }
                    cantidadesSeleccionadas = cantidadesIniciales
                } else {
                    val error = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("EventoDetailViewModel", "Error al cargar tipos de entrada: $error")
                    // No establecemos error general para no interrumpir la visualización del evento
                }
            } catch (e: Exception) {
                Log.e("EventoDetailViewModel", "Excepción al cargar tipos de entrada", e)
                // No establecemos error general para no interrumpir la visualización del evento
            }
        }
    }
    
    fun incrementarCantidad(tipoEntradaId: Int) {
        val cantidadActual = cantidadesSeleccionadas[tipoEntradaId] ?: 0
        val tipoEntrada = tiposEntrada.find { it.id == tipoEntradaId }
        
        // Comprobar si se puede incrementar (disponibilidad)
        if (tipoEntrada != null) {
            val disponibles = tipoEntrada.disponibilidad ?: 
                (tipoEntrada.cantidadDisponible?.minus(tipoEntrada.entradasVendidas) ?: 0)
            
            if (tipoEntrada.esIlimitado || cantidadActual < disponibles) {
                cantidadesSeleccionadas = cantidadesSeleccionadas.toMutableMap().apply {
                    put(tipoEntradaId, cantidadActual + 1)
                }
            }
        }
    }
    
    fun decrementarCantidad(tipoEntradaId: Int) {
        val cantidadActual = cantidadesSeleccionadas[tipoEntradaId] ?: 0
        if (cantidadActual > 0) {
            cantidadesSeleccionadas = cantidadesSeleccionadas.toMutableMap().apply {
                put(tipoEntradaId, cantidadActual - 1)
            }
        }
    }
    
    fun obtenerCantidad(tipoEntradaId: Int): Int {
        return cantidadesSeleccionadas[tipoEntradaId] ?: 0
    }
    
    fun calcularTotal(): Double {
        var total = 0.0
        tiposEntrada.forEach { tipoEntrada ->
            val cantidad = cantidadesSeleccionadas[tipoEntrada.id] ?: 0
            if (cantidad > 0) {
                val precio = tipoEntrada.precio.toDoubleOrNull() ?: 0.0
                total += precio * cantidad
            }
        }
        return total
    }
    
    fun hayEntradasSeleccionadas(): Boolean {
        return cantidadesSeleccionadas.values.any { it > 0 }
    }
    
    fun mostrarDialogoPago() {
        _showPaymentDialog.value = true
    }
    
    fun cerrarDialogoPago() {
        _showPaymentDialog.value = false
    }
    
    fun realizarCompra() {
        viewModelScope.launch {
            try {
                _compraProcesando.value = true
                
                // Obtener token
                val token = SessionManager.getToken()
                if (token == null) {
                    _mensajeCompra.value = "Debes iniciar sesión para comprar entradas"
                    return@launch
                }
                
                // Crear solicitud de compra
                val entradas = cantidadesSeleccionadas
                    .filter { it.value > 0 }
                    .map { 
                        val tipoEntrada = tiposEntrada.find { tipo -> tipo.id == it.key }
                        val precio = tipoEntrada?.precio?.toDoubleOrNull() ?: 0.0
                        EntradaCompra(
                            idTipoEntrada = it.key, 
                            cantidad = it.value,
                            precio = precio
                        ) 
                    }
                
                if (entradas.isEmpty()) {
                    _mensajeCompra.value = "No has seleccionado ninguna entrada"
                    return@launch
                }
                
                val compraRequest = CompraRequest(
                    idEvento = evento?.id ?: return@launch,
                    entradas = entradas
                )
                
                // Realizar petición
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.comprarEntradas("Bearer $token", compraRequest)
                }
                
                if (response.isSuccessful) {
                    // Compra exitosa
                    _compraExitosa.value = true
                    _mensajeCompra.value = "¡Compra realizada con éxito!"
                    
                    // Reiniciar cantidades
                    cantidadesSeleccionadas = tiposEntrada.associate { it.id to 0 }
                } else {
                    // Error en la compra
                    val errorBody = response.errorBody()?.string()
                    Log.e("EventoDetailViewModel", "Error al realizar compra: $errorBody")
                    _mensajeCompra.value = when (response.code()) {
                        422 -> "Error: Datos de compra inválidos o entradas agotadas"
                        401, 403 -> "Error: No tienes permisos para realizar esta compra"
                        404 -> "Error: Evento no encontrado"
                        else -> "Error al realizar la compra: ${errorBody ?: "Error desconocido"}"
                    }
                }
            } catch (e: Exception) {
                // Excepción
                Log.e("EventoDetailViewModel", "Excepción al realizar compra", e)
                _mensajeCompra.value = "Error de conexión: ${e.localizedMessage}"
            } finally {
                _compraProcesando.value = false
                // Cerrar diálogo después de 3 segundos si fue exitoso
                if (_compraExitosa.value) {
                    kotlinx.coroutines.delay(3000)
                    _showPaymentDialog.value = false
                    _compraExitosa.value = false
                }
            }
        }
    }
    
    private fun setError(error: String) {
        errorMessage = error
        isError = true
        Log.e("EventoDetailViewModel", "Error establecido: $error")
    }
} 