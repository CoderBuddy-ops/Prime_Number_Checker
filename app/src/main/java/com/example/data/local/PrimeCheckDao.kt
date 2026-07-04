package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PrimeCheck
import kotlinx.coroutines.flow.Flow

@Dao
interface PrimeCheckDao {
    @Query("SELECT * FROM prime_checks ORDER BY timestamp DESC")
    fun getAllChecks(): Flow<List<PrimeCheck>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheck(check: PrimeCheck)

    @Query("DELETE FROM prime_checks WHERE id = :id")
    suspend fun deleteCheckById(id: Int)

    @Query("DELETE FROM prime_checks")
    suspend fun clearHistory()
}
