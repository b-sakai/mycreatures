package com.websarva.wings.android.mycreatures.database

import androidx.room.TypeConverter
import java.util.*


// Room型変換
internal class RoomTypeConverters {
    companion object {
        @JvmStatic
        @TypeConverter
        fun commaStringToList(commaString: String): List<String> {
            return if (commaString == "") {
                listOf<String>()
            } else {
                commaString.split(",").map { it.trim() }
            }
        }

        @JvmStatic
        @TypeConverter
        fun listToComma(list: List<String>) = list.toTypedArray().joinToString(",")

        @JvmStatic
        @TypeConverter
        fun commaStringToArrayList(commaString: String): ArrayList<String> =
            commaString.split(",").map { it.trim() } as ArrayList<String>

        @JvmStatic
        @TypeConverter
        fun arrayListToComma(list: ArrayList<String>) = list.toTypedArray().joinToString(",")
    }



}