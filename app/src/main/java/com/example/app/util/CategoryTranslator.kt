package com.example.app.util

import android.util.Log
import com.example.app.MyApplication
import com.example.app.R

/**
 * Utilidad para traducir categorías de eventos según el idioma seleccionado por el usuario
 */
object CategoryTranslator {
    
    /**
     * Traduce una categoría de evento al idioma seleccionado por el usuario
     * 
     * @param categoria Categoría a traducir (en español, el idioma base)
     * @return Categoría traducida al idioma del usuario
     */
    fun translate(categoria: String): String {
        // Obtener el idioma actual del usuario
        val userLanguage = SessionManager.getUserLanguage() ?: "es"
        
        // Optimización: si el idioma es español y no queremos realizar la traducción,
        // podemos devolver la categoría original directamente
        // Sin embargo, para evitar problemas, es mejor siempre buscar el recurso
        // por si hay inconsistencias entre los strings originales y los definidos
        
        // Obtener el contexto de la aplicación
        val context = MyApplication.appContext
        
        // Mapear la categoría a su identificador de recurso
        val resourceId = when (categoria.lowercase()) {
            "concierto" -> R.string.categoria_conciertos
            "festival" -> R.string.categoria_festivales
            "teatro" -> R.string.categoria_teatro
            "deportes" -> R.string.categoria_deportes
            "conferencia" -> R.string.categoria_charlas_conferencias
            "exposición", "exposicion" -> R.string.categoria_exposiciones
            "taller" -> R.string.categoria_talleres
            "otro" -> R.string.categoria_varios
            // Mantener la compatibilidad con categorías de la antigua versión si es necesario
            "arte y cultura" -> R.string.categoria_arte_cultura
            "belleza y bienestar" -> R.string.categoria_belleza_bienestar
            "charlas y conferencias" -> R.string.categoria_charlas_conferencias
            "cine" -> R.string.categoria_cine
            "comedia" -> R.string.categoria_comedia
            "conciertos" -> R.string.categoria_conciertos
            "discoteca" -> R.string.categoria_discoteca
            "educación", "educacion" -> R.string.categoria_educacion
            "empresarial" -> R.string.categoria_empresarial
            "exposiciones" -> R.string.categoria_exposiciones
            "familiar" -> R.string.categoria_familiar
            "festivales" -> R.string.categoria_festivales
            "gastronomía", "gastronomia" -> R.string.categoria_gastronomia
            "infantil" -> R.string.categoria_infantil
            "moda" -> R.string.categoria_moda
            "música en directo", "musica en directo" -> R.string.categoria_musica_directo
            "networking" -> R.string.categoria_networking
            "ocio nocturno" -> R.string.categoria_ocio_nocturno
            "presentaciones" -> R.string.categoria_presentaciones
            "restaurantes" -> R.string.categoria_restaurantes
            "salud y deporte" -> R.string.categoria_salud_deporte
            "seminarios" -> R.string.categoria_seminarios
            "talleres" -> R.string.categoria_talleres
            "tecnología", "tecnologia" -> R.string.categoria_tecnologia
            "turismo" -> R.string.categoria_turismo
            "varios" -> R.string.categoria_varios
            else -> {
                // Si no se encuentra la categoría, registrar un error y devolver la original
                Log.d("CategoryTranslator", "No se encontró traducción para: $categoria en idioma: $userLanguage")
                return categoria
            }
        }
        
        // Obtener la traducción de los recursos
        return try {
            val traduccion = context.getString(resourceId)
            Log.d("CategoryTranslator", "Categoría '$categoria' traducida a '$traduccion' (idioma: $userLanguage)")
            traduccion
        } catch (e: Exception) {
            Log.e("CategoryTranslator", "Error al obtener traducción para: $categoria en idioma: $userLanguage", e)
            categoria // Devolver la categoría original en caso de error
        }
    }
} 