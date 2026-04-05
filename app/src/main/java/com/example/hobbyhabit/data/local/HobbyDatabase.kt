package com.example.hobbyhabit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Hobby::class, Session::class],
    version = 1,
    exportSchema = false
)
abstract class HobbyDatabase : RoomDatabase() {

    abstract fun hobbyDao(): HobbyDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: HobbyDatabase? = null

        fun getDatabase(context: Context): HobbyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HobbyDatabase::class.java,
                    "hobby_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
