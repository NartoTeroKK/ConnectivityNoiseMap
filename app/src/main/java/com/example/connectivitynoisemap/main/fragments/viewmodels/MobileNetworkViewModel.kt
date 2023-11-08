@file:Suppress("DEPRECATION")

package com.example.connectivitynoisemap.main.fragments.viewmodels

import android.content.Context
import android.telephony.CellSignalStrengthLte
import android.telephony.CellSignalStrengthWcdma
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import androidx.lifecycle.ViewModel

@Suppress("DEPRECATION")
class MobileNetworkViewModel : ViewModel() {

    private lateinit var mTelephonyManager: TelephonyManager
    private lateinit var mPhoneStateListener: MyPhoneStateListener

    private val isMobileDataEnabled: Boolean
        get() = mTelephonyManager.dataState == TelephonyManager.DATA_CONNECTED

    val mobileRssi: Double
        get() = getMobileRsii().toDouble()

    fun lateInit(context: Context) {
        mTelephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        mPhoneStateListener = MyPhoneStateListener(context)
    }

    private inner class MyPhoneStateListener(context: Context) : PhoneStateListener() {
        private var signalStrength: Int = 0
        val telephonyManager: TelephonyManager

        init{
            telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            telephonyManager.listen(this, LISTEN_SIGNAL_STRENGTHS)
        }

        fun getSignalStrength(): Int {
            return signalStrength
        }

        @Deprecated("Deprecated in Java")
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
            super.onSignalStrengthsChanged(signalStrength)

            val signal = signalStrength.cellSignalStrengths[0]
            when (signal.javaClass) {
                CellSignalStrengthLte::class.java -> {
                    val signalLte =
                        signal as CellSignalStrengthLte
                    this.signalStrength = signalLte.rssi
                }
                CellSignalStrengthWcdma::class.java -> {
                    val signalWcdma =
                        signal as CellSignalStrengthWcdma
                    this.signalStrength = signalWcdma.dbm
                }
                else -> this.signalStrength = 0
            }
        }

        fun removePhoneStateListener() =
            telephonyManager.listen(this, LISTEN_NONE)


    }

    private fun getMobileRsii(): Int {
        return if (isMobileDataEnabled)
            this.mPhoneStateListener.getSignalStrength()
        else
            1
    }

    fun removeListener() =
        mPhoneStateListener.removePhoneStateListener()

    ////////////////////////

}
