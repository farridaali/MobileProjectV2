package com.example.mobileprojectv2

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database([ItemEntity::class], version = 1, exportSchema = false)
abstract class GroceryDatabase: RoomDatabase() {

    abstract fun GroceryDao(): GroceryDao

    companion object{
        @Volatile
        private var INSTANCE: GroceryDatabase? = null

        fun getInstance(context: Context): GroceryDatabase{
            if (INSTANCE == null)
            {
                synchronized(GroceryDatabase::class)
                {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        GroceryDatabase::class.java,
                        "grocery_db"
                    ).build()
                }
            }
            return INSTANCE!!
        }
    }
}