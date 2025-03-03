package com.example.app.model.evento.detail

import com.example.app.model.Evento
import com.google.gson.annotations.SerializedName

data class EventoDetailResponse(
    @SerializedName("message") val message: String,
    @SerializedName("evento") val evento: Evento
) 