package com.example.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.app.model.User

@Entity(
    tableName = "participante",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("idUser"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Participante(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "idParticipante")
    val idParticipante: Int = 0,

    @ColumnInfo(name = "dni")
    val dni: String,

    @ColumnInfo(name = "telefono")
    val telefono: String,

    @ColumnInfo(name = "idUser")
    val idUser: Int,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: String? = null
) 