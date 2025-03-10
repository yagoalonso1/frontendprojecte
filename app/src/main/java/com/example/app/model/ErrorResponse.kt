package com.example.app.model

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: String?,
    @SerializedName("code") val code: Int?
) 