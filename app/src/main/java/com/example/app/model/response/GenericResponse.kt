package com.example.app.model.response

/**
 * Respuesta genérica para varias operaciones de la API
 */
data class GenericResponse(
    val message: String = "",
    val status: String = "",
    val code: String = "",
    val error: String? = null
) 