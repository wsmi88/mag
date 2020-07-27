package com.smiatek.myapplication.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Route::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routeCoordinateDAO(): RouteCoordinateDAO

}