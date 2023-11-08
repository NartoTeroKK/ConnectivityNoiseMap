package com.example.connectivitynoisemap.main.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng

@Suppress("DEPRECATION")
class LatLngListener constructor(
    context: Context,
    isLocationPermGranted: Boolean
) {

    companion object {
        private var instance: LatLngListener? = null

        fun getInstance(
            context: Context,
            isLocationPermGranted: Boolean
        ): LatLngListener {
            return instance ?: LatLngListener(context, isLocationPermGranted).also { instance = it }
        }
    }

    private val fusedLocationProviderClient: FusedLocationProviderClient
    by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }
    private val locationListener by lazy {
        LocationListener { location ->
            val latLng = locationToLatLng(location)
            _currentLatLng.postValue(latLng) // update the LiveData
            Log.d("LOCATION", "Location: $latLng")
        }
    }
    private val locationManager: LocationManager
    by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    val isLocationEnabled: Boolean
        get() = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    private val _currentLatLng = MutableLiveData<LatLng>()
    val currentLatLng: LiveData<LatLng>
        get() = _currentLatLng

    init {
        if (isLocationPermGranted) {
            onLocationPermGranted()
        }

    }

    fun onLocationPermGranted(){
        initLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun initLocationUpdates() {

        val locationRequest = LocationRequest()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 500
        locationRequest.smallestDisplacement = 1f
        locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationListener,
            Looper.getMainLooper()
        )
    }

    /*
    @SuppressLint("MissingPermission")
    fun getCurrentLatLng() {
        val location = fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,null)
        location.addOnSuccessListener{ loc ->
            if (loc != null ) {
                val latLng = locationToLatLng(loc)
                _currentLatLng.postValue(latLng) // update the LiveData
            }
        }
        location.addOnFailureListener { e ->
            Log.e("ERROR", e.message.toString())
            e.printStackTrace()
        }
    }
     */

    private fun locationToLatLng(location: Location): LatLng {
        return LatLng(location.latitude, location.longitude)
    }

    fun removeLocationUpdates(){
        this.fusedLocationProviderClient.removeLocationUpdates(locationListener)
    }

}
