package com.smiatek.myapplication.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.smiatek.myapplication.R
import com.smiatek.myapplication.db.Route
import kotlinx.android.synthetic.main.activity_detail.*
import java.text.SimpleDateFormat


class DetailActivity : AppCompatActivity(), GoogleMap.OnPolylineClickListener, OnMapReadyCallback {

    lateinit var route: Route

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

//        //data form db
        route = intent.getSerializableExtra("route_data") as Route
//
        //data for altitude chart
        //val dateFormat = SimpleDateFormat("HH:mm")
        val altChart = ArrayList<Entry>()
        route.listRouteCoordinate.forEach {
            altChart.add(Entry(it.timediff?.toFloat()!!, it.altitude?.toFloat()!!))
        }
        //TEST
//        val entries = ArrayList<Entry>()
//
////Part2
//        entries.add(Entry(1f, 10f))
//        entries.add(Entry(2f, 2f))
//        entries.add(Entry(3f, 7f))
//        entries.add(Entry(4f, 20f))
//        entries.add(Entry(5f, 16f))

        //TEST

        //Part3

        val vl = LineDataSet(altChart, "test")//altChart, "Altitude m a.s.l.")
        //Part4
        vl.setDrawValues(true)
        vl.setDrawFilled(true)
        vl.lineWidth = 3f

        var xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                var time: String = convertSecondsToHHMMSS(value.toLong())
                return time
            }
        }

        lineChart.data = LineData(vl)
//        vl.fillColor = R.color.gray
//        vl.fillAlpha = R.color.red
        //Part5
//        lineChart.xAxis.valueFormatter.getFormattedValue()
        lineChart.xAxis.labelRotationAngle = 0f


        //Part6
        lineChart.data = LineData(vl)
//
        //Part7
//        lineChart.axisRight.isEnabled = false
//        lineChart.xAxis.axisMaximum = j+0.1f
//
        //Part8
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
//
        //Part9
        lineChart.description.text = "Time"

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * In this tutorial, we add polylines and polygons to represent routes and areas on the map.
     */
    override fun onMapReady(googleMap: GoogleMap) {

        // Add polylines to the map.
        // Polylines are useful to show a route or some other connection between points.

        var coordList: MutableList<LatLng> = mutableListOf()
        var startPosition = LatLng(
            route.listRouteCoordinate[0].latitude!!,
            route.listRouteCoordinate[0].longitude!!
        )

        route.listRouteCoordinate.forEach {
            coordList.add(LatLng(it.latitude!!, it.longitude!!))
        }


        val polyline1 = googleMap.addPolyline(
            PolylineOptions()
                .clickable(true)
                .addAll(coordList)
        )

        // Position the map's camera is a start point of the route
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPosition, 16f))

        // Set listeners for click events.
        googleMap.setOnPolylineClickListener(this)
    }

    override fun onPolylineClick(p0: Polyline?) {
        TODO("Not yet implemented")
    }

    fun convertSecondsToHHMMSS(millis: Long): String {
        var seconds: Long = (millis / 1000) % 60
        var minutes: Long = (millis / (1000 * 60)) % 60
        var hours: Long = (millis / (1000 * 60 * 60))

        var convertedTime: StringBuilder = StringBuilder()
        convertedTime.append(if (hours == 0L) "00" else if (hours < 10) "0$hours" else hours.toString())
        convertedTime.append(":")
        convertedTime.append(if (minutes == 0L) "00" else if (minutes < 10) "0$minutes" else minutes.toString())
        convertedTime.append(":")
        convertedTime.append(if (seconds == 0L) "00" else if (seconds < 10) "0$seconds" else seconds.toString())
        return convertedTime.toString()
    }
}