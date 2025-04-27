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
    
    @SerializedName("avatar_url")
    val avatarUrl: String? = null
)

data class UserInfo(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("name")
    val nombre: String = "",
    
    @SerializedName("email")
    val email: String = "",
    
    @SerializedName("avatar_url")
    val avatarUrl: String? = null
)

data class Organizador(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre_organizacion") val nombre: String,
    @SerializedName("telefono_contacto") val telefonoContacto: String,
    @SerializedName("direccion_fiscal") val direccionFiscal: String? = null,
    @SerializedName("cif") val cif: String? = null,
    @SerializedName("nombre_usuario") val nombreUsuario: String? = null,
    @SerializedName("user") val user: UserInfo?,
    @SerializedName("avatar_url") val avatarUrl: String? = null
)

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
        avatarUrl = this.avatarUrl
    )
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

// Extensión para obtener la URL del avatar del organizador
fun Organizador.getAvatarUrl(viewModelUrl: String? = null): String {
    val defaultAvatarUrl = "https://example.com/storage/avatars/default_avatar.png"
    
    return when {
        !this.avatarUrl.isNullOrBlank() -> {
            Log.d("Organizador", "Usando avatarUrl del modelo: ${this.avatarUrl}")
            this.avatarUrl
        }
        !viewModelUrl.isNullOrBlank() -> {
            Log.d("Organizador", "Usando avatarUrl del viewModel: $viewModelUrl")
            viewModelUrl
        }
        user?.avatarUrl != null -> {
            Log.d("Organizador", "Usando avatarUrl del usuario: ${user.avatarUrl}")
            user.avatarUrl
        }
        else -> {
            Log.d("Organizador", "Usando avatarUrl por defecto")
            defaultAvatarUrl
        }
    }
}