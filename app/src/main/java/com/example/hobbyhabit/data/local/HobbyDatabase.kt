package com.example.hobbyhabit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@TypeConverters(EventConverters::class)
@Database(
    entities = [Hobby::class, Session::class, User::class, Event::class],
    version = 8,
    exportSchema = false
)
abstract class HobbyDatabase : RoomDatabase() {

    abstract fun hobbyDao(): HobbyDao
    abstract fun sessionDao(): SessionDao
    abstract fun userDao(): UserDao
    abstract fun eventDao(): EventDao

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