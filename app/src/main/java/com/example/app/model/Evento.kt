package com.example.app.model

import com.example.app.data.model.Organizador
import com.google.gson.annotations.SerializedName

data class Evento(
    @SerializedName("id") val id: Int,
    @SerializedName("nombreEvento") val titulo: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("fechaEvento") val fechaEvento: String,
    @SerializedName("hora") val hora: String,
    @SerializedName("ubicacion") val ubicacion: String,
    @SerializedName("precio") val precio: Double = 0.0,
    @SerializedName("categoria") val categoria: String,
    @SerializedName("imagen") val imagen: String?,
    @SerializedName("lugar") val lugar: String,
    @SerializedName("organizador") val organizador: Organizador,
)