package com.smiatek.myapplication.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "route_table")
data class RouteCoordinate(
    @ColumnInfo(name = "latitude") val latitude: Double? = 12.0,
    @ColumnInfo(name = "longitude") val longitude: Double? = 12.0,
    @ColumnInfo(name = "altitude") val altitude: Double? = 100.0,
    @ColumnInfo(name = "time") val time: Long?,
    @ColumnInfo(name = "time_stamp") val time_stamp: Long?

) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
}

