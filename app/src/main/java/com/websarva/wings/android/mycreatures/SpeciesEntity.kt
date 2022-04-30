package com.websarva.wings.android.mycreatures

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// ユーザー情報とか
@Entity(tableName="creatures")
data class SpeciesEntity (
    @PrimaryKey
    @ColumnInfo(name="name")
    val name: String = "",

    @ColumnInfo(name="explanation")
    var explanation: String,

    @ColumnInfo(name="parentName")
    var parentName: List<String>,

    @ColumnInfo(name="childrenName")
    var childrenName: List<String>,

    @ColumnInfo(name = "imageUris")
    var imageUris: ArrayList<String> = arrayListOf(),

    @ColumnInfo(name = "created_at")
    var createdAt: Long = 0,

    @ColumnInfo(name = "modified_at")
    var modifiedAt: Long = 0
)