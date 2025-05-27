package com.example.app.model

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ProfileData? = null,
    @SerializedName("status") val status: String? = null
)

data class ProfileData(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("apellido1") val apellido1: String? = null,
    @SerializedName("apellido2") val apellido2: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("tipo_usuario") val tipoUsuario: String? = null,
    // Campos específicos de participante
    @SerializedName("dni") val dni: String? = null,
    @SerializedName("telefono") val telefono: String? = null,
    // Campos específicos de organizador
    @SerializedName("nombre_organizacion") val nombreOrganizacion: String? = null,
    @SerializedName("telefono_contacto") val telefonoContacto: String? = null,
    @SerializedName("avatar") val avatar: String? = null
) 