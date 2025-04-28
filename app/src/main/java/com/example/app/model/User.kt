package com.example.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @SerializedName("id")
    @ColumnInfo(name = "id")
    val id: Int,
    
    @SerializedName("nombre")
    @ColumnInfo(name = "nombre")
    val nombre: String,
    
    @SerializedName("apellido1")
    @ColumnInfo(name = "apellido1")
    val apellido1: String,
    
    @SerializedName("apellido2")
    @ColumnInfo(name = "apellido2")
    val apellido2: String?,
    
    @SerializedName("email")
    @ColumnInfo(name = "email")
    val email: String,
    
    @SerializedName("role")
    @ColumnInfo(name = "role")
    val role: String,
    
    @SerializedName("created_at")
    @ColumnInfo(name = "created_at")
    val createdAt: String?,
    
    @SerializedName("updated_at")
    @ColumnInfo(name = "updated_at")
    val updatedAt: String?,
    
    @SerializedName("avatar_url")
    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String?
)