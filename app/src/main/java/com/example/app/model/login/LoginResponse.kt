package com.example.app.model.login

import com.example.app.model.User
import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("message") val message: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("user") val user: User,
    @SerializedName("profile_data") val profileData: ProfileData? = null,
    @SerializedName("auth") val auth: Auth,
    @SerializedName("role") val role: String = "",
    @SerializedName("userRole") val userRole: String = "",
    @SerializedName("user_role") val userRoleAlt: String = ""
)

data class Auth(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String
)

data class ProfileData(
    @SerializedName("idOrganizador") val idOrganizador: Int? = null,
    @SerializedName("nombre_organizacion") val nombreOrganizacion: String? = null,
    @SerializedName("telefono_contacto") val telefonoContacto: String? = null
)