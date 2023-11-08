package com.example.connectivitynoisemap.main.fragments.viewmodels

import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import androidx.lifecycle.ViewModel


@Suppress("DEPRECATION")
class WifiViewModel : ViewModel() {

    private lateinit var wifiManager: WifiManager

    val wifiRssi: Double
        get() = getWifiRssi().toDouble()

    fun lateInit(context: Context){
        wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    private fun getWifiRssi(): Int {
        if(wifiManager.isWifiEnabled){
            val wifiInfo: WifiInfo? = wifiManager.connectionInfo

            if (wifiInfo != null && wifiInfo.bssid != null) {
                return wifiInfo.rssi
            }
        }else{
            return 0
        }
        return +1
    }

}

