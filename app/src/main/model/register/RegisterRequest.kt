package com.example.app.model.register

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido1") val apellido1: String,
    @SerializedName("apellido2") val apellido2: String?,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String,
    // Datos específicos de Organizador
    @SerializedName("nombre_organizacion") val nombreOrganizacion: String?,
    @SerializedName("telefono_contacto") val telefonoContacto: String?,
    // Datos específicos de Participante
    @SerializedName("dni") val dni: String?,
    @SerializedName("telefono") val telefono: String?
)
