package com.example.connectivitynoisemap.main.utils

import android.graphics.Color
import com.example.connectivitynoisemap.data.type.DataType
import com.example.connectivitynoisemap.main.enums.NoiseClass
import com.example.connectivitynoisemap.main.enums.SignalClass

class ValueClass {
    companion object{
        fun fromValueToClass(dataType: DataType, avgValue: Double): Enum<*> {
            val valueClass =
                when(dataType){
                    DataType.MOBILE_NETWORK -> getSignalClass(avgValue)
                    DataType.WIFI -> getSignalClass(avgValue)
                    DataType.NOISE -> getNoiseClass(avgValue)
                }
            return valueClass
        }

        fun fromClassToColor(valueClass: Enum<*>) : Int{
            val red = Color.argb(60,255, 0, 0)
            val orange = Color.argb(60,255, 128, 0)
            val yellow = Color.argb(60,255, 255, 0)
            val green = Color.argb(60,0, 255, 0)

            val color =
                when(valueClass){
                    is NoiseClass -> {
                        when(valueClass){
                            NoiseClass.LOW -> green
                            NoiseClass.MEDIUM -> yellow
                            NoiseClass.LOUD -> orange
                            NoiseClass.DANGEROUS -> red
                        }
                    }
                    is SignalClass -> {
                        when(valueClass){
                            SignalClass.WEAK -> red
                            SignalClass.FAIR -> orange
                            SignalClass.GOOD -> yellow
                            SignalClass.EXCELLENT -> green
                        }
                    }
                    else -> error("ERROR: fromClassToColor")
                }
            return color
        }

        private fun getSignalClass(value: Double): SignalClass {
            val signalClass = when {
                value < -70 || value == 0.0 -> SignalClass.WEAK
                value < -60 -> SignalClass.FAIR
                value < -50 -> SignalClass.GOOD
                else -> SignalClass.EXCELLENT
            }
            return signalClass
        }

        /***
        Source: https://www.audionovaitalia.it/blog/protezione-udito/suono-e-decibel/
         ***/
        private fun getNoiseClass(value : Double): NoiseClass {
            val noiseClass = when{
                value < 40 -> NoiseClass.LOW
                value < 60 -> NoiseClass.MEDIUM
                value < 85 -> NoiseClass.LOUD
                else -> NoiseClass.DANGEROUS
            }
            return noiseClass
        }
    }
}