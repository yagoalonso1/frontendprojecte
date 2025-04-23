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
import com.example.app.model.TiposEntradaResponse
import com.example.app.model.CompraRequest
import com.example.app.model.CompraResponse
import com.example.app.model.response.GenericResponse
import com.example.app.model.response.MisEventosResponse
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
import com.example.app.model.tickets.TicketsResponse
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Field
import retrofit2.http.Query

interface ApiService {
    @POST("api/register")
    suspend fun registerUser(@Body registerRequest: RegisterRequest): Response<RegisterResponse>
    
    @POST("api/login")
    suspend fun loginUser(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
    @POST("api/logout")
    suspend fun logoutUser(
        @Header("Authorization") token: String
    ): Response<LogoutResponse>
    
    @POST("api/reset-password")
    suspend fun resetPassword(@Body resetRequest: ResetPasswordRequest): Response<ResetPasswordResponse>
    
    @GET("api/eventos")
    suspend fun getAllEventos(): Response<EventoResponse>
    
    @GET("api/eventos/{id}")
    suspend fun getEventoById(@Path("id") id: String): Response<EventoDetailResponse>
    
    @GET("api/eventos/{id}/tipos-entrada")
    suspend fun getTiposEntrada(@Path("id") id: String): Response<TiposEntradaResponse>
    
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
    suspend fun getMisEventosFromApi(
        @Header("Authorization") token: String
    ): Response<EventoResponse>
    
    @POST("api/eventos")
    suspend fun crearEvento(
        @Header("Authorization") token: String,
        @Body request: EventoRequest
    ): Response<CrearEventoResponse>

    @Multipart
    @POST("api/eventos")
    suspend fun crearEventoConImagen(
        @Header("Authorization") token: String,
        @Part("titulo") titulo: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("fecha") fecha: RequestBody,
        @Part("hora") hora: RequestBody,
        @Part("ubicacion") ubicacion: RequestBody,
        @Part("categoria") categoria: RequestBody,
        @Part("es_online") esOnline: RequestBody,
        @Part tiposEntradas: List<MultipartBody.Part>,
        @Part imagen: MultipartBody.Part
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
    
    @GET("api/compras")
    suspend fun getMisTickets(
        @Header("Authorization") token: String
    ): Response<TicketsResponse>
    
    @POST("api/compras")
    suspend fun comprarEntradas(
        @Header("Authorization") token: String,
        @Body compraRequest: CompraRequest
    ): Response<CompraResponse>

    @PUT("api/eventos/{id}")
    suspend fun actualizarEvento(
        @Path("id") id: String,
        @Header("Authorization") token: String,
        @Body request: EventoRequest
    ): Response<CrearEventoResponse>

    @Multipart
    @POST("api/eventos/{id}")
    suspend fun actualizarEventoConImagen(
        @Path("id") id: String,
        @Header("Authorization") token: String,
        @Part("titulo") titulo: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("fecha") fecha: RequestBody,
        @Part("hora") hora: RequestBody,
        @Part("ubicacion") ubicacion: RequestBody,
        @Part("categoria") categoria: RequestBody,
        @Part("es_online") esOnline: RequestBody,
        @Part tiposEntradas: List<MultipartBody.Part>,
        @Part imagen: MultipartBody.Part,
        @Part("_method") method: RequestBody = "PUT".toRequestBody("text/plain".toMediaTypeOrNull())
    ): Response<CrearEventoResponse>

    @GET("api/debug/testid/{id}")
    suspend fun testId(@Path("id") id: String): Response<String>
    
    // Función extra para retornar el ID recibido, útil para depuración
    @GET("api/debug/echo/{id}")
    suspend fun echoId(@Path("id") id: String): Response<String>

    // Obtener mis eventos (del organizador logueado)
    @GET("api/mis-eventos")
    suspend fun getMisEventos(
        @Header("Authorization") token: String
    ): Response<MisEventosResponse>
    
    // Eliminar un evento
    @DELETE("api/eventos/{id}")
    suspend fun deleteEvento(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Response<GenericResponse>
    
    // Eliminar cuenta de usuario (usando POST con un parámetro _method=DELETE)
    @POST("api/account")
    suspend fun deleteAccount(
        @Header("Authorization") token: String,
        @Body deleteRequest: DeleteAccountRequest,
        @Query("_method") method: String = "DELETE"
    ): Response<GenericResponse>
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

// Modelo para la solicitud de eliminación de cuenta
data class DeleteAccountRequest(
    @SerializedName("password") val password: String,
    @SerializedName("confirm_deletion") val confirmDeletion: Boolean
)