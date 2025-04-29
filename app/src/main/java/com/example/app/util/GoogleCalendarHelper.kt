package com.example.app.util

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Collections

class GoogleCalendarHelper(private val context: Context) {
    private val SCOPES = Collections.singleton(CalendarScopes.CALENDAR)

    suspend fun getCalendarService(account: GoogleSignInAccount): Calendar {
        return withContext(Dispatchers.IO) {
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                SCOPES
            ).setSelectedAccount(account.account)

            Calendar.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName("Tu App")
                .build()
        }
    }
} 