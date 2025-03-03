package com.example.app.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/**
 * Formatea una fecha en formato ISO (yyyy-MM-dd) a un formato más legible
 * @param dateString La fecha en formato ISO
 * @param showYear Si se debe mostrar el año en el formato
 * @return La fecha formateada
 */
fun formatDate(dateString: String, showYear: Boolean = true): String {
    return try {
        val date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val pattern = if (showYear) "dd 'de' MMMM 'de' yyyy" else "dd 'de' MMMM"
        date.format(DateTimeFormatter.ofPattern(pattern, Locale("es", "ES")))
    } catch (e: DateTimeParseException) {
        dateString
    }
} 