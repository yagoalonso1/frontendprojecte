package com.example.app.model.favoritos

import com.example.app.model.Organizador
import com.google.gson.annotations.SerializedName

data class OrganizadoresFavoritosResponse(
    @SerializedName("message") val message: String,
    @SerializedName("favoritos") val organizadores: List<Organizador>,
    @SerializedName("status") val status: String
) 