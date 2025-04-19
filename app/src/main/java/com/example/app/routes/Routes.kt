package com.example.app.routes

import com.example.app.util.isValidEventoId
import com.example.app.util.getEventoIdErrorMessage

sealed class Routes(val route: String) {
    object Login : Routes("login")
    object Register : Routes("register")
    object RegisterOrganizador : Routes("register/organizador")
    object RegisterParticipante : Routes("register/participante")
    object Eventos : Routes("eventos")
    object MisEventos : Routes("mis_eventos")
    object CrearEvento : Routes("crear_evento")
    object MisTickets : Routes("mis_tickets")
    object Favoritos : Routes("favoritos")
    object Perfil : Routes("perfil")
    
    // Ruta con parámetros
    object EventoDetalle : Routes("evento_detalle/{eventoId}") {
        fun createRoute(eventoId: String): String {
            val id = eventoId.trim()
            if (!id.isValidEventoId()) {
                val errorMsg = id.getEventoIdErrorMessage()
                android.util.Log.e("Routes", errorMsg)
                throw IllegalArgumentException(errorMsg)
            }
            android.util.Log.d("Routes", "Creando ruta para detalle de evento: $id")
            return "evento_detalle/$id"
        }
    }
    
    object EditarEvento : Routes("editar_evento/{eventoId}") {
        fun createRoute(eventoId: String): String {
            val id = eventoId.trim()
            
            android.util.Log.d("Routes", "Creando ruta EditarEvento con ID: '$id' (${id.javaClass.name})")
            
            if (!id.isValidEventoId()) {
                val errorMsg = id.getEventoIdErrorMessage()
                android.util.Log.e("Routes", errorMsg)
                throw IllegalArgumentException(errorMsg)
            }
            
            val rutaFinal = "editar_evento/$id"
            android.util.Log.d("Routes", "Ruta final creada: $rutaFinal")
            return rutaFinal
        }
    }
    
    object ForgotPassword : Routes("forgot_password")
}