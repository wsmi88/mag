package com.smiatek.myapplication.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "route_table")
data class Route(
    @ColumnInfo(name = "route") val listRouteCoordinate: List<RouteCoordinate>? = null
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
}