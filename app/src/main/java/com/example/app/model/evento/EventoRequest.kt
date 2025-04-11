package com.example.app.model.evento

import com.google.gson.annotations.SerializedName

data class EventoRequest(
    @SerializedName("titulo") val titulo: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("hora") val hora: String,
    @SerializedName("ubicacion") val ubicacion: String,
    @SerializedName("categoria") val categoria: String,
    @SerializedName("es_online") val esOnline: Boolean = false,
    @SerializedName("tipos_entrada") val tiposEntrada: List<TipoEntradaRequest>
)