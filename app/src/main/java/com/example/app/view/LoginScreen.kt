package com.example.app.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.viewmodel.LoginViewModel
import com.example.app.R

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToRecoverPassword: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Volver atrás y Registrarse
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { /* Accion de Volver atrás */ }) {
                Text(text = "← Volver atrás")
            }
            TextButton(onClick = onNavigateToRegister) {
                Text(text = "Registrarse")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Título
        Text(text = "Eventclix", style = MaterialTheme.typography.titleLarge)
        Text(text = "Inicia sesión", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(32.dp))

        // Correo Electrónico
        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Contraseña
        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón de Continuar
        Button(
            onClick = { /* Accion de Iniciar sesión */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Divider
        Divider()

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de Google
        OutlinedButton(
            onClick = { /* Accion de Iniciar sesión con Google */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar con Google")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Recuperar contraseña
        Text("¿Olvidaste tu contraseña?")
        ClickableText(
            text = androidx.compose.ui.text.AnnotatedString("Recuperar contraseña"),
            onClick = { onNavigateToRecoverPassword() },
            style = TextStyle(
                color = Color.Blue,
                textDecoration = TextDecoration.Underline
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Imagen decorativa (cambia el recurso según tu imagen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}