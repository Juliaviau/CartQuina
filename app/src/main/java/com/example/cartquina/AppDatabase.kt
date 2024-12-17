package com.example.cartquina

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [CartroEntity::class, PartidaEntity::class, CartroEstatEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartroDao(): CartroDao
}
