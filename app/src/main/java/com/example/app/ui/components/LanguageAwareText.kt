package com.example.app.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.app.util.LocaleHelper

/**
 * Componente Text que se actualiza automáticamente cuando cambia el idioma
 * sin necesidad de recrear la actividad
 */
@Composable
fun LanguageAwareText(
    textId: Int,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    // Observar cambios en el idioma mediante el timestamp
    val languageUpdateTimestamp = LocaleHelper.languageFlow.collectAsState().value
    
    // Obtener el contexto actual para acceder a los recursos
    val context = LocalContext.current
    
    // Obtener el texto del recurso - se recargará cuando cambie el idioma
    val text = remember(languageUpdateTimestamp) { 
        context.resources.getString(textId)
    }
    
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}

/**
 * Versión de LanguageAwareText para cuando necesitas formatear el texto
 */
@Composable
fun LanguageAwareText(
    textId: Int,
    vararg formatArgs: Any,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    // Observar cambios en el idioma mediante el timestamp
    val languageUpdateTimestamp = LocaleHelper.languageFlow.collectAsState().value
    
    // Obtener el contexto actual para acceder a los recursos
    val context = LocalContext.current
    
    // Obtener el texto del recurso - se recargará cuando cambie el idioma o los argumentos
    val text = remember(languageUpdateTimestamp, formatArgs) { 
        context.resources.getString(textId, *formatArgs)
    }
    
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
} 