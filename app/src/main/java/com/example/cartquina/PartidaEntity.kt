package com.example.cartquina

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "partides")
data class PartidaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val data: String, // Fecha de la partida
    val numerosDit: List<Int> = listOf(), // Números llamados
    val estat: String, // Puede ser "quines" o "linia"
    val cartronsAsignats: List<Int> = listOf() // Lista de IDs de los cartones asignados a la partida
)
