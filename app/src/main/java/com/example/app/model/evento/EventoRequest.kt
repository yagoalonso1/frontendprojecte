package com.example.app.model.evento

import com.google.gson.annotations.SerializedName

data class EventoRequest(
    @SerializedName("titulo") val nombreEvento: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("fecha") val fechaEvento: String,
    @SerializedName("hora") val hora: String,
    @SerializedName("ubicacion") val ubicacion: String,
    @SerializedName("categoria") val categoria: String,
    @SerializedName("es_online") val esOnline: Boolean = false,
    @SerializedName("tipos_entrada") val tiposEntrada: List<TipoEntradaRequest>
)

data class TipoEntradaRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("precio") val precio: Double,
    @SerializedName("cantidad_disponible") val cantidadDisponible: Int? = null,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("es_ilimitado") val esIlimitado: Boolean = false
)