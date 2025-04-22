package com.example.app.model.login

import com.google.gson.annotations.SerializedName

/**
 * Respuesta del endpoint de redirección a Google
 */
data class GoogleAuthRedirectResponse(
    @SerializedName("url") val url: String
)

// Usamos el mismo nombre que la respuesta original para mantener compatibilidad
data class GoogleAuthUrlResponse(
    @SerializedName("url") val url: String
)

/**
 * Solicitud para autenticar con Google
 * Incluye todos los campos necesarios para el backend Laravel
 */
data class GoogleAuthRequest(
    @SerializedName("token") val token: String,
    @SerializedName("id") val id: String,
    @SerializedName("google_id") val googleId: String,
    @SerializedName("email") val email: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido1") val apellido1: String,
    @SerializedName("apellido2") val apellido2: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("email_verified_at") val emailVerifiedAt: String? = null,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("photo_url") val photoUrl: String? = null,
    @SerializedName("role") val role: String = "participante"
)
