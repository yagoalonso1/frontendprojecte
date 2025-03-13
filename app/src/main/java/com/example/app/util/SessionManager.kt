package com.example.app.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object SessionManager {
    private const val PREF_NAME = "EventFlixPrefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER_ROLE = "user_role"
    private lateinit var prefs: SharedPreferences
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        Log.d("SessionManager", "Inicializado con éxito")
    }
    
    fun saveToken(token: String) {
        Log.d("SessionManager", "Guardando token: $token")
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }
    
    fun getToken(): String? {
        val token = prefs.getString(KEY_TOKEN, null)
        Log.d("SessionManager", "Recuperando token: $token")
        return token
    }
    
    fun saveUserRole(role: String) {
        Log.d("SessionManager", "Guardando rol: $role")
        prefs.edit().putString(KEY_USER_ROLE, role).apply()
        // Verificar inmediatamente que se guardó
        val savedRole = getUserRole()
        Log.d("SessionManager", "Rol guardado verificado: $savedRole")
    }
    
    fun getUserRole(): String? {
        val role = prefs.getString(KEY_USER_ROLE, null)
        Log.d("SessionManager", "Recuperando rol: $role")
        return role
    }
    
    fun clearSession() {
        Log.d("SessionManager", "Limpiando sesión")
        prefs.edit().clear().apply()
    }
    
    fun isLoggedIn(): Boolean {
        val isLogged = getToken() != null
        Log.d("SessionManager", "Verificando si está logueado: $isLogged")
        return isLogged
    }
}