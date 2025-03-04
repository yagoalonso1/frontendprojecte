package com.example.app.model.favoritos

import com.google.gson.annotations.SerializedName

data class FavoritoCheckResponse(
    @SerializedName("isFavorito") val isFavorito: Boolean
) 