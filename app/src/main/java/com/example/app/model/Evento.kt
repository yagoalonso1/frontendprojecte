package com.example.app.model

import com.google.gson.annotations.SerializedName
import android.util.Log

data class Evento(
    @SerializedName(value = "id", alternate = ["idEvento"]) val idEvento: Int? = null,
    @SerializedName("nombreEvento") val titulo: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("imagen") val imagen: String,
    @SerializedName("imagen_url") val imagenUrl: String? = null,
    @SerializedName("fechaEvento") val fechaEvento: String,
    @SerializedName("hora") val hora: String,
    @SerializedName("ubicacion") val ubicacion: String,
    @SerializedName("categoria") val categoria: String,
    @SerializedName("lugar") val lugar: String,
    @SerializedName("precio") val precio: Double = 0.0,
    @SerializedName("organizador") val organizador: OrganizadorEvento?,
    @SerializedName("isFavorito") val isFavorito: Boolean = false,
    @SerializedName("entradas") val entradas: List<TipoEntrada> = emptyList(),
    @SerializedName("es_online") val esOnline: Boolean = false,
    @SerializedName("precio_minimo") val precioMinimo: Double? = null,
    @SerializedName("precio_maximo") val precioMaximo: Double? = null
) {
    fun getEventoId(): Int = idEvento ?: -1
}

data class OrganizadorEvento(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre_organizacion") val nombre: String,
    @SerializedName("telefono_contacto") val telefonoContacto: String,
    @SerializedName("direccion_fiscal") val direccionFiscal: String? = null,
    @SerializedName("cif") val cif: String? = null,
    @SerializedName("user") val user: com.example.app.model.UserInfo?,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("avatar") val avatar: String? = null
)

data class TipoEntrada(
    @SerializedName("id") val id: Int,
    @SerializedName("tipo") val nombre: String,
    @SerializedName("precio") val precio: Double,
    @SerializedName("cantidad_disponible") val cantidadDisponible: Int?,
    @SerializedName("entradas_vendidas") val entradasVendidas: Int = 0,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("es_ilimitado") val esIlimitado: Boolean = false
)

fun Evento.getImageUrl(): String {
    Log.d("Evento", "getImageUrl() - ID: ${this.getEventoId()}, tipo: ${this.getEventoId().javaClass.name}")
    
    return if (!this.imagenUrl.isNullOrBlank()) {
        Log.d("Evento", "Usando URL directa: ${this.imagenUrl}")
        this.imagenUrl
    } else if (!this.imagen.isNullOrBlank()) {
        val baseUrl = "https://example.com/storage/" // o la URL base que corresponda
        val urlFinal = baseUrl + this.imagen
        Log.d("Evento", "Usando URL construida: $urlFinal")
        urlFinal
    } else {
        val urlDefault = "https://example.com/storage/eventos/default.jpg" // o la URL por defecto
        Log.d("Evento", "Usando URL por defecto: $urlDefault")
        urlDefault
    }
}

/**
 * Método de extensión para obtener la URL del avatar del organizador
 * Prioridad: avatarUrl > avatar > avatar del usuario > avatar generado
 */
fun OrganizadorEvento.getAvatarUrl(): String {
    val result = when {
        !avatarUrl.isNullOrEmpty() -> {
            // Si ya tenemos una URL, la usamos directamente
            Log.d("OrganizadorEvento", "Usando avatarUrl: $avatarUrl")
            avatarUrl
        }
        !avatar.isNullOrEmpty() -> {
            // Si tenemos avatar, lo usamos directamente
            Log.d("OrganizadorEvento", "Usando avatar: $avatar")
            avatar
        }
        user?.avatar != null -> {
            // Si tenemos avatar de usuario, lo usamos
            Log.d("OrganizadorEvento", "Usando avatar de user: ${user.avatar}")
            user.avatar
        }
        else -> {
            // Si no hay avatar, generamos uno con las iniciales del nombre de la organización
            val initials = nombre.take(2).uppercase()
            val generatedUrl = "https://ui-avatars.com/api/?name=$initials&background=random&color=fff&size=128"
            Log.d("OrganizadorEvento", "Generando avatar con iniciales: $initials, URL: $generatedUrl")
            generatedUrl
        }
    }
    
    Log.d("OrganizadorEvento", "URL final del avatar: $result")
    return result
}