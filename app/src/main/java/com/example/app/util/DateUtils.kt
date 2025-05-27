package com.example.app.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import android.util.Log
import com.example.app.util.SessionManager
import com.example.app.MyApplication
import com.example.app.R

/**
 * Formatea una fecha en formato ISO (yyyy-MM-dd) a un formato más legible
 * @param dateString La fecha en formato ISO
 * @param showYear Si se debe mostrar el año en el formato
 * @return La fecha formateada
 */
fun formatDate(dateString: String, showYear: Boolean = true): String {
    return try {
        val date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val userLanguage = SessionManager.getUserLanguage() ?: "es"
        
        Log.d("DateUtils", "Formateando fecha con idioma: $userLanguage")
        
        // Obtener el contexto de la aplicación
        val context = MyApplication.appContext
        val dateOf = context.getString(R.string.date_of)
        
        // Definir patrones según el idioma
        val pattern = when (userLanguage) {
            "en" -> if (showYear) "MMMM d, yyyy" else "MMMM d"
            "ca", "es" -> {
                // Usar el texto 'de' desde recursos
                if (showYear) "d 'de' MMMM 'de' yyyy" else "d 'de' MMMM"
            }
            else -> {
                // Por defecto, usar el mismo formato que en español
                if (showYear) "d 'de' MMMM 'de' yyyy" else "d 'de' MMMM"
            }
        }
        
        // Definir locale según el idioma
        val locale = when (userLanguage) {
            "en" -> Locale.ENGLISH
            "ca" -> Locale("ca", "ES")
            "es" -> Locale("es", "ES")
            else -> Locale("es", "ES") // Español por defecto
        }
        
        date.format(DateTimeFormatter.ofPattern(pattern, locale))
    } catch (e: Exception) {
        Log.e("DateUtils", "Error al formatear fecha: $dateString", e)
        dateString
    }
} 