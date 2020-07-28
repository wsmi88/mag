package com.smiatek.myapplication.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(RouteCoordinate::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routeCoordinateDAO(): RouteCoordinateDAO

}