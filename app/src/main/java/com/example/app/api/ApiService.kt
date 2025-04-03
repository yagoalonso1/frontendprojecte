package com.example.app.api

import com.example.app.model.Evento
import com.example.app.model.evento.EventoRequest
import com.example.app.model.evento.detail.EventoDetailResponse
import com.example.app.model.login.LoginRequest
import com.example.app.model.login.LoginResponse
import com.example.app.model.logout.LogoutResponse
import com.example.app.model.register.RegisterRequest
import com.example.app.model.register.RegisterResponse
import com.example.app.model.resetpassword.ResetPasswordRequest
import com.example.app.model.resetpassword.ResetPasswordResponse
import com.example.app.model.evento.EventoResponse
import com.example.app.model.favoritos.FavoritoRequest
import com.example.app.model.favoritos.FavoritosResponse
import com.example.app.model.favoritos.FavoritoCheckResponse
import com.example.app.model.CategoriasResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName
import com.example.app.model.evento.CrearEventoResponse
import com.example.app.model.ProfileResponse
import retrofit2.http.PUT

interface ApiService {
    @POST("api/register")
    suspend fun registerUser(@Body registerRequest: RegisterRequest): Response<RegisterResponse>
    
    @POST("api/login")
    suspend fun loginUser(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
    @POST("logout")
    suspend fun logoutUser(
        @Header("Authorization") token: String
    ): Response<LogoutResponse>
    
    @POST("api/reset-password")
    suspend fun resetPassword(@Body resetRequest: ResetPasswordRequest): Response<ResetPasswordResponse>
    
    @GET("api/eventos")
    suspend fun getAllEventos(): Response<EventoResponse>
    
    @GET("api/eventos/{id}")
    suspend fun getEventoById(@Path("id") id: String): Response<EventoDetailResponse>
    
    @GET("api/favoritos")
    suspend fun getFavoritos(
        @Header("Authorization") token: String
    ): Response<FavoritosResponse>
    
    @POST("api/favoritos")
    suspend fun addFavorito(
        @Header("Authorization") token: String,
        @Body request: FavoritoRequest
    ): Response<MessageResponse>
    
    @DELETE("api/favoritos/{idEvento}")
    suspend fun removeFavorito(
        @Header("Authorization") token: String,
        @Path("idEvento") idEvento: Int
    ): Response<MessageResponse>
    
    @GET("api/favoritos/check/{idEvento}")
    suspend fun checkFavorito(
        @Header("Authorization") token: String,
        @Path("idEvento") idEvento: Int
    ): Response<FavoritoCheckResponse>
    
    @POST("eventos/{eventoId}/favorito")
    suspend fun toggleFavorito(@Path("eventoId") eventoId: Int): Response<Unit>
    
    @GET("api/mis-eventos")
    suspend fun getMisEventos(
        @Header("Authorization") token: String
    ): Response<EventoResponse>

    @POST("api/eventos")
    suspend fun crearEvento(
        @Header("Authorization") token: String,
        @Body eventoRequest: EventoRequest
    ): Response<CrearEventoResponse>

    @GET("api/categorias")
    suspend fun getCategorias(
        @Header("Authorization") token: String
    ): Response<CategoriasResponse>

    @GET("api/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<ProfileResponse>
    
    @PUT("api/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body profileData: Map<String, String>
    ): Response<ProfileResponse>
    
    @GET("api/user")
    suspend fun getUser(
        @Header("Authorization") token: String
    ): Response<com.example.app.model.User>
}

data class FavoritosResponse(
    @SerializedName("message") val message: String,
    @SerializedName("favoritos") val eventos: List<Evento>
)

data class MessageResponse(
    @SerializedName("message") val message: String
)

data class FavoritoCheckResponse(
    @SerializedName("isFavorito") val isFavorito: Boolean
)