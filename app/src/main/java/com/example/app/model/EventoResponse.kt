package com.example.app.model

import com.google.gson.annotations.SerializedName

// Respuesta para la lista de eventos por categoría
data class EventosResponse(
    @SerializedName("eventos") val eventos: List<Evento>
) 