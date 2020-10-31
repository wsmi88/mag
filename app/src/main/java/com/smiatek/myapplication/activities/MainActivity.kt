package com.smiatek.myapplication.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.*
import android.bluetooth.BluetoothDevice.TRANSPORT_LE
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
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
import com.smiatek.myapplication.db.RouteCoordinate
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Math.pow
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val ENABLE_BLUETOOTH_REQUEST_CODE = 1
    private val LOCATION_PERMISSION_REQUEST_CODE = 2
    private val MY_SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
    private val MY_CHARACTERISTIC_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")
    private val CLIENT_CHARACTERISTIC_CONFIG_UUID =
        UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")

    private lateinit var mMap: GoogleMap
    private var flag = false
    private lateinit var client: FusedLocationProviderClient
    private lateinit var service: ApiService
    private lateinit var serviceWheather: WeatherService
    private var currentPressure = 0.0f
    var AppId = "43a9657b0d1e1d72375482bd34426d86"

    private lateinit var poly: Polyline
    private lateinit var startLocation: LatLng
    private lateinit var newLocation: LatLng
    private var timeStamp: Long = 0

    private var pressureData = 0.0f
    private var currentLat = 0.0
    private var currentLng = 0.0

    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val scan_button: Button = findViewById<Button>(R.id.scan_button)
        // blokada wygaszenia ekranu
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val powerManager: PowerManager = getSystemService(POWER_SERVICE) as PowerManager
        val wakeLock: PowerManager.WakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MyWakelockTag"
        )
        wakeLock.acquire()
        //
        scan_button.setOnClickListener {
            if (isScanning) {
                stopBleScan()
            } else {
                startBleScan()
            }
        }
        setupRecyclerView()
        setMap()
    }

    private fun setMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onResume() {
        super.onResume()
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }

    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) {
                    promptEnableBluetooth()
                }
            }
        }
    }

    val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun startBleScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            requestLocationPermission()
        } else {
            scanResults.clear()
            scanResultAdapter.notifyDataSetChanged()
            bleScanner.startScan(null, scanSettings, scanCallback)
            isScanning = true
        }
    }

    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }

    private fun requestLocationPermission() {
        if (isLocationPermissionGranted) {
            return
        }
        runOnUiThread {
            val alert = AlertDialog.Builder(this)
            alert.setTitle("Location permission required")
            alert.setMessage(
                "Starting from Android M (6.0), the system requires apps to be granted " +
                        "location access in order to scan for BLE devices."
            )
            alert.setCancelable(false)
            alert.setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, i: Int ->
                requestPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
            alert.show()
        }
    }

    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermission()
                } else {
                    startBleScan()
                }
            }
        }

    }

    //   @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    val filter = ScanFilter.Builder().setServiceUuid(ParcelUuid(MY_SERVICE_UUID)).build()


    private var isScanning = false
        set(value) {
            field = value
            runOnUiThread { scan_button.text = if (value) "Stop Scan" else "Start Scan" }
        }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) { // A scan result already exists with the same address
                scanResults[indexQuery] = result
                scanResultAdapter.notifyItemChanged(indexQuery)
            } else {
                with(result.device) {
                    Log.i(
                        "ScanCallback",
                        "Found BLE device! Name: ${name ?: "Unnamed"}, address: $address"
                    )
                }
                scanResults.add(result)
                scanResultAdapter.notifyItemInserted(scanResults.size - 1)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("ScanCallback", "onScanFailed: code $errorCode")
        }
    }

    private fun setupRecyclerView() {
        scan_results_recycler_view.apply {
            adapter = scanResultAdapter
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }

        val animator = scan_results_recycler_view.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    private val scanResults = mutableListOf<ScanResult>()
    private val scanResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter(scanResults) { result ->
            if (mapContainer.visibility == View.GONE) {
                scanContainer.visibility = View.GONE
                mapContainer.visibility = View.VISIBLE
                scan_button.visibility = View.GONE
            }
            // User tapped on a scan result
            //nowe jak sie polaczy to sie nie znajdzie juz

            val device = bluetoothAdapter.getRemoteDevice("3C:71:BF:9E:2C:96")
            val gatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                device.connectGatt(applicationContext, true, gattCallback, TRANSPORT_LE)
            } else {
                TODO("VERSION.SDK_INT < M")
            }
            // koniec nowego
            if (isScanning) {
                stopBleScan()
            }
            with(result.device) {
                Log.w("ScanResultAdapter", "Connecting to $address")
                //CONTEXT Z PIZDY
                //      connectGatt(this@MainActivity, false, gattCallback)

            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address
            val bluetoothGatt = gatt
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    Handler(Looper.getMainLooper()).post {
                        bluetoothGatt?.discoverServices()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w(
                        "BluetoothGattCallback",
                        "Successfully disconnected from $deviceAddress"
                    )
                    bluetoothGatt.disconnect()
                    gatt.close()
                }
            } else {
                Log.w(
                    "BluetoothGattCallback",
                    "Error $status encountered for $deviceAddress! Disconnecting..."
                )
                bluetoothGatt.disconnect()
                gatt.close()
            }

        }


        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            //nowe
            val characteristic = gatt.getService(MY_SERVICE_UUID)
                .getCharacteristic(MY_CHARACTERISTIC_UUID)
            gatt.setCharacteristicNotification(characteristic, true)
            val descriptor: BluetoothGattDescriptor = characteristic.getDescriptor(
                CLIENT_CHARACTERISTIC_CONFIG_UUID
            )
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            gatt.writeDescriptor(descriptor)
            //koniec

            with(gatt) {
                Log.w(
                    "BluetoothGattCallback",
                    "Discovered ${services.size} services for ${device.address}"
                )
                printGattTable() // See implementation just above this section
                // Consider connection setup as complete here
            }
        }

        //nowe
        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            val characteristic = gatt?.getService(MY_SERVICE_UUID)?.getCharacteristic(
                MY_CHARACTERISTIC_UUID
            )
            if (characteristic != null) {
                characteristic.value = byteArrayOf(1, 1)
            }
            gatt?.writeCharacteristic(characteristic)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            with(characteristic) {

                var pressure = 0.0f
                characteristic?.value?.toHexString()?.let {
                    var buffer = hexToAscii(it)
                    pressure = buffer?.toFloat()!!
                }
                pressureData = pressure

                Log.i(
                    "BluetoothGattCallback",
                    "Characteristic ${this?.uuid} changed | value: ${this?.value?.toHexString()} | pressure: $pressure | altitude: ${
                        readAltitude(
                            1013.25,
                            pressure.toDouble()
                        )
                    }"
                )
            }
        }


        private fun BluetoothGatt.printGattTable() {
            if (services.isEmpty()) {
                Log.i(
                    "printGattTable",
                    "No service and characteristic available, call discoverServices() first?"
                )
                return
            }
            services.forEach { service ->
                //val characteristic = service.getCharacteristic(MY_CHARACTERISTIC_UUID)
                val characteristicsTable = service.characteristics.joinToString(
                    separator = "\n|--",
                    prefix = "|--"
                ) { it.uuid.toString() }
                Log.i(
                    "printGattTable",
                    "\nService ${service.uuid}\nCharacteristics:\n$characteristicsTable" //, value: ${readCharacteristic(characteristic)}
                )
            }
        }
    }

    private fun ByteArray.toHexString(): String =
        joinToString(separator = "", prefix = "") { String.format("%02X", it) }

    private fun hexToAscii(hexStr: String): String? {
        val output = StringBuilder("")
        var i = 0
        while (i < hexStr.length) {
            val str = hexStr.substring(i, i + 2)
            output.append(str.toInt(16).toChar())
            i += 2
        }
        return output.toString()
    }

    private fun readAltitude(p0: Double, p: Double): Double {
        return BigDecimal(44330 * (1 - pow((p / p0), (1 / 5.255)))).setScale(
            2,
            RoundingMode.HALF_EVEN
        ).toDouble()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        val toggle: ToggleButton = findViewById(R.id.toggleButton)
        client = LocationServices.getFusedLocationProviderClient(this)

        service = ApiClient.getClient()
            .create(ApiService::class.java)

        // for current pressure at sea level in openwheather API
        serviceWheather = WeatherClient.getClient()
            .create(WeatherService::class.java)
        var REQUEST_ENABLE_BT: Int = 2
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
//        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
//        }

        //
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        googleMap.isMyLocationEnabled = true
        //nowe do sprawdzenia cisnienia w punkcie startowym
        client.lastLocation.addOnCompleteListener {
            startLocation = LatLng(it.result!!.latitude, it.result!!.longitude)
            currentLat = startLocation.latitude
            currentLng = startLocation.longitude
        }


        toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Notification ID cannot be 0.
                // startForegroundService(1, notification)
                mMap.clear()
                flag = true
                timeStamp = System.currentTimeMillis()

                trackDeviceLocation()
//                currentPressure = getCurrentPressure(currentLat.toString(), currentLng.toString())

                serviceWheather.getCurrentWeatherData(
                    currentLat.toString(),
                    currentLng.toString(),
                    AppId
                )
                    .enqueue(object : Callback<WeatherResponse> {
                        override fun onResponse(
                            call: Call<WeatherResponse>?,
                            response: Response<WeatherResponse>?
                        ) {
                            val weatherResponse = response?.body()!!
                            currentPressure = weatherResponse.main!!.pressure
                            Log.d(
                                "wojtek current pressure",
                                "$currentPressure lat: ${currentLat}, lng: $currentLng"
                            )
                        }

                        override fun onFailure(call: Call<WeatherResponse>?, t: Throwable?) {
                            altitudeTv.text = "failure"
                        }
                    })

            } else {
                flag = false
                //stopForeground()
                // SAVING ROUTE, CLOSING THREAD
            }
        }
    }
//        private fun getCurrentPressure(latitude:String, longitude:String): Float {
//            serviceWheather = WeatherClient.getClient()
//                .create(WeatherService::class.java)
//            var currentPressure2 = 0.0f
//            serviceWheather.getCurrentWeatherData(latitude, longitude, AppId)
//                .enqueue(object : Callback<WeatherResponse> {
//                    override fun onResponse(
//                        call: Call<WeatherResponse>?,
//                        response: Response<WeatherResponse>?
//                    ) {
//                        val weatherResponse = response?.body()!!
//                        currentPressure2 = weatherResponse.main!!.pressure
//                        Log.d("wojtek current pressure", "$currentPressure lat: $latitude, lng: $longitude")
//                    }
//
//                    override fun onFailure(call: Call<WeatherResponse>?, t: Throwable?) {
//                        altitudeTv.text = "failure"
//                    }
//                })
//            return currentPressure2
//        }

    private fun trackDeviceLocation() {
        try {
            client.lastLocation.addOnCompleteListener {
                startLocation = LatLng(it.result!!.latitude, it.result!!.longitude)
                //mMap.addMarker(MarkerOptions().position(startLocation).title("Marker in Wojtek"))

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
                        var newPosition = LatLng(it.result!!.latitude, it.result!!.longitude)

//                            currentPressure = getCurrentPressure(currentLat.toString(), currentLng.toString())

                        poly =
                            mMap.addPolyline(
                                PolylineOptions().add((startLocation), (newPosition))
                            )

                        startLocation = newPosition
                        if (flag) {
                            trackDeviceLocation()
                        }
                        Log.d("wojtek", "interval")
                        Log.d("wojtek current pressure on rut", currentPressure.toString())

                        altitudeTv.text = readAltitude(
                            currentPressure.toDouble(),
                            pressureData.toDouble()
                        ).toString() + " m a.s.l."
                        // SENDING DATA TO DB
                        GlobalScope.async {
                            MyApp.getDatabase()?.routeCoordinateDAO()
                                ?.insertRouteCoordinate(
                                    RouteCoordinate(
                                        it.result!!.latitude,
                                        it.result!!.longitude,
                                        readAltitude(
                                            currentPressure.toDouble(),
                                            pressureData.toDouble()
                                        ),
                                        System.currentTimeMillis(),
                                        timeStamp
                                    )
                                )
                        }
                    }
                }, 10000)
            }

        } catch (e: SecurityException) {

        }
    }

}
