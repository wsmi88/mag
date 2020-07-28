package com.smiatek.myapplication.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smiatek.myapplication.MyApp
import com.smiatek.myapplication.R
import com.smiatek.myapplication.adapters.HistoryRecyclerAdapter
import com.smiatek.myapplication.db.RouteCoordinateDAO
import kotlinx.coroutines.*

class HistoriaActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: HistoryRecyclerAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historia)
        recyclerView = findViewById(R.id.history_recycler)
        viewManager = LinearLayoutManager(this)

        CoroutineScope(Dispatchers.IO).launch {
            var list = MyApp.getDatabase()?.routeCoordinateDAO()?.getRouteCoordinates()
            withContext(Dispatchers.Main) {
                viewAdapter = HistoryRecyclerAdapter(list!!)
                recyclerView.layoutManager = viewManager
                recyclerView.adapter = viewAdapter
            }
        }
    }
}
