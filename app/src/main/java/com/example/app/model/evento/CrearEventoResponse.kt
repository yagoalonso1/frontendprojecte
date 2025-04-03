package com.example.app.model.evento

import com.example.app.model.Evento
import com.google.gson.annotations.SerializedName

data class CrearEventoResponse(
    @SerializedName("message") val message: String,
    @SerializedName("evento") val evento: Evento,
    @SerializedName("status") val status: String
)