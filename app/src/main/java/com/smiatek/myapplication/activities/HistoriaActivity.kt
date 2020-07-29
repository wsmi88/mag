package com.smiatek.myapplication.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smiatek.myapplication.MyApp
import com.smiatek.myapplication.R
import com.smiatek.myapplication.adapters.HistoryRecyclerAdapter
import com.smiatek.myapplication.db.Route
import com.smiatek.myapplication.db.RouteCoordinate
import com.smiatek.myapplication.db.RouteCoordinateDAO
import kotlinx.coroutines.*
import java.io.Serializable

class HistoriaActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: HistoryRecyclerAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    lateinit var sortedCoordinates: ArrayList<RouteCoordinate>
    lateinit var sortedRoutes: ArrayList<Route>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historia)
        recyclerView = findViewById(R.id.history_recycler)
        viewManager = LinearLayoutManager(this)

        CoroutineScope(Dispatchers.IO).launch {
            var list = MyApp.getDatabase()?.routeCoordinateDAO()?.getRouteCoordinates()

            withContext(Dispatchers.Main) {
                viewAdapter =
                    HistoryRecyclerAdapter(sortGlobalList(list!!)) {
                        var i = Intent(this@HistoriaActivity, DetailActivity::class.java)
                        i.putExtra("route_data", it as Serializable)
                        startActivity(i)
                    }
                recyclerView.layoutManager = viewManager
                recyclerView.adapter = viewAdapter
            }
        }
    }

    private fun sortGlobalList(
        coorindatesList: List<RouteCoordinate>
    ): MutableList<Route> {
        val positionList = mutableListOf<Long>()
        val routeList = mutableListOf<Route>()

        coorindatesList.forEach { coordinate ->
            if ((positionList.any { it == coordinate.time_stamp }))
                routeList.find { it.listRouteCoordinate[0].time_stamp == coordinate.time_stamp }?.listRouteCoordinate?.add(
                    coordinate
                )
            else
                routeList.add(Route(mutableListOf(coordinate), coordinate.time_stamp))
            positionList.add(coordinate.time_stamp)
        }

        return routeList
    }
}
