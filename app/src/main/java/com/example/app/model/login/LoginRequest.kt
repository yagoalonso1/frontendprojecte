package com.example.app.model.login

import com.google.gson.annotations.SerializedName
import com.example.app.model.User

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

