package com.smiatek.myapplication.activities

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.smiatek.myapplication.MyReceiver
import com.smiatek.myapplication.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_menu.*


class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
//        Log.d("wojtek", "${intent.extras.getFloat("data")}")

        val filter = IntentFilter()
        filter.addAction("com.journaldev.CUSTOM_INTENT")

        val myReceiver = MyReceiver()
        registerReceiver(myReceiver, filter)

        if (ContextCompat.checkSelfPermission(
                this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {

        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                10
            )
        }

        trackerBtn.setOnClickListener {
            startActivity(Intent(this@MenuActivity, MainActivity::class.java))
        }

        historiaBtn.setOnClickListener {
            startActivity(Intent(this@MenuActivity, HistoryActivity::class.java))
        }

        var intent = Intent()
        intent.setAction("com.journaldev.CUSTOM_INTENT")
        sendBroadcast(intent)
    }
}


