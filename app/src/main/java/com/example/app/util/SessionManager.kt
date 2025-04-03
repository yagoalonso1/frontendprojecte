package com.example.app.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object SessionManager {
    private const val PREF_NAME = "EventFlixPrefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER_ROLE = "user_role"
    private var prefs: SharedPreferences? = null
    private var initialized = false
    
    fun init(context: Context) {
        try {
            prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            initialized = true
            Log.d("SessionManager", "Inicializado con éxito")
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al inicializar: ${e.message}")
            initialized = false
        }
    }
    
    fun saveToken(token: String) {
        Log.d("SessionManager", "Guardando token: $token")
        try {
            if (!initialized || prefs == null) {
                Log.e("SessionManager", "No inicializado, no se puede guardar el token")
                return
            }
            prefs?.edit()?.putString(KEY_TOKEN, token)?.apply()
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al guardar token: ${e.message}")
        }
    }
    
    fun getToken(): String? {
        try {
            if (!initialized || prefs == null) {
                Log.e("SessionManager", "No inicializado, no se puede recuperar el token")
                return null
            }
            val token = prefs?.getString(KEY_TOKEN, null)
            Log.d("SessionManager", "Recuperando token: $token")
            return token
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al recuperar token: ${e.message}")
            return null
        }
    }
    
    fun saveUserRole(role: String) {
        Log.d("SessionManager", "Guardando rol: $role")
        try {
            if (!initialized || prefs == null) {
                Log.e("SessionManager", "No inicializado, no se puede guardar el rol")
                return
            }
            prefs?.edit()?.putString(KEY_USER_ROLE, role)?.apply()
            // Verificar inmediatamente que se guardó
            val savedRole = getUserRole()
            Log.d("SessionManager", "Rol guardado verificado: $savedRole")
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al guardar rol: ${e.message}")
        }
    }
    
    fun getUserRole(): String? {
        try {
            if (!initialized || prefs == null) {
                Log.e("SessionManager", "No inicializado, no se puede recuperar el rol")
                return null
            }
            val role = prefs?.getString(KEY_USER_ROLE, null)
            Log.d("SessionManager", "Recuperando rol: $role")
            return role
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al recuperar rol: ${e.message}")
            return null
        }
    }
    
    fun clearSession() {
        Log.d("SessionManager", "Limpiando sesión")
        try {
            if (!initialized || prefs == null) {
                Log.e("SessionManager", "No inicializado, no se puede limpiar la sesión")
                return
            }
            prefs?.edit()?.clear()?.apply()
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al limpiar sesión: ${e.message}")
        }
    }
    
    fun isLoggedIn(): Boolean {
        try {
            if (!initialized || prefs == null) {
                Log.e("SessionManager", "No inicializado, no se puede verificar si está logueado")
                return false
            }
            val isLogged = getToken() != null
            Log.d("SessionManager", "Verificando si está logueado: $isLogged")
            return isLogged
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al verificar si está logueado: ${e.message}")
            return false
        }
    }
    
    fun isInitialized(): Boolean {
        return initialized && prefs != null
    }
}