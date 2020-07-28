package com.smiatek.myapplication.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smiatek.myapplication.MyApp
import com.smiatek.myapplication.R
import com.smiatek.myapplication.adapters.HistoryRecyclerAdapter
import kotlinx.coroutines.*

class HistoriaActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historia)

        viewManager = LinearLayoutManager(this)
        viewAdapter = HistoryRecyclerAdapter(myDataset)
        recyclerView = findViewById<RecyclerView>(R.id.history_recycler).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

            GlobalScope.async {
                Log.d(
                    "tomek",
                    " " + MyApp.getDatabase()?.routeCoordinateDAO()?.getRouteCoordinates()?.size
                )
            }
        }
    }
