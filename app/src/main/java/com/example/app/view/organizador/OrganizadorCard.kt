package com.example.app.view.organizador

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.app.model.Organizador
import coil.compose.SubcomposeAsyncImage

@Composable
fun OrganizadorCard(
    organizador: Organizador,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    primaryColor: Color,
    textPrimaryColor: Color,
    textSecondaryColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del organizador
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(organizador.obtenerAvatarUrl())
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar de ${organizador.nombre}",
                contentScale = ContentScale.Crop,
                loading = {
                    CircularProgressIndicator(
                        color = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                },
                error = {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        color = primaryColor.copy(alpha = 0.15f),
                        border = BorderStroke(2.dp, primaryColor.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Avatar por defecto",
                            modifier = Modifier
                                .padding(12.dp)
                                .size(24.dp),
                            tint = primaryColor
                        )
                    }
                },
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
            
            // Información del organizador
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                // Nombre de la organización (arriba, destacado)
                Text(
                    text = organizador.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Nombre completo del usuario (abajo, mismo tamaño que el teléfono)
                organizador.user?.let { user ->
                    val nombreCompleto = buildString {
                        append(user.nombre)
                        if (!user.apellido1.isNullOrBlank()) append(" ").append(user.apellido1)
                        if (!user.apellido2.isNullOrBlank()) append(" ").append(user.apellido2)
                    }
                    Text(
                        text = nombreCompleto,
                        fontSize = 13.sp,
                        color = textSecondaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
} 