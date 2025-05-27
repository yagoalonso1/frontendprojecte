package com.example.app.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Asegurarnos de que la URL base esté correctamente formada
    private const val BASE_URL = "http://10.0.2.2:8000/"  // Quitamos 'api/' al final porque los endpoints ya lo incluyen

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        // Añadir interceptor para aceptar sólo JSON
        .addInterceptor { chain ->
            val original = chain.request()
            
            // Crear una nueva petición con el header Accept:application/json
            val requestBuilder = original.newBuilder()
                .header("Accept", "application/json")
                .method(original.method, original.body)
                
            val request = requestBuilder.build()
            chain.proceed(request)
        }
        .addInterceptor { chain ->
            val request = chain.request()
            Log.d("Retrofit", "Enviando petición a: ${request.url}")
            Log.d("Retrofit", "Headers: ${request.headers}")
            Log.d("Retrofit", "Método: ${request.method}")
            
            try {
                val response = chain.proceed(request)
                Log.d("Retrofit", "Respuesta recibida de: ${request.url}")
                Log.d("Retrofit", "Código de respuesta: ${response.code}")
                Log.d("Retrofit", "Headers de respuesta: ${response.headers}")
                
                // Leer y loggear el cuerpo de la respuesta
                val responseBody = response.peekBody(Long.MAX_VALUE).string()
                Log.d("Retrofit", "Cuerpo de la respuesta: $responseBody")
                
                response
            } catch (e: Exception) {
                Log.e("Retrofit", "Error en la petición a: ${request.url}", e)
                throw e
            }
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // Configuración personalizada de Gson para manejar mejor los valores nulos
    private val gson: Gson = GsonBuilder()
        .setLenient() // Permite JSON malformado
        .serializeNulls() // Serializa los campos nulos
        .create()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
