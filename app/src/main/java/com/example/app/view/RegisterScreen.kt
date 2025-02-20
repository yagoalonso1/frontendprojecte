package com.example.app.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Barra superior
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = {            }) {
                Text(
                    text = "← Volver atrás",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = "Iniciar sesión",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Crea una cuenta",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Correo Electrónico
        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
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

        Spacer(modifier = Modifier.height(16.dp))

        // Nombre
        OutlinedTextField(
            value = viewModel.name,
            onValueChange = { viewModel.name = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Selección de Rol
        Text(
            text = "¿Qué eres?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { viewModel.role = "Organizador" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.role == "Organizador") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (viewModel.role == "Organizador") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Organizador")
            }
            Button(
                onClick = { viewModel.role = "Participante" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.role == "Participante") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (viewModel.role == "Participante") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Participante")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón de Continuar
        Button(
            onClick = {
                viewModel.onRegisterClick()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Continuar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar mensaje de error
        if (viewModel.errorMessage.isNotEmpty()) {
            Text(
                text = viewModel.errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Redirigir a Login en caso de registro exitoso
        if (viewModel.isRegisterSuccessful) {
            LaunchedEffect(Unit) {
                onNavigateToLogin()
            }
        }
    }
}