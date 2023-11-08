package com.example.connectivitynoisemap.main.interfaces

import com.example.connectivitynoisemap.main.MainActivity

interface MeasurementFragmentInterface {
    val activity: MainActivity
    val fId: Int

    fun locationPermission()
    fun onLocationPermGranted()
    fun measureValue()
}
