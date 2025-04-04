package com.example.app.model.tickets

import com.google.gson.annotations.SerializedName

data class Ticket(
    @SerializedName("id") val id: Int,
    @SerializedName("precio") val precio: Double,
    @SerializedName("estado") val estado: String,
    @SerializedName("nombre_persona") val nombrePersona: String,
    @SerializedName("tipo_entrada") val tipoEntrada: String
)

data class EventoCompra(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("hora") val hora: String,
    @SerializedName("imagen") val imagen: String?
)

data class TicketCompra(
    @SerializedName("evento") val evento: EventoCompra,
    @SerializedName("entradas") val tickets: List<Ticket>,
    @SerializedName("total") val total: Double,
    @SerializedName("fecha_compra") val fechaCompra: String,
    @SerializedName("id_compra") val idCompra: Int
)

data class TicketsResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("compras") val compras: List<TicketCompra>,
    @SerializedName("status") val status: String?
) 