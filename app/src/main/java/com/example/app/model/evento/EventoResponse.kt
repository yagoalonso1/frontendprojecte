package com.example.app.model.evento

import com.example.app.model.Evento
import com.google.gson.annotations.SerializedName

data class EventoResponse(
    @SerializedName("message") val message: String,
    @SerializedName("eventos") val eventos: List<Evento>
) 