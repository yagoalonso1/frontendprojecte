package com.example.app.model.evento

import com.google.gson.annotations.SerializedName

data class TipoEntradaRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("precio") val precio: Double,
    @SerializedName("cantidad_disponible") val cantidadDisponible: Int? = null,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("es_ilimitado") val esIlimitado: Boolean = false
) 