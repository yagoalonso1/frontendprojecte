package com.example.app

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.app.routes.AppNavHost
import com.example.app.ui.theme.AppTheme
import com.example.app.model.tickets.TicketCompra
import com.example.app.util.GoogleCalendarHelper
import com.example.app.util.LocaleHelper
import com.example.app.util.SessionManager

class MainActivity : ComponentActivity() {
    
    // Este método puede ser llamado desde cualquier composable o fragmento
    fun agregarEventoAlCalendario(ticket: TicketCompra) {
        val calendarHelper = GoogleCalendarHelper(this)
        calendarHelper.addEventToCalendarFromActivity(this, ticket)
    }
    
    // Sobreescribir attachBaseContext para aplicar el idioma
    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = SessionManager.getUserLanguage()
        Log.d("MainActivity", "Aplicando idioma guardado en attachBaseContext: $savedLanguage")
        val context = LocaleHelper.setLocale(newBase, savedLanguage ?: "es")
        super.attachBaseContext(context)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Actualizar idioma en el onCreate también
        val savedLanguage = SessionManager.getUserLanguage()
        Log.d("MainActivity", "Verificando idioma en onCreate: $savedLanguage")
        LocaleHelper.setLocale(this, savedLanguage ?: "es")
        
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavHost(navController = navController)
                }
            }
        }
    }
}