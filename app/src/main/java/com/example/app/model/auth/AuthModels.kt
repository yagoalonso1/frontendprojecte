package com.example.app.model.auth

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val nombre: String,
    val apellido1: String,
    val apellido2: String?,
    val email: String,
    val password: String,
    val password_confirmation: String
)

data class ResetPasswordRequest(
    val email: String
)

data class AuthResponse(
    val token: String?,
    val user: UserData?,
    val message: String?,
    val error: String?
)

data class UserData(
    val id: Int,
    val nombre: String,
    val apellido1: String,
    val apellido2: String?,
    val email: String
) 