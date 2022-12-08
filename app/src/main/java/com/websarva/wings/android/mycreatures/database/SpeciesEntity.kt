package com.websarva.wings.android.mycreatures.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// ユーザー情報とか
@Entity(tableName="creatures")
data class SpeciesEntity (
    @PrimaryKey(autoGenerate = true)
    val id : Int,

    @ColumnInfo(name="parent")
    var parent: Int = -1,

    @ColumnInfo(name="name")
    var name: String = "",

    @ColumnInfo(name="explanation")
    var explanation: String,

    @ColumnInfo(name = "imageUris")
    var imageUris: ArrayList<String> = arrayListOf(),

    @ColumnInfo(name = "created_at")
    var createdAt: Long = 0,

    @ColumnInfo(name = "modified_at")
    var modifiedAt: Long = 0
)