package com.example.app.model

import com.google.gson.annotations.SerializedName

data class Evento(
    @SerializedName("id") val id: Int,
    @SerializedName("nombreEvento") val titulo: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("imagen") val imagen: String,
    @SerializedName("imagen_url") val imagenUrl: String? = null,
    @SerializedName("fechaEvento") val fechaEvento: String,
    @SerializedName("hora") val hora: String,
    @SerializedName("ubicacion") val ubicacion: String,
    @SerializedName("categoria") val categoria: String,
    @SerializedName("lugar") val lugar: String,
    @SerializedName("precio") val precio: Double,
    @SerializedName("organizador") val organizador: Organizador?,
    @SerializedName("isFavorito") val isFavorito: Boolean = false,
    @SerializedName("entradas") val entradas: List<TipoEntrada> = emptyList(),
    @SerializedName("es_online") val esOnline: Boolean = false
)

data class Organizador(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String
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