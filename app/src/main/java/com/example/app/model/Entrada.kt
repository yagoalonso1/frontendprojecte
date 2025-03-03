package com.example.app.model

import com.google.gson.annotations.SerializedName

data class Entrada(
    @SerializedName("id") val id: Int,
    @SerializedName("tipo") val tipo: String,
    @SerializedName("precio") val precio: Double,
    @SerializedName("cantidad_disponible") val cantidadDisponible: Int
)