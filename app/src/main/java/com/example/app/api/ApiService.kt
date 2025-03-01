package com.example.app.api

import com.example.app.model.login.LoginRequest
import com.example.app.model.logout.LogoutResponse
import com.example.app.model.register.RegisterRequest
import com.example.app.model.register.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("api/register")
    suspend fun registerUser(@Body registerRequest: RegisterRequest): Response<RegisterResponse>
    
    @POST("api/login")
    suspend fun loginUser(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
    @POST("logout")
    suspend fun logoutUser(@Header("Authorization") token: String): Response<LogoutResponse>
} 