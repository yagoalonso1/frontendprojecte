package com.example.app.util

import android.util.Log

/**
 * Función de extensión para validar si un String es un ID válido de evento
 */
fun String?.isValidEventoId(): Boolean {
    // Log detallado para diagnóstico
    Log.d("StringExt", "Validando ID: '$this' (tipo: ${this?.javaClass?.name ?: "null"})")
    
    if (this == null || this.isBlank() || this == "-1" || this == "null") {
        Log.d("StringExt", "ID inválido (nulo/vacío/reservado): '$this'")
        return false
    }
    
    val cleanId = this.trim()
    Log.d("StringExt", "ID limpio para validación: '$cleanId'")
    
    val idNum = cleanId.toIntOrNull()
    if (idNum == null) {
        Log.d("StringExt", "ID inválido (no es número): '$cleanId'")
        return false
    }
    
    if (idNum <= 0) {
        Log.d("StringExt", "ID inválido (no es positivo): $idNum")
        return false
    }
    
    Log.d("StringExt", "ID válido: $idNum")
    return true
}

/**
 * Función de extensión para convertir un String a Int con validación segura
 * Retorna null si no es un número válido o es <= 0
 */
fun String?.toValidEventoId(): Int? {
    if (this == null || this.isBlank() || this == "-1" || this == "null") {
        return null
    }
    
    val idNum = this.toIntOrNull()
    if (idNum == null || idNum <= 0) {
        return null
    }
    
    return idNum
}

/**
 * Función de extensión para obtener un mensaje de error según el tipo de invalidez
 */
fun String?.getEventoIdErrorMessage(): String {
    return when {
        this == null -> "ID de evento es nulo"
        this.isBlank() -> "ID de evento está vacío"
        this == "-1" || this == "null" -> "ID de evento reservado inválido: '$this'"
        this.toIntOrNull() == null -> "ID de evento no es un número: '$this'"
        this.toIntOrNull() ?: 0 <= 0 -> "ID de evento debe ser mayor que cero: '$this'"
        else -> "ID de evento inválido: '$this'"
    }
} 