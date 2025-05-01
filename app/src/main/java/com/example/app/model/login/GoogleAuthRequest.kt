package com.example.app.model.login

import com.google.gson.annotations.SerializedName

data class GoogleAuthRequest(
    @SerializedName("email") val email: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido1") val apellido1: String,
    @SerializedName("apellido2") val apellido2: String?,
    @SerializedName("photo_url") val photoUrl: String?,
    @SerializedName("token") val token: String,
    @SerializedName("id") val id: String,
    @SerializedName("google_id") val googleId: String,
    @SerializedName("password") val password: String = java.util.UUID.randomUUID().toString().replace("-", "")
) 