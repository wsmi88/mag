package com.smiatek.myapplication.activities

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.Api
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.smiatek.myapplication.MyApp
import com.smiatek.myapplication.R
import com.smiatek.myapplication.adapters.ScanResultAdapter
import com.smiatek.myapplication.api.*
import com.smiatek.myapplication.api.WeatherClient.Companion.getClient
import com.smiatek.myapplication.db.RouteCoordinate
import com.smiatek.myapplication.db.RouteCoordinateDAO
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    //for bluetooth connection
    // Initializes Bluetooth adapter.
//    val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
//    val bluetoothAdapter = bluetoothManager.adapter
//    var bluetoothGatt: BluetoothGatt? = null

    private lateinit var mMap: GoogleMap
    private lateinit var service: ApiService
    private lateinit var serviceWheather: WeatherService
    private var currentPressure: Float = 0.0f
    var AppId = "43a9657b0d1e1d72375482bd34426d86"

    private lateinit var poly: Polyline
    private lateinit var startLocation: LatLng

    private lateinit var client: FusedLocationProviderClient

    private var timeStamp: Long = 0
    private var flag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        client = LocationServices.getFusedLocationProviderClient(this)

        service = ApiClient.getClient()
            .create(ApiService::class.java)

        // for current pressure at sea level in openwheather API
        serviceWheather = WeatherClient.getClient()
            .create(WeatherService::class.java)

    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        val toggle: ToggleButton = findViewById(R.id.toggleButton)
        var REQUEST_ENABLE_BT: Int = 2
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
//        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
//        }

        //
        mMap = googleMap
//        googleMap.isMyLocationEnabled = true
        //trackDeviceLocation()
        toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                mMap.clear()
                flag = true
                timeStamp = System.currentTimeMillis()
                trackDeviceLocation()
                //TEST - getting pressure from API
                serviceWheather.getCurrentWeatherData("39", "135", AppId)
                    .enqueue(object : Callback<WeatherResponse> {
                        override fun onResponse(
                            call: Call<WeatherResponse>?,
                            response: Response<WeatherResponse>?
                        ) {
                            val weatherResponse = response?.body()!!
                            currentPressure = weatherResponse.main!!.pressure
                            Log.d("wojtek current pressure", currentPressure.toString())
                        }

                        override fun onFailure(call: Call<WeatherResponse>?, t: Throwable?) {
//                            altitudeTv.text = "failure"
                        }
                    }) //END OF TEST

            } else {
                flag = false
                // SAVING ROUTE, CLOSING THREAD
            }
        }
    }

    private fun trackDeviceLocation() {
        try {
            client.lastLocation.addOnCompleteListener {
                startLocation = LatLng(it.result!!.latitude, it.result!!.longitude)
//                mMap.addMarker(MarkerOptions().position(startLocation).title("Marker in Wojtek"))

                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        startLocation,
                        16f
                    )
                )  // the desired zoom level, in the range of 2.0 to 21.0.
                // Values below this range are set to 2.0, and values above it are set to 21.0.
                // Increase the value to zoom in. Not all areas have tiles at the largest zoom levels.


                Handler().postDelayed({
                    client.lastLocation.addOnCompleteListener {
                        val newPosition = LatLng(it.result!!.latitude, it.result!!.longitude)
                        poly =
                            mMap.addPolyline(PolylineOptions().add(startLocation).add(newPosition))
                        startLocation = newPosition
                        if (flag) {
                            trackDeviceLocation()
                        }
                        Log.d("wojtek", "interval")

                        // GETTING THE ALTITUDE FROM ESP32
                        service.getData("http://192.168.1.1")
                            .enqueue(object : Callback<List<Double>> {
                                override fun onResponse(
                                    call: Call<List<Double>>?,
                                    response: Response<List<Double>>?
                                ) {
//                                    altitudeTv.text = currentPressure.toString()
                                    //response?.body()!!.last().toString() + " m a.s.l."

                                    // SENDING DARA TO DB
                                    GlobalScope.async {
                                        MyApp.getDatabase()?.routeCoordinateDAO()
                                            ?.insertRouteCoordinate(
                                                RouteCoordinate(
                                                    it.result!!.latitude,
                                                    it.result!!.longitude,
                                                    response?.body()!!.last().toDouble(),
                                                    System.currentTimeMillis(),
                                                    timeStamp
                                                )
                                            )
                                    }

                                }


                                override fun onFailure(call: Call<List<Double>>?, t: Throwable?) {

                                }
                            })
                    }
                }, 10000)
            }

        } catch (e: SecurityException) {

        }
    }
}
