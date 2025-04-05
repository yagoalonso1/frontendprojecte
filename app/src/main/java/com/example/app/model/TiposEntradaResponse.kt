package com.example.app.model

import com.google.gson.annotations.SerializedName

data class TiposEntradaResponse(
    @SerializedName("message") val message: String,
    @SerializedName("data") val tiposEntrada: List<TipoEntradaDetalle>,
    @SerializedName("status") val status: String
)

data class TipoEntradaDetalle(
    @SerializedName("idTipoEntrada") val id: Int,
    @SerializedName("idEvento") val idEvento: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("precio") val precio: String,
    @SerializedName("cantidad_disponible") val cantidadDisponible: Int?,
    @SerializedName("entradas_vendidas") val entradasVendidas: Int,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("es_ilimitado") val esIlimitado: Boolean,
    @SerializedName("activo") val activo: Boolean,
    @SerializedName("disponibilidad") val disponibilidad: Int?
) 