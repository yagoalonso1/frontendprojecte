# 🎫 EventApp - Aplicación de Gestión de Eventos

## 📱 Sobre el Proyecto

EventApp es una aplicación móvil moderna desarrollada en Kotlin con Jetpack Compose que permite a los usuarios gestionar, comprar y organizar eventos de manera intuitiva y eficiente. La aplicación ofrece una experiencia de usuario fluida y moderna, siguiendo las últimas tendencias en diseño de Material Design 3.

### 🎯 Objetivos del Proyecto

- Proporcionar una plataforma intuitiva para la gestión de eventos
- Facilitar la compra y venta de entradas de manera segura
- Ofrecer herramientas avanzadas para organizadores de eventos
- Mejorar la experiencia del usuario en eventos presenciales y virtuales
- Integrar funcionalidades sociales para compartir y descubrir eventos

### 🌟 Características Principales

#### 👤 Para Participantes:
- **Exploración de Eventos:**
  - Búsqueda avanzada con filtros
  - Categorización por tipo, fecha y ubicación
  - Vista de mapa interactivo
  - Recomendaciones personalizadas

- **Gestión de Entradas:**
  - Proceso de compra seguro con múltiples métodos de pago
  - Generación de entradas en PDF con códigos QR
  - Sistema de reembolsos automatizado
  - Transferencia de entradas entre usuarios

- **Funcionalidades Personales:**
  - Perfil personalizado con historial de eventos
  - Sistema de valoraciones y reseñas
  - Lista de deseos y favoritos
  - Notificaciones personalizadas
  - Integración con calendario del dispositivo

- **Características Sociales:**
  - Compartir eventos en redes sociales
  - Crear grupos de asistentes
  - Chat integrado para grupos de eventos
  - Sistema de seguimiento de organizadores

#### 🎭 Para Organizadores:
- **Gestión de Eventos:**
  - Panel de control completo
  - Creación de eventos con plantillas
  - Gestión de múltiples tipos de entradas
  - Configuración de precios dinámicos
  - Sistema de códigos promocionales

- **Herramientas de Marketing:**
  - Análisis de datos y estadísticas
  - Informes de ventas en tiempo real
  - Herramientas de email marketing
  - Gestión de redes sociales

- **Gestión de Asistentes:**
  - Control de acceso mediante QR
  - Gestión de lista de espera
  - Sistema de acreditaciones
  - Comunicación masiva con asistentes

### 🛠️ Tecnologías Utilizadas

#### Frontend
- **UI/UX:**
  - Jetpack Compose (última versión)
  - Material Design 3
  - Animaciones personalizadas
  - Temas dinámicos y modo oscuro

- **Arquitectura:**
  - MVVM (Model-View-ViewModel)
  - Clean Architecture
  - Repository Pattern
  - Use Cases

#### Backend y Servicios
- **API y Networking:**
  - REST API con Laravel
  - GraphQL para consultas complejas
  - WebSockets para tiempo real
  - Cache con Redis

- **Almacenamiento:**
  - Room Database
  - SharedPreferences
  - Firebase Cloud Storage
  - SQLite local

#### Seguridad
- **Autenticación:**
  - JWT (JSON Web Tokens)
  - OAuth 2.0
  - Biometric Authentication
  - Google Sign-In

#### Integraciones
- **APIs Externas:**
  - Google Maps Platform
  - Stripe Payments
  - Firebase Analytics
  - Google Calendar API

### 📚 Bibliotecas Principales

```kotlin
dependencies {
    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.runtime:runtime:1.5.4")
    implementation("androidx.compose.foundation:foundation:1.5.4")
    
    // Navegación
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("androidx.navigation:navigation-runtime-ktx:2.7.5")
    
    // Lifecycle y ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    
    // Inyección de Dependencias
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    
    // Imágenes y Multimedia
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("com.github.bumptech.glide:compose:1.0.0-alpha.5")
    
    // Almacenamiento
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    kapt("androidx.room:room-compiler:2.6.0")
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
}
```

## 🚀 Instalación y Configuración

### Requisitos Previos
- Android Studio Hedgehog | 2023.1.1 o superior
- JDK 17 o superior
- Kotlin 1.9.0 o superior
- Gradle 8.0 o superior

### Pasos de Instalación

1. **Clonar el Repositorio:**
   ```bash
   git clone https://github.com/tuusuario/eventapp.git
   cd eventapp
   ```

2. **Configurar Variables de Entorno:**
   Crea un archivo `local.properties` en la raíz del proyecto:
   ```properties
   sdk.dir=TU_RUTA_SDK_ANDROID
   BASE_URL="TU_URL_API"
   MAPS_API_KEY="TU_CLAVE_API_GOOGLE_MAPS"
   STRIPE_PUBLIC_KEY="TU_CLAVE_PUBLICA_STRIPE"
   FIREBASE_APP_ID="TU_APP_ID_FIREBASE"
   ```

3. **Configurar Firebase:**
   - Descarga el archivo `google-services.json`
   - Colócalo en la carpeta `app/`

4. **Sincronizar y Compilar:**
   ```bash
   ./gradlew clean build
   ```

### Configuración del Entorno de Desarrollo

1. **Android Studio:**
   - Instalar plugins recomendados
   - Configurar el emulador o dispositivo físico
   - Verificar la configuración de Gradle

2. **Configuración de Git:**
   ```bash
   git config user.name "Tu Nombre"
   git config user.email "tu@email.com"
   ```

## 🏗️ Arquitectura

### Estructura del Proyecto
```
app/
├── api/                 # Servicios de red y modelos de API
│   ├── interceptors/    # Interceptores de red
│   ├── models/          # Modelos de datos API
│   └── services/        # Interfaces de servicios
├── di/                  # Módulos de inyección de dependencias
│   ├── modules/         # Módulos Hilt
│   └── qualifiers/      # Calificadores personalizados
├── domain/             # Lógica de negocio
│   ├── models/          # Modelos de dominio
│   ├── repositories/    # Interfaces de repositorio
│   └── usecases/       # Casos de uso
├── data/              # Implementación de datos
│   ├── local/          # Fuentes de datos locales
│   ├── remote/         # Fuentes de datos remotas
│   └── repositories/   # Implementaciones de repositorio
├── ui/
│   ├── components/     # Componentes reutilizables
│   ├── screens/        # Pantallas de la aplicación
│   ├── theme/          # Temas y estilos
│   └── navigation/     # Navegación
├── util/              # Utilidades y extensiones
└── viewmodel/         # ViewModels
```

### Patrones de Diseño Implementados
- **MVVM (Model-View-ViewModel)**
- **Repository Pattern**
- **Factory Pattern**
- **Dependency Injection**
- **Observer Pattern**
- **Builder Pattern**

## 🔐 Seguridad

### Medidas Implementadas
- **Autenticación:**
  - JWT con renovación automática
  - Almacenamiento seguro de tokens
  - Biometric authentication
  
- **Datos Sensibles:**
  - Encriptación AES-256
  - Secure SharedPreferences
  - ProGuard/R8 optimización
  
- **Red:**
  - Certificate Pinning
  - HTTPS obligatorio
  - Validación de certificados

### Buenas Prácticas
- Sanitización de inputs
- Prevención de inyección SQL
- Rate limiting
- Logging seguro

## 🌐 API y Endpoints

### Base URL
```
https://api.eventapp.com/v1/
```

### Endpoints Principales

#### Autenticación
```
POST /auth/login
POST /auth/register
POST /auth/refresh
POST /auth/logout
```

#### Eventos
```
GET /events
POST /events
GET /events/{id}
PUT /events/{id}
DELETE /events/{id}
```

#### Entradas
```
GET /tickets
POST /tickets/purchase
GET /tickets/{id}
POST /tickets/{id}/transfer
```

## 📱 Capturas de Pantalla y Diseño

### Pantallas Principales
[Aquí se incluirían las capturas de pantalla organizadas por sección]

### Guía de Estilos
- **Colores:**
  - Primary: #FF5722
  - Secondary: #2196F3
  - Background: #FFFFFF
  - Surface: #F5F5F5
  
- **Tipografía:**
  - Familia: Roboto
  - Tamaños: 12sp - 24sp
  
- **Espaciado:**
  - Padding: 8dp - 24dp
  - Márgenes: 16dp - 32dp

## 🤝 Contribución

### Proceso de Contribución
1. Fork del repositorio
2. Crear rama feature (`git checkout -b feature/NuevaCaracteristica`)
3. Commit cambios (`git commit -m 'Añadir nueva característica'`)
4. Push a la rama (`git push origin feature/NuevaCaracteristica`)
5. Crear Pull Request

### Guías de Contribución
- Seguir convenciones de código
- Documentar cambios
- Añadir tests unitarios
- Mantener compatibilidad

### Flujo de Trabajo Git
- Main: Producción
- Develop: Desarrollo
- Feature/*: Nuevas características
- Hotfix/*: Correcciones urgentes

## ✅ Testing

### Tipos de Tests
- **Unitarios:** JUnit, Mockito
- **Integración:** Espresso
- **UI:** Compose Testing
- **End-to-End:** Maestro

### Cobertura de Código
- Mínimo 80% en lógica de negocio
- Reportes automáticos en CI/CD

## 📈 Análisis y Monitoreo

### Herramientas
- Firebase Analytics
- Crashlytics
- Performance Monitoring
- Google Analytics

### Métricas Principales
- Tiempo de inicio
- Tasa de errores
- Uso de memoria
- Rendimiento de red

## ✍️ Autores

- **Yago Alonso** - *Frontend Developer* - [GitHub](https://github.com/tuusuario)
  - Especializado en UI/UX y arquitectura
  - Líder técnico del proyecto

- **Arnau Gil** - *Frontend Developer* - [GitHub](https://github.com/tuusuario)
  - Experto en integración de APIs
  - Desarrollo de funcionalidades core

- **Alex Vilanova** - *Frontend Developer* - [GitHub](https://github.com/tuusuario)
  - Especialista en testing y seguridad
  - Optimización de rendimiento

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE.md](LICENSE.md) para detalles

### Términos Principales
- Uso comercial permitido
- Modificación permitida
- Distribución permitida
- Uso privado permitido

## 🙏 Agradecimientos

- **Frameworks y Librerías:**
  - Material Design por la guía de diseño
  - JetBrains por Android Studio
  - Google por Jetpack Compose
  
- **Recursos:**
  - Icons8 por los iconos
  - Unsplash por las imágenes
  - Firebase por la infraestructura

- **Comunidad:**
  - Contribuidores de código abierto
  - Beta testers
  - Usuarios iniciales

## 📞 Soporte y Contacto

### Canales de Soporte
- Email: support@eventapp.com
- Discord: [EventApp Community]
- Twitter: @EventApp

### Reportar Problemas
- Usar GitHub Issues
- Incluir logs y pasos de reproducción
- Adjuntar capturas de pantalla

---
Desarrollado con ❤️ por Yago Alonso, Arnau Gil y Alex Vilanova

[Última actualización: 2024] 