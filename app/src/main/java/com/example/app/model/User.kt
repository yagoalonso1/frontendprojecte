package com.example.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    
    @ColumnInfo(name = "nombre")
    val nombre: String,
    
    @ColumnInfo(name = "apellido1")
    val apellido1: String,
    
    @ColumnInfo(name = "apellido2")
    val apellido2: String?,
    
    @ColumnInfo(name = "email")
    val email: String,
    
    @ColumnInfo(name = "role")
    val role: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String?,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: String?
)