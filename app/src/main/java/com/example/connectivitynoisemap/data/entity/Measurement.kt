package com.example.connectivitynoisemap.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.connectivitynoisemap.data.type.DataType
import com.example.connectivitynoisemap.main.utils.MapUtils.Companion.mgrsToLatLng
import com.google.android.gms.maps.model.LatLng
import mil.nga.mgrs.MGRS

@Entity(tableName = "measurement_table")
data class Measurement(
    val dataType: DataType,
    val neCorner: String,
    val nwCorner: String,
    val swCorner: String,
    val seCorner: String,
    val value: Double,
    val processed: Boolean = false,
    val timestamp: Long,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)

data class CornersAvgValue(
    val neCorner: String,
    val nwCorner: String,
    val swCorner: String,
    val seCorner: String,
    val avgValue: Double
){
    fun toCornersLatLngMap(): Map<String, LatLng> {
        return mapOf(
            "ne" to mgrsStringToLatLng(neCorner),
            "nw" to mgrsStringToLatLng(nwCorner),
            "sw" to mgrsStringToLatLng(swCorner),
            "se" to mgrsStringToLatLng(seCorner)
        )
    }

    private fun mgrsStringToLatLng(mgrsString: String): LatLng{
        val mgrs = MGRS.parse(mgrsString)
        return mgrsToLatLng(mgrs)
    }
}

data class CornersCount(
    val neCorner: String,
    val nwCorner: String,
    val swCorner: String,
    val seCorner: String,
    val count: Int
)