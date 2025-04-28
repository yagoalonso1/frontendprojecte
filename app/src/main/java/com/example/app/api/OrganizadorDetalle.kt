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
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("is_favorite") val isFavorite: Boolean = false
) 