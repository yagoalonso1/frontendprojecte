package com.example.app.model

import com.google.gson.annotations.SerializedName

data class CompraRequest(
    @SerializedName("idEvento") val idEvento: Int,
    @SerializedName("entradas") val entradas: List<EntradaCompra>,
    @SerializedName("emitir_factura") val emitirFactura: Boolean = true,
    @SerializedName("metodo_pago") val metodoPago: String = "tarjeta"
)

data class EntradaCompra(
    @SerializedName("idTipoEntrada") val idTipoEntrada: Int,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("precio") val precio: Double = 0.0
)

data class CompraResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("id_compra") val idCompra: Int?
)  