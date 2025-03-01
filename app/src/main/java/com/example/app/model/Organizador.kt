package com.example.app.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.app.model.User

@Entity(
    tableName = "organizador",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("idUser"),
            childColumns = arrayOf("user_id")
        )
    ]
)

data class Organizador(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "idOrganizador")
    val idOrganizador: Int = 0,

    @ColumnInfo(name = "nombre_organizacion")
    val nombreOrganizacion: String,

    @ColumnInfo(name = "telefono_contacto")
    val telefonoContacto: String,

    @ColumnInfo(name = "user_id")
    val userId: Int,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: String? = null
)