package com.example.app.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import java.util.*
import android.app.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.util.Log

/**
 * Clase de utilidad para gestionar los cambios de idioma en la aplicación
 */
object LocaleHelper {
    private const val LANGUAGE_KEY = "selected_language"
    
    // StateFlow para notificar cambios de idioma - Hacer más reactivo
    private val _languageFlow = MutableStateFlow(System.currentTimeMillis())
    val languageFlow: StateFlow<Long> = _languageFlow.asStateFlow()
    
    /**
     * Establece el idioma de la aplicación (versión composable)
     * @param languageCode Código del idioma (es, ca, en)
     */
    @Composable
    fun setLocaleComposable(languageCode: String) {
        val context = LocalContext.current
        setLocale(context, languageCode)
    }
    
    /**
     * Obtiene el idioma actual (versión Composable)
     */
    @Composable
    fun getCurrentLanguage(): String {
        // Observar cambios para forzar recomposición
        languageFlow.collectAsState().value
        // Devolver el idioma actual
        return getLanguage(LocalContext.current)
    }
    
    /**
     * Establece el idioma de la aplicación
     * @param context Contexto de la aplicación
     * @param languageCode Código del idioma (es, ca, en)
     * @return Contexto actualizado con el nuevo idioma
     */
    fun setLocale(context: Context, languageCode: String): Context {
        // Guardar la preferencia de idioma
        saveLanguagePreference(context, languageCode)
        
        // Configurar el locale
        val locale = when (languageCode) {
            "ca" -> Locale("ca", "ES")
            "en" -> Locale.ENGLISH
            else -> Locale("es", "ES") // Español por defecto
        }
        
        // Establecer el locale a nivel de aplicación
        Locale.setDefault(locale)
        
        // Configurar los recursos
        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }
        
        // Actualizar configuración
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val updatedContext = context.createConfigurationContext(configuration)
            resources.updateConfiguration(configuration, resources.displayMetrics)
            
            // Actualizar el StateFlow para notificar a los componentes
            _languageFlow.value = System.currentTimeMillis()
            
            return updatedContext
        } else {
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
        
        // Actualizar el StateFlow para notificar a los componentes
        _languageFlow.value = System.currentTimeMillis()
        
        return context
    }
    
    /**
     * Obtiene el idioma actualmente seleccionado
     * @param context Contexto de la aplicación
     * @return Código del idioma seleccionado
     */
    fun getLanguage(context: Context): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(LANGUAGE_KEY, Locale.getDefault().language) ?: "es"
    }
    
    /**
     * Guarda la preferencia de idioma
     * @param context Contexto de la aplicación
     * @param languageCode Código del idioma
     */
    private fun saveLanguagePreference(context: Context, languageCode: String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().putString(LANGUAGE_KEY, languageCode).apply()
    }
    
    /**
     * Inicializa el idioma de la aplicación según la preferencia guardada
     * @param context Contexto de la aplicación
     * @return Contexto actualizado con el idioma guardado
     */
    fun initLocale(context: Context): Context {
        // Primero verificar si hay un idioma guardado en SessionManager
        val sessionLanguage = SessionManager.getUserLanguage()
        
        // Si hay un idioma guardado en SessionManager, usarlo
        if (sessionLanguage != null) {
            return setLocale(context, sessionLanguage)
        }
        
        // Si no, usar el idioma guardado en SharedPreferences
        val language = getLanguage(context)
        return setLocale(context, language)
    }
    
    /**
     * Aplica el cambio de idioma sin recrear la actividad
     */
    fun applyLanguageWithoutRecreate(activity: Activity, languageCode: String) {
        // Guardar idioma en SharedPreferences y SessionManager
        saveLanguagePreference(activity, languageCode)
        SessionManager.saveUserLanguage(languageCode)
        
        // Crear un nuevo contexto con el idioma actualizado
        val context = setLocale(activity, languageCode)
        
        // Actualizar el contexto de recursos de la actividad
        val resources = activity.resources
        val metrics = resources.displayMetrics
        val configuration = Configuration(resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(when (languageCode) {
                "ca" -> Locale("ca", "ES")
                "en" -> Locale.ENGLISH
                else -> Locale("es", "ES") // Español por defecto
            })
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = when (languageCode) {
                "ca" -> Locale("ca", "ES")
                "en" -> Locale.ENGLISH
                else -> Locale("es", "ES") // Español por defecto
            }
        }
        
        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, metrics)
        
        // Notificar el cambio
        _languageFlow.value = System.currentTimeMillis()
        
        // Log para depuración
        Log.d("LocaleHelper", "Idioma actualizado a: $languageCode sin recrear actividad")
    }
    
    /**
     * Aplica el cambio de idioma y fuerza la actualización de recursos
     */
    fun forceLocaleUpdate(activity: Activity, languageCode: String) {
        // Guardar idioma en SharedPreferences y SessionManager
        saveLanguagePreference(activity, languageCode)
        SessionManager.saveUserLanguage(languageCode)
        
        // Establecer el locale
        val locale = when (languageCode) {
            "ca" -> Locale("ca", "ES")
            "en" -> Locale.ENGLISH
            else -> Locale("es", "ES") // Español por defecto
        }
        
        // Establecer el locale a nivel de aplicación
        Locale.setDefault(locale)
        
        // Actualizar recursos de la actividad
        val resources = activity.resources
        val config = resources.configuration
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
            activity.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
        
        // Notificar el cambio
        _languageFlow.value = System.currentTimeMillis()
        
        // Forzar actualización completa recreando la actividad
        try {
            // Guardar estado de la actividad si es necesario
            val intent = activity.intent
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.finish()
            activity.startActivity(intent)
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        } catch (e: Exception) {
            Log.e("LocaleHelper", "Error recreando actividad: ${e.message}")
        }
        
        Log.d("LocaleHelper", "Idioma actualizado a: $languageCode y actividad recreada")
    }
} 