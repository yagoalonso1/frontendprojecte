package com.example.app.model.register

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido1") val apellido1: String,
    @SerializedName("apellido2") val apellido2: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String? = null,
    @SerializedName("confirm_password") val confirmPassword: String? = null,
    @SerializedName("role") val role: String,
    @SerializedName("rol") val rol: String? = null,
    // Datos específicos de Organizador
    @SerializedName("nombre_organizacion") val nombreOrganizacion: String,
    @SerializedName("telefono_contacto") val telefonoContacto: String,
    // Datos específicos de Participante
    @SerializedName("dni") val dni: String,
    @SerializedName("telefono") val telefono: String,
    // Datos de Google Auth
    @SerializedName("google_token") val googleToken: String? = null
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
                confirmPassword = password,
                role = "participante",
                rol = "participante",
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
                confirmPassword = password,
                role = "organizador",
                rol = "organizador",
                nombreOrganizacion = nombreOrganizacion,
                telefonoContacto = telefonoContacto,
                dni = "N/A",                // Usar "N/A" en lugar de espacio en blanco
                telefono = "N/A"           // Usar "N/A" en lugar de espacio en blanco
            )
        }

        // Nuevo método para crear solicitud con datos de Google
        fun createWithGoogleAuth(
            nombre: String,
            apellido1: String,
            apellido2: String,
            email: String,
            googleToken: String?,
            password: String = java.util.UUID.randomUUID().toString().replace("-", ""),
            dni: String? = null,
            telefono: String? = null,
            role: String = "participante"
        ): RegisterRequest {
            // Asegurarnos de que siempre haya una contraseña
            val finalPassword = if (password.isBlank()) {
                // Generar una contraseña aleatoria si no se proporcionó una
                java.util.UUID.randomUUID().toString().replace("-", "")
            } else {
                password
            }
            
            return RegisterRequest(
                nombre = nombre,
                apellido1 = apellido1,
                apellido2 = apellido2,
                email = email,
                password = finalPassword,
                confirmPassword = finalPassword,
                role = role,
                rol = role,
                nombreOrganizacion = "N/A",
                telefonoContacto = "N/A",
                dni = dni ?: "N/A",
                telefono = telefono ?: "N/A",
                googleToken = googleToken
            )
        }
    }
}