package com.example.app.model.favoritos

import com.google.gson.annotations.SerializedName

data class OrganizadorFavoritoRequest(
    @SerializedName("idOrganizador") val idOrganizador: Int
) 