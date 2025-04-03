package com.example.app.model

import com.google.gson.annotations.SerializedName

data class CategoriasResponse(
    @SerializedName("message") val message: String,
    @SerializedName("categorias") val categorias: List<String>
)