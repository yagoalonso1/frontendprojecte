package com.example.app.api

import com.example.app.model.UserInfo
import com.google.gson.annotations.SerializedName

data class OrganizadorDetalle(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre_organizacion") val nombre: String,
    @SerializedName("telefono_contacto") val telefonoContacto: String,
    @SerializedName("direccion_fiscal") val direccionFiscal: String? = null,
    @SerializedName("cif") val cif: String? = null,
    @SerializedName("nombre_usuario") val nombreUsuario: String? = null,
    @SerializedName("user") val user: com.example.app.model.UserInfo?,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("is_favorite") val isFavorite: Boolean = false
) {
    // Función para obtener la URL del avatar de forma segura
    fun obtenerAvatarUrl(): String {
        return when {
            !avatar.isNullOrEmpty() -> {
                if (avatar.startsWith("http")) {
                    avatar
                } else {
                    "https://api.example.com/storage/$avatar"
                }
            }
            user?.avatar != null -> {
                if (user.avatar.startsWith("http")) {
                    user.avatar
                } else {
                    "https://api.example.com/storage/${user.avatar}"
                }
            }
            else -> "https://ui-avatars.com/api/?name=${nombre.take(2)}&background=0D8ABC&color=fff&size=256"
        }
    }
} 