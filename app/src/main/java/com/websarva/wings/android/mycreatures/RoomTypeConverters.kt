package com.websarva.wings.android.mycreatures

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64.decode
import android.util.Base64.encodeToString
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream
import java.lang.Double.min
import java.util.*


// Room型変換
internal class RoomTypeConverters {
    companion object {
        @JvmStatic
        @TypeConverter
        fun commaStringToList(commaString: String): List<String> =
            commaString.split(",").map { it.trim() }

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