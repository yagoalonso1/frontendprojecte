package com.example.app.model.favoritos

import com.google.gson.annotations.SerializedName

data class FavoritoRequest(
    @SerializedName("idEvento") val idEvento: Int
)