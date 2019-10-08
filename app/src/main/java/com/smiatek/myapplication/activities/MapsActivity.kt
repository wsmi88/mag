package com.smiatek.myapplication.activities

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.smiatek.myapplication.R
import com.smiatek.myapplication.api.ApiClient
import com.smiatek.myapplication.api.ApiService
import kotlinx.android.synthetic.main.activity_maps.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var service: ApiService

    private lateinit var poly: Polyline
    private lateinit var startLocation: LatLng

    private lateinit var client: FusedLocationProviderClient

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
        mMap = googleMap
        googleMap.isMyLocationEnabled = true
        trackDeviceLocation()

    }

    private fun trackDeviceLocation() {
        try {
            client.lastLocation.addOnCompleteListener {
                startLocation = LatLng(it.result!!.latitude, it.result!!.longitude)
                mMap.addMarker(MarkerOptions().position(startLocation).title("Marker in Wojtek"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 13f))

                Handler().postDelayed({
                    client.lastLocation.addOnCompleteListener {
                        val newPosition = LatLng(it.result!!.latitude, it.result!!.longitude)
                        poly =
                            mMap.addPolyline(PolylineOptions().add(startLocation).add(newPosition))
                        startLocation = newPosition
                        trackDeviceLocation()
                        Log.d("tomek", "interval")
                        service.getData("http://192.168.1.1")
                            .enqueue(object : Callback<List<Double>> {
                                override fun onResponse(
                                    call: Call<List<Double>>?,
                                    response: Response<List<Double>>?
                                ) {
                                    altitudeTv.text =
                                        response?.body()!!.last().toString() + " m n.p.m."
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
