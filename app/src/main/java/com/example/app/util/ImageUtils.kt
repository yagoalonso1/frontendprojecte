package com.example.app.util

import com.example.app.model.Evento
import com.example.app.model.tickets.EventoCompra

/**
 * Obtiene la URL completa de la imagen para un Evento.
 * Prioriza usar imagen_url si está disponible, de lo contrario construye la URL
 * basada en el campo imagen y las constantes de la aplicación.
 *
 * @return URL completa de la imagen
 */
fun Evento.getImageUrl(): String {
    return imagenUrl ?: when {
        imagen.startsWith("http") -> imagen
        imagen.isNotEmpty() -> "${Constants.STORAGE_URL}${imagen}"
        else -> "${Constants.STORAGE_URL}${Constants.DEFAULT_EVENT_IMAGE}"
    }
}

/**
 * Obtiene la URL completa de la imagen para un EventoCompra.
 * Esta versión es específica para la estructura EventoCompra usada en los tickets.
 *
 * @return URL completa de la imagen
 */
fun EventoCompra.getImageUrl(): String {
    return imagen?.let { img ->
        when {
            img.startsWith("http") -> img
            img.isNotEmpty() -> "${Constants.STORAGE_URL}${img}"
            else -> "${Constants.STORAGE_URL}${Constants.DEFAULT_EVENT_IMAGE}"
        }
    } ?: "${Constants.STORAGE_URL}${Constants.DEFAULT_EVENT_IMAGE}"
} 