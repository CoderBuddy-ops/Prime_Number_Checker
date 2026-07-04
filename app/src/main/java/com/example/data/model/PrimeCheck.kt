package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prime_checks")
data class PrimeCheck(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val numberString: String,
    val isPrime: Boolean,
    val resultType: String, // "PRIME", "COMPOSITE", "NEITHER", "INVALID"
    val explanation: String,
    val checkDurationMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)
