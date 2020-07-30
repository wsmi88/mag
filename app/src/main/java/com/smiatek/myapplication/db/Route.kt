package com.smiatek.myapplication.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

data class Route(
    val listRouteCoordinate: MutableList<RouteCoordinate>,
    val timeStamp: Long?
) : Serializable
