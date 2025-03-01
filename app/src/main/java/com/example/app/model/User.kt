package com.example.app.model

data class User(
    val name: String,
    val email: String,
    val password: String,
    val password_confirmation: String,
    val role: String,
    // Campos adicionales para Organizador
    val nombre_organizacion: String? = null,
    val telefono_contacto: String? = null,
    // Campos adicionales para Participante
    val dni: String? = null,
    val telefono: String? = null
)