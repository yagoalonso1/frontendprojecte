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
    @SerializedName("isFavorito") val isFavorito: Boolean = false
)

data class Organizador(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String
)