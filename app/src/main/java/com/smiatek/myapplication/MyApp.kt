package com.smiatek.myapplication

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import com.smiatek.myapplication.db.AppDatabase


class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        roomDataBase = createDatabase()
    }

    fun createDatabase() =
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "route-database"
        ).fallbackToDestructiveMigration().build()

    companion object {
        private var roomDataBase: AppDatabase? = null
        fun getDatabase() = roomDataBase
    }
}