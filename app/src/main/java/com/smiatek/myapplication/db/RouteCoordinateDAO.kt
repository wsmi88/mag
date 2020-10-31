package com.smiatek.myapplication.db

import androidx.room.*
import retrofit2.http.DELETE

@Dao

interface RouteCoordinateDAO {
    @Insert
    fun insertRouteCoordinate(routeCoordinate: RouteCoordinate)


    @Query("SELECT * FROM route_table")
    fun getRouteCoordinates(): List<RouteCoordinate>

    @Delete
    fun deleteRouteCoordinate(routeCoordinate: RouteCoordinate)
}