package com.websarva.wings.android.mycreatures

import android.media.Image
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SpeciesDao {
    @Insert
    suspend fun insert(species: SpeciesEntity)

    @Delete
    suspend fun delete(species: SpeciesEntity)

    @Query("DELETE FROM creatures")
    suspend fun clear()

    @Query("DELETE FROM creatures WHERE name = :key")
    suspend fun deleteByKey(key: String)

    @Query("SELECT * FROM creatures WHERE name = :key")
    suspend fun get(key: String): SpeciesEntity?

    @Query("SELECT * FROM creatures")
    suspend fun getAllItem(): List<SpeciesEntity>

    @Query("SELECT * FROM creatures")
    fun getAllItemUIThread(): List<SpeciesEntity>

    suspend fun insertWithTimestamp(species: SpeciesEntity) {
        insert(species.apply{
            createdAt = System.currentTimeMillis()
            modifiedAt = System.currentTimeMillis()
        })
    }
    suspend fun updateWithTimestamp(species: SpeciesEntity) {
        insert(species.apply{
            modifiedAt = System.currentTimeMillis()
        })
    }

    @Query ("SELECT * FROM creatures WHERE created_at > :time")
    suspend fun getRecentItems(time: Long): List<SpeciesEntity>

    @Query("SELECT EXISTS(SELECT * FROM creatures WHERE name = :key)")
    suspend fun isRowIsExist(key: String) : Boolean
}