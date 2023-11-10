package com.example.connectivitynoisemap.main.interfaces

import com.example.connectivitynoisemap.data.type.DataType
import com.example.connectivitynoisemap.main.MainActivity

interface MeasurementFragmentInterface {
    val activity: MainActivity
    val dataType: DataType

    fun locationPermission()
    fun onLocationPermGranted()
    fun measureValue()
}
