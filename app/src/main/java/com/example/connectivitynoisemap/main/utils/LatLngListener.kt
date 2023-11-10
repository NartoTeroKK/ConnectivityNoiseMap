package com.example.connectivitynoisemap.main.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
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

    private val _currentLatLng = MutableLiveData<LatLng>()
    val currentLatLng: LiveData<LatLng> = _currentLatLng

    private val fusedLocationProviderClient: FusedLocationProviderClient
    by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }
    private val locationCallback : LocationCallback
    by lazy {
        object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    _currentLatLng.postValue(locationToLatLng(location))
                }
            }
        }
    }
    private val locationManager: LocationManager
    by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    val isLocationEnabled: Boolean
        get() = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

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
        locationRequest.interval = 500
        locationRequest.fastestInterval = 250
        locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
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
        this.fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

}
