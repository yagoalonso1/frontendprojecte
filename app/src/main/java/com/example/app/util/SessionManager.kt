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
    
    // Cache de token para reducir accesos a disco
    private var cachedToken: String? = null
    private var cachedRole: String? = null
    
    fun init(context: Context) {
        try {
            prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            initialized = true
            // Inicializar cache
            cachedToken = prefs?.getString(KEY_TOKEN, null)
            cachedRole = prefs?.getString(KEY_USER_ROLE, null)
            Log.d("SessionManager", "Inicializado con éxito, token en cache: ${cachedToken?.take(10)}")
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al inicializar: ${e.message}")
            initialized = false
        }
    }
    
    fun saveToken(token: String) {
        Log.d("SessionManager", "Guardando token: ${token.take(10)}")
        try {
            if (!initialized || prefs == null) {
                Log.e("SessionManager", "No inicializado, no se puede guardar el token")
                return
            }
            // Actualizar caché inmediatamente
            cachedToken = token
            // Guardar en disco
            prefs?.edit()?.putString(KEY_TOKEN, token)?.apply()
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al guardar token: ${e.message}")
        }
    }
    
    fun getToken(): String? {
        // Si tenemos el token en caché, lo devolvemos directamente sin acceder a disco
        if (cachedToken != null) {
            return cachedToken
        }
        
        try {
            if (!initialized || prefs == null) {
                Log.e("SessionManager", "No inicializado, no se puede recuperar el token")
                return null
            }
            val token = prefs?.getString(KEY_TOKEN, null)
            Log.d("SessionManager", "Recuperando token de disco: ${token?.take(10)}")
            // Actualizar caché
            cachedToken = token
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
            // Actualizar caché
            cachedRole = role
            // Guardar en disco
            prefs?.edit()?.putString(KEY_USER_ROLE, role)?.apply()
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al guardar rol: ${e.message}")
        }
    }
    
    fun getUserRole(): String? {
        // Si tenemos el rol en caché, lo devolvemos directamente
        if (cachedRole != null) {
            return cachedRole
        }
        
        try {
            if (!initialized || prefs == null) {
                Log.e("SessionManager", "No inicializado, no se puede recuperar el rol")
                return null
            }
            val role = prefs?.getString(KEY_USER_ROLE, null)
            Log.d("SessionManager", "Recuperando rol de disco: $role")
            // Actualizar caché
            cachedRole = role
            return role
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al recuperar rol: ${e.message}")
            return null
        }
    }
    
    fun clearSession() {
        Log.d("SessionManager", "Limpiando sesión")
        try {
            // Limpiar caché inmediatamente
            cachedToken = null
            cachedRole = null
            
            if (!initialized || prefs == null) {
                Log.e("SessionManager", "No inicializado, no se puede limpiar la sesión")
                return
            }
            
            // Limpiar disco usando commit para que sea inmediato
            prefs?.edit()?.clear()?.commit()
            Log.d("SessionManager", "Sesión limpiada correctamente (caché y disco)")
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al limpiar sesión: ${e.message}")
        }
    }
    
    /**
     * Limpia la sesión y espera a que se complete (bloquea el hilo)
     * Usar solo cuando sea absolutamente necesario garantizar que se ha limpiado
     * antes de continuar con otra operación.
     */
    fun clearSessionSync(): Boolean {
        Log.d("SessionManager", "Limpiando sesión sincrónicamente")
        try {
            // Limpiar caché inmediatamente
            cachedToken = null
            cachedRole = null
            
            if (!initialized || prefs == null) {
                Log.e("SessionManager", "No inicializado, no se puede limpiar la sesión")
                return false
            }
            
            // Limpiar disco usando commit (síncrono)
            val result = prefs?.edit()?.clear()?.commit() ?: false
            Log.d("SessionManager", "Sesión limpiada correctamente (síncrono): $result")
            return result
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al limpiar sesión síncrona: ${e.message}")
            return false
        }
    }
    
    fun isLoggedIn(): Boolean {
        // Verificación rápida con caché
        if (cachedToken != null) {
            return true
        }
        
        try {
            if (!initialized || prefs == null) {
                Log.e("SessionManager", "No inicializado, no se puede verificar si está logueado")
                return false
            }
            
            // Solo si no hay caché, accedemos a disco
            val token = prefs?.getString(KEY_TOKEN, null)
            // Actualizar caché
            cachedToken = token
            
            val isLogged = token != null
            Log.d("SessionManager", "Verificando si está logueado: $isLogged")
            return isLogged
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al verificar si está logueado: ${e.message}")
            return false
        }
    }
    
    fun hasValidToken(): Boolean {
        return cachedToken != null || (initialized && prefs?.getString(KEY_TOKEN, null) != null)
    }
    
    fun isInitialized(): Boolean {
        return initialized && prefs != null
    }
    
    fun getUserLanguage(): String {
        // Por defecto, devolvemos español
        return "es"
    }
}