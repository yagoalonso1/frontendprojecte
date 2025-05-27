package com.example.app.model

import com.google.gson.annotations.SerializedName
import android.util.Log

data class OrganizadorEntity(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("nombre_organizacion")
    val nombre: String = "",
    
    @SerializedName("telefono_contacto")
    val telefonoContacto: String = "",
    
    @SerializedName("direccion_fiscal")
    val direccionFiscal: String = "",
    
    @SerializedName("cif")
    val cif: String = "",
    
    @SerializedName("user")
    val user: UserInfo? = null,
    
    @SerializedName("avatar")
    val avatar: String? = null
)

data class UserInfo(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("nombre")
    val nombre: String = "",
    
    @SerializedName("apellido1")
    val apellido1: String? = null,
    
    @SerializedName("apellido2")
    val apellido2: String? = null,
    
    @SerializedName("email")
    val email: String = "",
    
    @SerializedName("avatar")
    val avatar: String? = null
)

data class Organizador(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre_organizacion") val nombre: String,
    @SerializedName("telefono_contacto") val telefonoContacto: String,
    @SerializedName("direccion_fiscal") val direccionFiscal: String? = null,
    @SerializedName("cif") val cif: String? = null,
    @SerializedName("nombre_usuario") val nombreUsuario: String? = null,
    @SerializedName("user") val user: UserInfo?,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("is_favorite") val isFavorite: Boolean = false
) {
    // Función que actualiza si el organizador es favorito o no
    fun copyWithFavorite(isFavorite: Boolean): Organizador {
        return Organizador(
            id = this.id,
            nombre = this.nombre,
            telefonoContacto = this.telefonoContacto,
            direccionFiscal = this.direccionFiscal,
            cif = this.cif,
            nombreUsuario = this.nombreUsuario,
            user = this.user,
            avatar = this.avatar,
            isFavorite = isFavorite
        )
    }
    
    // Función para obtener la URL completa del avatar
    fun obtenerAvatarUrl(): String {
        return user?.avatar ?: avatar ?: "https://ui-avatars.com/api/?name=${nombre.take(1)}&background=random"
    }
}

// Función global para convertir una OrganizadorEntity a un Organizador
fun OrganizadorEntity.toOrganizador(): Organizador {
    return Organizador(
        id = this.id,
        nombre = this.nombre,
        telefonoContacto = this.telefonoContacto,
        direccionFiscal = this.direccionFiscal,
        cif = this.cif,
        nombreUsuario = null,
        user = this.user,
        avatar = this.avatar
    )
}

// Función para obtener la URL del avatar con formato consistente (para usar desde fuera de la clase)
fun getOrganizadorAvatarUrl(organizador: Organizador, viewModelUrl: String? = null): String {
    val defaultUrl = "https://ui-avatars.com/api/?name=${organizador.nombre.take(1)}&background=random"
    
    return when {
        !organizador.avatar.isNullOrBlank() -> organizador.avatar
        !viewModelUrl.isNullOrBlank() -> viewModelUrl
        organizador.user?.avatar != null -> organizador.user.avatar
        else -> defaultUrl
    }
}

// Función para obtener la URL del avatar con formato consistente
fun getValidAvatarUrl(url: String?): String {
    // URL por defecto si es null o vacío
    val defaultUrl = "https://example.com/storage/avatars/default_avatar.png"
    
    if (url.isNullOrBlank()) {
        Log.d("AvatarUrl", "Usando URL por defecto: $defaultUrl")
        return defaultUrl
    }
    
    // Si la URL ya empieza con http:// o https://, se usa directamente
    if (url.startsWith("http://") || url.startsWith("https://")) {
        Log.d("AvatarUrl", "Usando URL completa: $url")
        return url
    }
    
    // Si no tiene protocolo, añadir https://
    val formattedUrl = "https://$url"
    Log.d("AvatarUrl", "Formateando URL: $formattedUrl")
    return formattedUrl
}

// Función para obtener la URL del avatar con formato consistente
fun obtenerAvatarUrl(organizador: Organizador): String {
    return organizador.avatar ?: "https://ui-avatars.com/api/?name=${organizador.nombre}&background=random"
}