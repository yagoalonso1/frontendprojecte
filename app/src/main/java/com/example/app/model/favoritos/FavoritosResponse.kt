package com.example.app.model.favoritos

import com.example.app.model.Evento
import com.google.gson.annotations.SerializedName

data class FavoritosResponse(
    @SerializedName("message") val message: String,
    @SerializedName("eventos") val eventos: List<Evento>?,
    @SerializedName("favoritos") val favoritos: List<FavoritoItem>?,
    @SerializedName("total") val total: Int?,
    @SerializedName("status") val status: String?
)

data class FavoritoItem(
    @SerializedName("id") val id: Int?,
    @SerializedName("idParticipante") val idParticipante: Int?,
    @SerializedName("idEvento") val idEvento: Int?,
    @SerializedName("fechaAgregado") val fechaAgregado: String?,
    @SerializedName("evento") val evento: Evento?
) 