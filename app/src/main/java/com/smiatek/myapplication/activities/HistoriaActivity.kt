package com.smiatek.myapplication.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.smiatek.myapplication.MyApp
import com.smiatek.myapplication.R
import kotlinx.coroutines.*

class HistoriaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historia)

        GlobalScope.async {
//            Log.d("tomek", " "+MyApp.getDatabase()?.routeCoordinateDAO()?.getRouteCoordinates()?.size)
        }
    }
}
