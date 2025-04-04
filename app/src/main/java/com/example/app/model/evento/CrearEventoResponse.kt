package com.example.app.model.evento

import com.example.app.model.Evento
import com.google.gson.annotations.SerializedName

data class CrearEventoResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("evento") val evento: EventoCreado? = null
)

data class EventoCreado(
    @SerializedName("id") val id: Int,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("hora") val hora: String,
    @SerializedName("ubicacion") val ubicacion: String,
    @SerializedName("categoria") val categoria: String,
    @SerializedName("imagen") val imagen: String? = null,
    @SerializedName("es_online") val esOnline: Boolean = false
)