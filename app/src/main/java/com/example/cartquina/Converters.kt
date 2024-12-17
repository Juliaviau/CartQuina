package com.example.cartquina

import androidx.room.TypeConverter
import com.google.gson.Gson

class Converters {
    @TypeConverter
    fun fromList(numbers: List<Int?>?): String {
        return Gson().toJson(numbers)
    }

    @TypeConverter
    fun toList(numbersString: String): List<Int?> {
        val type = object : com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken<List<Int?>>() {}.type
        return Gson().fromJson(numbersString, type)
    }
}
