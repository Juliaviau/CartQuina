package com.example.cartquina

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

//Accedir a la base de dades
@Dao
interface CartroDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartro(cartro: CartroEntity) : Long

    @Query("SELECT * FROM cartrons")
    suspend fun getAllCartros(): List<CartroEntity>

    @Delete
    suspend fun deleteCartro(cartro: CartroEntity)

    // Operaciones para partidas
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartida(partida: PartidaEntity)

    @Query("SELECT * FROM partides")
    suspend fun getAllPartides(): List<PartidaEntity>

    @Delete
    suspend fun deletePartida(partida: PartidaEntity)

    // Operaciones para el estado de los cartones
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartroEstat(cartroEstat: CartroEstatEntity)

    @Query("SELECT * FROM cartro_estat WHERE cartroId = :cartroId")
     fun getCartroEstat(cartroId: Int): CartroEstatEntity?

    @Delete
    suspend fun deleteCartroEstat(cartroEstat: CartroEstatEntity)

    @Query("SELECT * FROM cartrons WHERE id = :cartroId")
    suspend fun getCartroById(cartroId: Int): CartroEntity?

    @Query("SELECT * FROM partides WHERE id = :partidaId")
    suspend fun getPartidaById(partidaId: Int): PartidaEntity?

    @Update
    suspend fun updatePartida(partida: PartidaEntity)
}
