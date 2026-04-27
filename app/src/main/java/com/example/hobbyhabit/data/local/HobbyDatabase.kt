package com.example.hobbyhabit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Hobby::class, Session::class, User::class],
    version = 4,
    exportSchema = false
)
abstract class HobbyDatabase : RoomDatabase() {

    abstract fun hobbyDao(): HobbyDao
    abstract fun sessionDao(): SessionDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: HobbyDatabase? = null

        fun getDatabase(context: Context): HobbyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HobbyDatabase::class.java,
                    "hobby_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}