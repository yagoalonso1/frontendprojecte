package com.example.app.ui.components

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.example.app.R
import java.util.*

// Colores consistentes con la app
private val primaryColor = Color(0xFFE53935)  // Rojo principal de la app
private val textPrimaryColor = Color(0xFF333333) // Color de texto principal

// Colores para las banderas
private val españaRojo = Color(0xFFAA151B)
private val españaAmarillo = Color(0xFFF1BF00)
private val catalanAmarillo = Color(0xFFFFD700)
private val catalanRojo = Color(0xFFDA121A)
private val ukAzul = Color(0xFF012169)
private val ukRojo = Color(0xFFC8102E)
private val ukBlanco = Color(0xFFFFFFFF)
private val escudoColor = Color(0xFF8A2BE2) // Color para representar el escudo

/**
 * Botón de selección de idioma con un icono de globo terráqueo
 * @param onSelectLanguage Callback que se ejecuta al seleccionar un idioma
 * @param modifier Modificador para personalizar la apariencia
 */
@Composable
fun LanguageMenuButton(
    onSelectLanguage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    
    // Obtener el idioma actual
    val currentLanguage = remember { mutableStateOf(com.example.app.util.SessionManager.getUserLanguage() ?: "es") }
    
    // Usar RememberUpdatedState para evitar problemas con el callback
    val currentOnSelectLanguage by rememberUpdatedState(onSelectLanguage)
    
    // Lista de idiomas disponibles
    val languages = listOf(
        LanguageItem("es", stringResource(R.string.language_spanish), FlagType.SPAIN),
        LanguageItem("ca", stringResource(R.string.language_catalan), FlagType.CATALONIA),
        LanguageItem("en", stringResource(R.string.language_english), FlagType.UK)
    )
    
    // Buscar el idioma seleccionado actualmente
    val selectedLanguage = languages.find { it.code == currentLanguage.value } ?: languages[0]
    
    Box(modifier = modifier) {
        // Botón simple con icono de globo
        IconButton(
            onClick = { showMenu = true },
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = stringResource(R.string.language_selector_title),
                tint = primaryColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Menú desplegable de idiomas
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier
                .width(240.dp)
                .background(Color.White),
            properties = PopupProperties(focusable = true)
        ) {
            // Título del menú
            Text(
                text = stringResource(R.string.language_selector_title),
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = textPrimaryColor
            )
            
            HorizontalDivider(
                thickness = 1.dp,
                color = Color.LightGray
            )
            
            // Opciones de idioma
            languages.forEach { language ->
                LanguageMenuItem(
                    language = language,
                    onClick = {
                        // Usar la referencia actualizada del callback
                        currentOnSelectLanguage(language.code)
                        showMenu = false
                        // Actualizar el idioma seleccionado
                        currentLanguage.value = language.code
                        
                        // Aplicar cambio de idioma sin recrear la actividad 
                        try {
                            // Obtener la actividad actual
                            val activity = context as? android.app.Activity
                            if (activity != null) {
                                Log.d("LanguageMenuButton", "Cambiando idioma a: ${language.code}")
                                
                                // Aplicar cambio de idioma y forzar actualización
                                com.example.app.util.LocaleHelper.forceLocaleUpdate(
                                    activity,
                                    language.code
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("LanguageMenuButton", "Error al aplicar cambio de idioma: ${e.message}")
                        }
                    }
                )
            }
        }
    }
}

/**
 * Elemento de menú para un idioma
 */
@Composable
fun LanguageMenuItem(
    language: LanguageItem,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
                // Forzar el reinicio de la actividad para aplicar los cambios de idioma
                try {
                    val activity = context as? android.app.Activity
                    if (activity != null) {
                        // Guardar el idioma seleccionado
                        com.example.app.util.SessionManager.saveUserLanguage(language.code)
                        
                        // Aplicar el idioma
                        com.example.app.util.LocaleHelper.setLocale(context, language.code)
                        
                        // Crear un intent para reiniciar la actividad
                        val intent = activity.intent
                        activity.finish()
                        activity.startActivity(intent)
                        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                } catch (e: Exception) {
                    Log.e("LanguageMenuItem", "Error reiniciando actividad: ${e.message}")
                }
            }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bandera según el tipo
        Box(
            modifier = Modifier
                .size(38.dp, 26.dp)
                .clip(RoundedCornerShape(2.dp))
                .border(0.5.dp, Color.LightGray, RoundedCornerShape(2.dp))
        ) {
            when (language.flagType) {
                FlagType.SPAIN -> SpainFlag()
                FlagType.CATALONIA -> CataloniaFlag()
                FlagType.UK -> UkFlag()
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Nombre del idioma
        Text(
            text = language.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = textPrimaryColor
        )
    }
}

/**
 * Bandera de España
 */
@Composable
fun SpainFlag() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val height = size.height
        val width = size.width
        
        // Franja superior roja
        drawRect(
            color = españaRojo,
            size = Size(width, height / 3)
        )
        
        // Franja central amarilla
        drawRect(
            color = españaAmarillo,
            topLeft = Offset(0f, height / 3),
            size = Size(width, height / 3)
        )
        
        // Franja inferior roja
        drawRect(
            color = españaRojo,
            topLeft = Offset(0f, height * 2 / 3),
            size = Size(width, height / 3)
        )
        
        // Dibujar un elemento que represente el escudo
        val escudoWidth = width * 0.2f
        val escudoHeight = height * 0.5f
        val escudoX = width * 0.25f
        val escudoY = height / 4
        
        // Forma del escudo
        val escudoPath = Path().apply {
            moveTo(escudoX, escudoY)
            lineTo(escudoX + escudoWidth, escudoY)
            lineTo(escudoX + escudoWidth, escudoY + escudoHeight * 0.7f)
            lineTo(escudoX + escudoWidth / 2, escudoY + escudoHeight)
            lineTo(escudoX, escudoY + escudoHeight * 0.7f)
            close()
        }
        
        // Dibujar contorno del escudo
        drawPath(
            path = escudoPath,
            color = Color.DarkGray,
            style = Stroke(width = 1.5f)
        )
        
        // Dibujar interior del escudo
        drawPath(
            path = escudoPath,
            color = españaAmarillo.copy(alpha = 0.7f),
            style = Fill
        )
        
        // Detalles interiores del escudo (símbolos simplificados)
        drawCircle(
            color = españaRojo,
            radius = escudoWidth * 0.15f,
            center = Offset(escudoX + escudoWidth / 2, escudoY + escudoHeight * 0.3f)
        )
        
        // Líneas decorativas
        drawLine(
            color = españaRojo,
            start = Offset(escudoX + escudoWidth * 0.3f, escudoY + escudoHeight * 0.5f),
            end = Offset(escudoX + escudoWidth * 0.7f, escudoY + escudoHeight * 0.5f),
            strokeWidth = 1.5f
        )
        
        drawLine(
            color = españaRojo,
            start = Offset(escudoX + escudoWidth * 0.3f, escudoY + escudoHeight * 0.6f),
            end = Offset(escudoX + escudoWidth * 0.7f, escudoY + escudoHeight * 0.6f),
            strokeWidth = 1.5f
        )
    }
}

/**
 * Bandera de Cataluña
 */
@Composable
fun CataloniaFlag() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val height = size.height
        val width = size.width
        val stripeHeight = height / 9
        
        // Fondo amarillo
        drawRect(
            color = catalanAmarillo,
            size = Size(width, height)
        )
        
        // 4 Franjas rojas
        for (i in 0 until 4) {
            drawRect(
                color = catalanRojo,
                topLeft = Offset(0f, stripeHeight + i * stripeHeight * 2),
                size = Size(width, stripeHeight)
            )
        }
    }
}

/**
 * Bandera de Reino Unido
 */
@Composable
fun UkFlag() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val height = size.height
        val width = size.width
        
        // Fondo azul
        drawRect(
            color = ukAzul,
            size = Size(width, height)
        )
        
        // Cruz vertical blanca
        val verticalWidth = width / 6
        drawRect(
            color = ukBlanco,
            topLeft = Offset(width / 2 - verticalWidth / 2, 0f),
            size = Size(verticalWidth, height)
        )
        
        // Cruz horizontal blanca
        val horizontalHeight = height / 6
        drawRect(
            color = ukBlanco,
            topLeft = Offset(0f, height / 2 - horizontalHeight / 2),
            size = Size(width, horizontalHeight)
        )
        
        // Cruz vertical roja
        val verticalRedWidth = width / 10
        drawRect(
            color = ukRojo,
            topLeft = Offset(width / 2 - verticalRedWidth / 2, 0f),
            size = Size(verticalRedWidth, height)
        )
        
        // Cruz horizontal roja
        val horizontalRedHeight = height / 10
        drawRect(
            color = ukRojo,
            topLeft = Offset(0f, height / 2 - horizontalRedHeight / 2),
            size = Size(width, horizontalRedHeight)
        )
        
        // Diagonales blancas (aspas de San Patricio)
        val diagonalStrokeWidth = width / 12
        
        // Diagonal de arriba-izquierda a abajo-derecha
        val path1 = Path().apply {
            moveTo(-diagonalStrokeWidth, 0f)
            lineTo(0f, 0f)
            lineTo(width, height)
            lineTo(width, height + diagonalStrokeWidth)
            lineTo(width - diagonalStrokeWidth, height)
            lineTo(0f, diagonalStrokeWidth)
            close()
        }
        
        // Diagonal de arriba-derecha a abajo-izquierda
        val path2 = Path().apply {
            moveTo(width + diagonalStrokeWidth, 0f)
            lineTo(width, 0f)
            lineTo(0f, height)
            lineTo(0f, height + diagonalStrokeWidth)
            lineTo(diagonalStrokeWidth, height)
            lineTo(width, diagonalStrokeWidth)
            close()
        }
        
        drawPath(path1, color = ukBlanco)
        drawPath(path2, color = ukBlanco)
        
        // Diagonales rojas (más delgadas)
        val redDiagonalStrokeWidth = width / 20
        
        // Diagonal roja de arriba-izquierda a abajo-derecha
        val redPath1 = Path().apply {
            val offset = (diagonalStrokeWidth - redDiagonalStrokeWidth) / 2
            moveTo(-redDiagonalStrokeWidth + offset, offset)
            lineTo(0f + offset, offset)
            lineTo(width - offset, height - offset)
            lineTo(width - offset, height + redDiagonalStrokeWidth - offset)
            lineTo(width - redDiagonalStrokeWidth - offset, height - offset)
            lineTo(0f + offset, redDiagonalStrokeWidth + offset)
            close()
        }
        
        // Diagonal roja de arriba-derecha a abajo-izquierda
        val redPath2 = Path().apply {
            val offset = (diagonalStrokeWidth - redDiagonalStrokeWidth) / 2
            moveTo(width + redDiagonalStrokeWidth - offset, offset)
            lineTo(width - offset, offset)
            lineTo(0f + offset, height - offset)
            lineTo(0f + offset, height + redDiagonalStrokeWidth - offset)
            lineTo(redDiagonalStrokeWidth + offset, height - offset)
            lineTo(width - offset, redDiagonalStrokeWidth + offset)
            close()
        }
        
        drawPath(redPath1, color = ukRojo)
        drawPath(redPath2, color = ukRojo)
    }
}

/**
 * Tipos de banderas disponibles
 */
enum class FlagType {
    SPAIN, CATALONIA, UK
}

/**
 * Datos de un idioma
 */
data class LanguageItem(
    val code: String,
    val name: String,
    val flagType: FlagType
) 