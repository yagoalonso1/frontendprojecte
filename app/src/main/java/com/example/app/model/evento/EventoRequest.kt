package com.example.app.model.evento

import com.google.gson.annotations.SerializedName

data class EventoRequest(
    @SerializedName("nombreEvento") val nombreEvento: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("fechaEvento") val fechaEvento: String,
    @SerializedName("hora") val hora: String,
    @SerializedName("ubicacion") val ubicacion: String,
    @SerializedName("lugar") val lugar: String,
    @SerializedName("categoria") val categoria: String,
    @SerializedName("imagen") val imagen: String?
)