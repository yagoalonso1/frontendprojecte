package com.example.app.model.logout

import com.google.gson.annotations.SerializedName

data class LogoutResponse(
    @SerializedName("message") val message: String
) 