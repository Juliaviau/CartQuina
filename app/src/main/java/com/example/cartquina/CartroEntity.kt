package com.example.cartquina

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.Serial

//Taula
@Serializable
@Entity(tableName = "cartrons")
data class CartroEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    @SerialName("numbers")
    val numeros: List<Int?> = emptyList(),/*, // Llista dels 27 números o nulls

    @SerialName("data")
    val data: String? = null*/
)
const val tableName = "CartroEntity"
