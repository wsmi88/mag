package com.smiatek.myapplication.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao

interface RouteCoordinateDAO {
    //    @Insert
//    fun insertRouteCoordinate(routeCoordinate: RouteCoordinate)
    @Insert
    fun insertRoute(route: Route)

    @Query("SELECT * FROM route_table")
    fun getRouteCoordinates(): List<Route>

}