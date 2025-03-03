package com.example.app.api

import com.example.app.model.evento.detail.EventoDetailResponse
import com.example.app.model.login.LoginRequest
import com.example.app.model.login.LoginResponse
import com.example.app.model.logout.LogoutResponse
import com.example.app.model.register.RegisterRequest
import com.example.app.model.register.RegisterResponse
import com.example.app.model.resetpassword.ResetPasswordRequest
import com.example.app.model.resetpassword.ResetPasswordResponse
import com.example.app.model.evento.EventoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("api/register")
    suspend fun registerUser(@Body registerRequest: RegisterRequest): Response<RegisterResponse>
    
    @POST("api/login")
    suspend fun loginUser(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
    @POST("logout")
    suspend fun logoutUser(@Header("Authorization") token: String): Response<LogoutResponse>
    
    @POST("api/reset-password")
    suspend fun resetPassword(@Body resetRequest: ResetPasswordRequest): Response<ResetPasswordResponse>
    
    @GET("api/eventos")
    suspend fun getAllEventos(): Response<EventoResponse>
    
    @GET("api/eventos/{id}")
    suspend fun getEventoById(@Path("id") id: Int): Response<EventoDetailResponse>
}