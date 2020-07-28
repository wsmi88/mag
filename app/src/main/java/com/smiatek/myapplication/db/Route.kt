package com.smiatek.myapplication.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

data class Route(
    val listRouteCoordinate: List<RouteCoordinate>? = null,
    val timeStamp: Long?
)