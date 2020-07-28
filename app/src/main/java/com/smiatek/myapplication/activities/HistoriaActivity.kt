package com.smiatek.myapplication.activities

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
                viewAdapter = HistoryRecyclerAdapter(sortGlobalList(createRouteList(list!!), list))
                recyclerView.layoutManager = viewManager
                recyclerView.adapter = viewAdapter
            }
        }
    }

    private fun sortGlobalList(
        routeList: ArrayList<Route>,
        coorindatesList: List<RouteCoordinate>
    ): ArrayList<Route> {

        for (i in 0..routeList.size) {
            val map = coorindatesList.associateBy({ i }, { it.time_stamp }).toMap()
        }

        sortedRoutes = ArrayList()
        for (i in 0..routeList.size) {
            sortedCoordinates = ArrayList()
            var timestamp: Long? = 0
            Log.d("wojtek", "route $i")
            for (j in 0..coorindatesList.size) {

                try {
                    var cor1: Long = coorindatesList[j].time_stamp!!
                    var cor2: Long = coorindatesList[j + 1].time_stamp!!
                    if (cor1 == cor2) {
                        sortedCoordinates.add(coorindatesList[j])
                        timestamp = coorindatesList[j].time_stamp
                    }
                } catch (e: Exception) {
                }

            }
            sortedRoutes.add(Route(sortedCoordinates, timestamp))
        }
        return sortedRoutes
    }

    private fun createRouteList(list: List<RouteCoordinate>): ArrayList<Route> {
        var routes = ArrayList<Route>()
        for (i in 0..list.size) {
            try {
                if (list[i].time_stamp != list[i + 1].time_stamp) {
                    routes.add(Route(list, 0))
                }
            } catch (e: Exception) {

            }
        }

        return routes
    }
}
