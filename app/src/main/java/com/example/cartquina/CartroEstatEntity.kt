package com.example.cartquina

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "cartro_estat")
data class CartroEstatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cartroId: Int,
    val numerusTachats: List<Int> = listOf()
)
