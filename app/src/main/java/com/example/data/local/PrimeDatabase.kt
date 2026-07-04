package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.PrimeCheck

@Database(entities = [PrimeCheck::class], version = 1, exportSchema = false)
abstract class PrimeDatabase : RoomDatabase() {
    abstract fun primeCheckDao(): PrimeCheckDao

    companion object {
        @Volatile
        private var INSTANCE: PrimeDatabase? = null

        fun getDatabase(context: Context): PrimeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PrimeDatabase::class.java,
                    "prime_checker_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
