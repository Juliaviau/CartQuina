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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCartronosuspend(cartro: CartroEntity) : Long

    @Query("SELECT * FROM cartrons")
    suspend fun getAllCartros(): List<CartroEntity>

    @Query("SELECT * FROM cartrons")
    fun getAllCartrosUn(): List<CartroEntity>

    @Delete
    fun deleteCartro(cartro: CartroEntity)

    @Update
    fun updateCartro(cartro: CartroEntity)

    // Operaciones para partidas
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPartida(partida: PartidaEntity) : Long

    @Query("SELECT * FROM partides")
    suspend fun getAllPartides(): List<PartidaEntity>

    @Query("SELECT * FROM partides")
    fun getAllPartidesUn(): List<PartidaEntity>

    @Delete
    fun deletePartida(partida: PartidaEntity)

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
    fun updatePartida(partida: PartidaEntity)
}
