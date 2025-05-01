package com.example.app.util

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

data class GoogleUserInfo(
    val email: String,
    val nombre: String,
    val apellido1: String,
    val apellido2: String?,
    val photoUrl: String?
)

class GoogleAuthHelper(private val context: Context) {
    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestProfile()
        .requestId()
        .build()

    private val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun handleSignInResult(task: Task<GoogleSignInAccount>): GoogleUserInfo? {
        return try {
            val account = task.getResult(ApiException::class.java)
            Log.d("GoogleAuthHelper", "Login exitoso: ${account.email}")
            
            val fullName = account.displayName ?: ""
            val nameParts = fullName.split(" ")
            
            val (nombre, apellido1, apellido2) = when {
                nameParts.size >= 3 -> Triple(nameParts[0], nameParts[1], nameParts.subList(2, nameParts.size).joinToString(" "))
                nameParts.size == 2 -> Triple(nameParts[0], nameParts[1], null)
                nameParts.size == 1 -> Triple(nameParts[0], "Usuario", null)
                else -> Triple("Usuario", "Google", null)
            }

            GoogleUserInfo(
                email = account.email ?: "",
                nombre = nombre,
                apellido1 = apellido1,
                apellido2 = apellido2,
                photoUrl = account.photoUrl?.toString()
            )
        } catch (e: ApiException) {
            Log.e("GoogleAuthHelper", "Error en el login con Google: ${e.message}")
            null
        }
    }

    fun signOut() {
        googleSignInClient.signOut()
    }
} 