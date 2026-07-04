package com.example.data.repository

import com.example.data.local.PrimeCheckDao
import com.example.data.model.PrimeCheck
import kotlinx.coroutines.flow.Flow

class PrimeRepository(private val primeCheckDao: PrimeCheckDao) {
    val allChecks: Flow<List<PrimeCheck>> = primeCheckDao.getAllChecks()

    suspend fun insertCheck(check: PrimeCheck) {
        primeCheckDao.insertCheck(check)
    }

    suspend fun deleteCheckById(id: Int) {
        primeCheckDao.deleteCheckById(id)
    }

    suspend fun clearHistory() {
        primeCheckDao.clearHistory()
    }
}
