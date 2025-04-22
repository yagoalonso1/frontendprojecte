package com.example.app.model

import com.example.app.viewmodel.CompraItem
import com.google.gson.annotations.SerializedName

data class HistorialComprasResponse(
    @SerializedName("message") val message: String,
    @SerializedName("compras") val compras: List<CompraItem>? = null,
    @SerializedName("status") val status: String
) 