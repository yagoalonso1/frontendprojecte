package com.example.app.model.register

import com.google.gson.annotations.SerializedName
import com.example.app.model.User

data class RegisterResponse(
    @SerializedName("message") val message: String,
    @SerializedName("user") val user: User
)