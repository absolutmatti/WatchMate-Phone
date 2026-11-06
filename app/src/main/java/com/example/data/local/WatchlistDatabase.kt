package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WatchlistItem::class], version = 1, exportSchema = false)
abstract class WatchlistDatabase : RoomDatabase() {

    abstract fun watchlistDao(): WatchlistDao

    companion object {
        @Volatile
        private var INSTANCE: WatchlistDatabase? = null

        fun getDatabase(context: Context): WatchlistDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WatchlistDatabase::class.java,
                    "watchmate_tv.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
