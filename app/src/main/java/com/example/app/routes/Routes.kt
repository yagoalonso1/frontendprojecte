package com.example.app.routes

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
        fun createRoute(eventoId: String) = "evento_detalle/$eventoId"
    }
    
    object ForgotPassword : Routes("forgot_password")
}