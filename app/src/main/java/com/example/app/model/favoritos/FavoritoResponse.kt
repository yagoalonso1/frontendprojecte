package com.example.app.model.favoritos

import com.example.app.model.Evento
import com.google.gson.annotations.SerializedName

data class FavoritosResponse(
    @SerializedName("message") val message: String,
    @SerializedName("eventos") val eventos: List<Evento>?
) 