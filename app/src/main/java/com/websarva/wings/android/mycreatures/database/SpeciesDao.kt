package com.websarva.wings.android.mycreatures.database

import androidx.room.*

@Dao
interface SpeciesDao {
    @Insert
    suspend fun insert(species: SpeciesEntity)

    @Update
    suspend fun update(species: SpeciesEntity)

    @Delete
    suspend fun delete(species: SpeciesEntity)

    @Query("DELETE FROM creatures")
    suspend fun clear()

    @Query("DELETE FROM creatures WHERE name = :key")
    suspend fun deleteByKey(key: String)

    @Query("SELECT * FROM creatures WHERE id = :id")
    suspend fun get(id: Int): SpeciesEntity?

    @Query("SELECT * FROM creatures WHERE name = :key")
    suspend fun getByName(key: String): SpeciesEntity?

    @Query("SELECT * FROM creatures WHERE parent = :key")
    suspend fun getChildrenItem(key: Int): List<SpeciesEntity>

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

    suspend fun insertWithTimestampOld(species: SpeciesEntity) {
        insert(species.apply{
            createdAt = System.currentTimeMillis()
            modifiedAt = System.currentTimeMillis()
        })
    }
    suspend fun updateWithTimestamp(species: SpeciesEntity) {
        update(species.apply{
            modifiedAt = System.currentTimeMillis()
        })
    }

    @Query ("SELECT * FROM creatures WHERE created_at > :time")
    suspend fun getRecentItems(time: Long): List<SpeciesEntity>

    @Query("SELECT EXISTS(SELECT * FROM creatures WHERE name = :key)")
    suspend fun isRowIsExist(key: String) : Boolean
}