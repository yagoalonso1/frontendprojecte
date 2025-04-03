package com.example.app.model.register

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido1") val apellido1: String,
    @SerializedName("apellido2") val apellido2: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String,
    // Datos específicos de Organizador
    @SerializedName("nombre_organizacion") val nombreOrganizacion: String,
    @SerializedName("telefono_contacto") val telefonoContacto: String,
    // Datos específicos de Participante
    @SerializedName("dni") val dni: String,
    @SerializedName("telefono") val telefono: String
) {
    companion object {
        fun createParticipante(
            nombre: String,
            apellido1: String,
            apellido2: String,
            email: String,
            password: String,
            dni: String,
            telefono: String
        ): RegisterRequest {
            return RegisterRequest(
                nombre = nombre,
                apellido1 = apellido1,
                apellido2 = apellido2,
                email = email,
                password = password,
                role = "participante",
                dni = dni,
                telefono = telefono,
                nombreOrganizacion = "N/A",  // Usar "N/A" en lugar de espacio en blanco
                telefonoContacto = "N/A"     // Usar "N/A" en lugar de espacio en blanco
            )
        }

        fun createOrganizador(
            nombre: String,
            apellido1: String,
            apellido2: String,
            email: String,
            password: String,
            nombreOrganizacion: String,
            telefonoContacto: String
        ): RegisterRequest {
            return RegisterRequest(
                nombre = nombre,
                apellido1 = apellido1,
                apellido2 = apellido2,
                email = email,
                password = password,
                role = "organizador",
                nombreOrganizacion = nombreOrganizacion,
                telefonoContacto = telefonoContacto,
                dni = "N/A",                // Usar "N/A" en lugar de espacio en blanco
                telefono = "N/A"           // Usar "N/A" en lugar de espacio en blanco
            )
        }
    }
}