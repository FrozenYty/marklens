package com.example.marklens.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.marklens.data.entity.RegionTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface RegionTemplateDao {
    @Insert
    suspend fun insert(template: RegionTemplate): Long

    @Query("SELECT * FROM region_templates ORDER BY name")
    fun getAll(): Flow<List<RegionTemplate>>

    @Delete
    suspend fun delete(template: RegionTemplate)
}
