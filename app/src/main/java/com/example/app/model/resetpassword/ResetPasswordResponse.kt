package com.example.app.model.resetpassword

import com.google.gson.annotations.SerializedName

data class ResetPasswordResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("password") val password: String?
)