package com.smiatek.myapplication.activities

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
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
    private val FILL_VERTICAL = 112

    private val STATE_DISCONNECTED = 0
    private val STATE_CONNECTING = 1
    private val STATE_CONNECTED = 2
    val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
    val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
    val ACTION_GATT_SERVICES_DISCOVERED =
        "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
    val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
    val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        //data from db
        route = intent.getSerializableExtra("route_data") as Route

        //data to TEXTVIEW
        val dayFormat = SimpleDateFormat("dd MMMM YYYY")
        val startTimeFormat = SimpleDateFormat("HH:mm")
        data_tv.text =
            "${dayFormat.format(route.timeStamp)} \n" +
                    "Start time: ${startTimeFormat.format(route.timeStamp)} \n" +
                    "Duration time: ${convertSecondsToHHMMSS(route.listRouteCoordinate.last().timediff)}"
        //data_tv.textSize = 20sp
        data_tv.gravity = FILL_VERTICAL

        //data for altitude chart
        val altChart = ArrayList<Entry>()
        route.listRouteCoordinate.forEach {
            altChart.add(Entry(it.timediff?.toFloat()!!, it.altitude?.toFloat()!!))
        }

        val vl = LineDataSet(altChart, "Altitude m a.s.l.")
        vl.setDrawValues(false) // true if you want to display value of all points
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

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 10)
        } else {
            scanBLEDevices(scanCallback)
        }


        lineChart.data = LineData(vl)
        lineChart.xAxis.labelRotationAngle = 0f
        lineChart.data = LineData(vl)
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.description.text = "Time HH:MM:SS"
        val markerView = CustomMarker(this@DetailActivity, R.layout.marker_view)
        lineChart.marker = markerView

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 10) {
            scanBLEDevices(scanCallback)
        }
    }

    fun scanBLEDevices(scanCallback: ScanCallback) {
        val bluetoothScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
        var scanning = false
        val handler = Handler()

        if (!scanning) {
            handler.postDelayed({
                scanning = false
                bluetoothScanner.stopScan(scanCallback)
            }, 1000)
            scanning = true
            bluetoothScanner.startScan(scanCallback)
        } else {
            scanning = false
            bluetoothScanner.stopScan(scanCallback)
        }
    }

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

//            if(result?.device?.uuids?.equals("TX Characteristic")){
            result?.device?.connectGatt(applicationContext, true, object : BluetoothGattCallback() {
                override fun onConnectionStateChange(
                    gatt: BluetoothGatt?,
                    status: Int,
                    newState: Int
                ) {
                    sendBroadcast(Intent(ACTION_DATA_AVAILABLE))
                }
            })
//            }
        }
    }

    class GattReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            //    Toast.makeText(context, "Action: " + intent?.action(), Toast.LENGTH_SHORT).show()
            Log.d("wojtek", " weszlo")

        }
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
                .clickable(false) // do przemyslenia czy cos wyswietlac na lini - czas/wysokosc?
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