package com.example.app.model.resetpassword

import com.google.gson.annotations.SerializedName

data class ResetPasswordRequest(
    @SerializedName("email") val email: String,
    @SerializedName("identificador") val identificador: String
)