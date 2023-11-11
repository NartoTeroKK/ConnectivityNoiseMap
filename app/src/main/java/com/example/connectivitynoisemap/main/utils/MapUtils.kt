package com.example.connectivitynoisemap.main.utils

import com.google.android.gms.maps.model.LatLng
import mil.nga.mgrs.MGRS

class MapUtils {
    companion object{
        fun mgrsToLatLng(mgrs: MGRS) : LatLng {
            return LatLng(mgrs.toPoint().latitude, mgrs.toPoint().longitude)
        }

        fun latLngToMgrs(latLng: LatLng) : MGRS {
            return MGRS.from(latLng.longitude, latLng.latitude)
        }

        fun getGridSquare(
            mgrs: MGRS,
            meters: Long = 10
        ): Map<String, MGRS> {

            val easting: Long = mgrs.easting - (mgrs.easting % meters)
            val northing: Long = mgrs.northing - (mgrs.northing % meters)
            val northeast = MGRS(mgrs.zone, mgrs.band, mgrs.column, mgrs.row, easting + meters, northing + meters)
            val northwest = MGRS(mgrs.zone, mgrs.band, mgrs.column, mgrs.row, easting, northing + meters)
            val southeast = MGRS(mgrs.zone, mgrs.band, mgrs.column, mgrs.row,easting + meters, northing)
            val southwest = MGRS(mgrs.zone, mgrs.band, mgrs.column, mgrs.row, easting, northing)

            return mapOf(
                "ne" to northeast,
                "nw" to northwest,
                "sw" to southwest,
                "se" to southeast)
        }

        fun toGridSquareString(
            gridSquare: Map<String, MGRS>
        ): Map<String, String> {
            return mapOf(
                "ne" to gridSquare["ne"].toString(),
                "nw" to gridSquare["nw"].toString(),
                "sw" to gridSquare["sw"].toString(),
                "se" to gridSquare["se"].toString())
        }

        /*
        fun isCurrentGridSquare(
            gridSquare:Map<String, MGRS>,
            currentMgrs: MGRS,
            meters: Long = 10
        ): Boolean {
            infix fun Long.isBetweenInclusive(range: LongRange) = this in range

            val point = gridSquare["ne"]!!
            val mgrs = MGRS(
                point.zone,
                point.band,
                point.column,
                point.row,
                point.easting - (meters/2),
                point.northing - (meters/2)
            )

            return (mgrs.zone == currentMgrs.zone &&
                    mgrs.band == currentMgrs.band &&
                    mgrs.column == currentMgrs.column &&
                    mgrs.row == currentMgrs.row &&
                    mgrs.easting isBetweenInclusive
                    gridSquare["sw"]!!.easting..gridSquare["se"]!!.easting &&
                    mgrs.northing isBetweenInclusive
                    gridSquare["sw"]!!.northing..gridSquare["nw"]!!.northing)
        }

         */
    }
}