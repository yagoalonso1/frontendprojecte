package com.example.app.model.response

import com.example.app.model.Evento

/**
 * Respuesta del API para la lista de eventos del organizador
 */
data class MisEventosResponse(
    val message: String = "",
    val eventos: List<Evento> = emptyList(),
    val total: Int = 0,
    val status: String = ""
) 