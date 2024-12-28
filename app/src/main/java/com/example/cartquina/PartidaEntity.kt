package com.example.cartquina

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "partides")
data class PartidaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val data: String,
    val numerosDit: List<Int> = listOf(),
    val estat: String,
    val cartronsAsignats: List<Int> = listOf()
)
