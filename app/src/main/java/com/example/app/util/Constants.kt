package com.example.app.util

/**
 * Constantes usadas en toda la aplicación
 */
object Constants {
    // URL base para la API
    const val BASE_URL = "http://10.0.2.2:8000/"
    
    // URL base para las imágenes
    const val STORAGE_URL = "http://10.0.2.2:8000/storage/"
    
    // Preferencias
    const val PREF_NAME = "app_preferences"
    const val PREF_TOKEN = "user_token"
    const val PREF_USER_ROLE = "user_role"
    
    // Valores por defecto
    const val DEFAULT_EVENT_IMAGE = "eventos/default.jpg"

    // Formatos de fecha y hora
    const val DATE_FORMAT = "yyyy-MM-dd"
    const val TIME_FORMAT = "HH:mm"
    const val DISPLAY_DATE_FORMAT = "d 'de' MMMM, yyyy"
    const val DISPLAY_DATE_WITH_DAY_FORMAT = "EEEE d 'de' MMMM, yyyy"
} 