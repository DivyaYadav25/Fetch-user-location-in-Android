package com.example.telyport

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.Secure
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.example.telyport.databinding.ActivityMain2Binding
import com.google.android.gms.location.*
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*


class MainActivity2 : AppCompatActivity(),EasyPermissions.PermissionListener, LocationFound  {
    private val TAG = MainActivity2::class.java.simpleName
    private var isGranted: Boolean = true
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mFusedLocationClient1: FusedLocationProviderClient? = null
    var latitude : Double? = null
    var longitude : Double? = null
    var lat: String? = null
    var log: String? = null
    lateinit var locationFound: LocationFound
    lateinit var locationRequest: LocationRequest
    lateinit var locationRequest1: LocationRequest
    lateinit var locationManager: LocationManager
    private var locationCallback: LocationCallback? = null
    private var locationCallback1: LocationCallback? = null
    var mTrackingLocation = true

    private lateinit var binding: ActivityMain2Binding
    val db = Firebase.firestore
    private var android_id : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= DataBindingUtil.setContentView(this, R.layout.activity_main2)

        android_id =  Secure.getString(this.getContentResolver(), Secure.ANDROID_ID)
        setListener()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient1 = LocationServices.getFusedLocationProviderClient(this)
        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = (30 * 1000).toLong()
        locationRequest.fastestInterval = (30 * 1000).toLong()

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        locationRequest1 = LocationRequest.create()
        locationRequest1.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest1.smallestDisplacement = 200F
        locationRequest1.interval = (60 * 1000).toLong()
        locationRequest1.fastestInterval = (60 * 1000).toLong()

        val builder1 = LocationSettingsRequest.Builder().addLocationRequest(locationRequest1)
        builder1.setAlwaysShow(true)
    }

    private fun getInfo() {
        if (Utility.isNetworkAvailable()) {
            binding.progressbar.visibility = View.VISIBLE

            db.collection("location_table")
                    .document(android_id)
                    .collection("device_location_detail")
                .orderBy("lastUpdatedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->

                    if (documents.documents.isNotEmpty()){
                        binding.tvNumDataUpload.text ="No. of entries " +  (documents.size())

                        val inputFormat = SimpleDateFormat(
                                "yyyy-MM-dd'T'hh:mm:ss'Z'",
                                Locale.getDefault()
                        )
                        val outputFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())

                        if (documents.documents.get(0).get("lastUpdatedAt").toString() != ""){
                            val  outputDate : Date = inputFormat.parse(documents.documents.get(0).get(
                                    "lastUpdatedAt").toString())!!
                            val date = outputFormat.format(outputDate)
                            binding.tvLastUpdate.text = "Database Updated at : " + date
                        }

                    }

                    binding.progressbar.visibility = View.GONE

                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents.", exception)
                    binding.progressbar.visibility = View.GONE
                }


        } else {
            Toast.makeText(this, "no_internet_connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setListener() {
        if (latitude==null && longitude==null) {
            setLocationListener(this)
        }
    }
    private fun setLocationListener(locationFound: LocationFound) {
        this.locationFound = locationFound
        isPermissionEnable()
    }

    private fun isPermissionEnable() {
        val locationList = java.util.ArrayList<String>()
        locationList.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            locationList.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        locationList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        EasyPermissions.checkAndRequestPermission(this, locationList, this@MainActivity2)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(
            requestCode,
            permissions as Array<String>,
            grantResults
        )
    }

    private fun getLocation() {
        if (isGranted) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                    isPermissionEnable()
                    return
                }

                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        if (locationResult == null) {
                            return
                        }
                        for (location in locationResult.locations) {
                            if (location != null) {
                                latitude = location?.latitude
                                longitude = location?.longitude

                                lat = location.latitude.toString()
                                log = location.longitude.toString()
                                locationFound.locationFound(lat!!, log!!)

                            }
                        }
                    }
                }

                locationCallback1 = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        if (locationResult == null) {
                            return
                        }
                        for (location in locationResult.locations) {
                            if (location != null) {
                                latitude = location?.latitude
                                longitude = location?.longitude

                                lat = location.latitude.toString()
                                log = location.longitude.toString()
                                locationFound.locationFound(lat!!, log!!)

                            }
                        }
                    }
                }
                mFusedLocationClient!!.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
                )
                mFusedLocationClient1!!.requestLocationUpdates(
                    locationRequest1,
                    locationCallback1,
                    null
                )
            } else {
                 Toast.makeText(this, "turn_on_location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            isPermissionEnable()
        }
    }

    override fun onPermissionGranted(mCustomPermission: List<String>?) {
        isGranted = true
    }

    override fun onPermissionDenied(mCustomPermission: List<String>?) {
        isGranted = false
    }
    override fun locationFound(lat: String, log: String) {
        val time = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'", Locale.getDefault()).format(System.currentTimeMillis())
        val location = Location(longitude, latitude, time)
        addInfo(android_id,location)

        Log.d(TAG, "Lat ${lat} or long ${log}")
    }


    private fun addInfo(androidId: String, location: Location) {
        binding.progressbar.visibility = View.VISIBLE
        if (Utility.isNetworkAvailable()) {
            db.collection("location_table").get().addOnSuccessListener {
                    result ->

                binding.progressbar.visibility = View.VISIBLE

                db.collection("location_table")
                        .document(androidId)
                        .collection("device_location_detail")
                        .add(location).addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully written!")
                    binding.progressbar.visibility = View.GONE
                    getInfo()
                }.addOnFailureListener {
                    binding.progressbar.visibility = View.GONE
                    Toast.makeText(this, "something_went_wrong", Toast.LENGTH_LONG).show()
                }

            }.addOnFailureListener{
                binding.progressbar.visibility = View.GONE
                Toast.makeText(this, "something_went_wrong", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "no_internet_connection", Toast.LENGTH_SHORT).show()
        }
    }

    override fun locationNotFound(string: String) {
        Toast.makeText(this, "Location Not Found", Toast.LENGTH_LONG).show()
    }

    override fun onStop() {
        super.onStop()
        if (mTrackingLocation) {
            stopTrackingLocation()
            mTrackingLocation = true
        }
    }

    override fun onResume() {
        if (mTrackingLocation) {
            getInfo()
            getLocation()
        }
        super.onResume()
    }


    private fun stopTrackingLocation() {
        mFusedLocationClient?.removeLocationUpdates(locationCallback)
        mTrackingLocation = !mTrackingLocation
    }
}

interface LocationFound {
    fun locationFound(lat: String, log: String)
    fun locationNotFound(string: String)
}