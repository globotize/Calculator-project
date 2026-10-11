package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {
    @Query("SELECT * FROM calculations ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CalculationEntity>>

    @Query("SELECT * FROM calculations WHERE isStarred = 1 ORDER BY timestamp DESC")
    fun getStarredHistory(): Flow<List<CalculationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(calculation: CalculationEntity): Long

    @Update
    suspend fun update(calculation: CalculationEntity)

    @Delete
    suspend fun delete(calculation: CalculationEntity)

    @Query("DELETE FROM calculations")
    suspend fun clearAll()

    @Query("DELETE FROM calculations WHERE isStarred = 0")
    suspend fun clearUnstarred()
}
