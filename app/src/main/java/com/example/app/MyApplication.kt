package com.example.app

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.app.utils.TokenManager
import com.example.app.util.SessionManager
import com.example.app.util.LocaleHelper

class MyApplication : Application() {
    companion object {
        lateinit var appContext: Context
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        
        try {
            Log.d("MyApplication", "Inicializando SessionManager...")
            SessionManager.init(this)
            Log.d("MyApplication", "SessionManager inicializado correctamente")
            
            Log.d("MyApplication", "Inicializando TokenManager...")
            TokenManager.init(this)
            Log.d("MyApplication", "TokenManager inicializado correctamente")
            
            // Inicializar el idioma de la aplicación
            Log.d("MyApplication", "Inicializando idioma de la aplicación...")
            // Verificar el idioma guardado
            val savedLanguage = SessionManager.getUserLanguage()
            Log.d("MyApplication", "Idioma guardado en SessionManager: $savedLanguage")
            
            val updatedContext = LocaleHelper.initLocale(this)
            val currentLanguage = LocaleHelper.getLanguage(this)
            Log.d("MyApplication", "Idioma inicializado correctamente: $currentLanguage")
        } catch (e: Exception) {
            Log.e("MyApplication", "Error al inicializar managers: ${e.message}")
            e.printStackTrace()
        }
    }
}