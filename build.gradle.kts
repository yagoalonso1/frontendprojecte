plugins {
    id("com.android.application") version "8.3.0" apply false 
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false 
    id("com.google.dagger.hilt.android") version "2.51.1" apply false 
}

// Configuración para asegurar la compatibilidad de versiones SDK
buildscript {
    repositories {
        google()
        mavenCentral()
    }
}
