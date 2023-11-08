package com.example.connectivitynoisemap.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.connectivitynoisemap.data.entity.CornersAvgValue
import com.example.connectivitynoisemap.data.entity.Measurement
import com.example.connectivitynoisemap.data.type.DataType
import com.example.connectivitynoisemap.main.module.implementation.MapHandler
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mil.nga.mgrs.MGRS

class MeasurementViewModel(
    private val repository: MeasurementRepository,
    private val dataType: DataType?
): ViewModel() {
    // Room Database Functions that calls the repository functions
    private fun upsertMeasurement(newMeasurement: Measurement) =
        viewModelScope.launch(Dispatchers.IO){
            repository.upsertMeasurement(newMeasurement)
        }

    fun deleteAllData() =
        viewModelScope.launch(Dispatchers.IO){
            repository.deleteAllData()
        }

    private fun getNumMeasurements(
        gridSquare: Map<String, MGRS>,
    ): Int {
        return repository.numData(
            dataType = dataType!!,
            neCorner = gridSquare["ne"].toString(),
            nwCorner = gridSquare["nw"].toString(),
            swCorner = gridSquare["sw"].toString(),
            seCorner = gridSquare["se"].toString(),
        )
    }

    fun avgValue(
        gridSquare: Map<String, MGRS>,
        processed: Boolean = true
    ): Double {
        return repository.avgValue(
            dataType = dataType!!,
            neCorner = gridSquare["ne"].toString(),
            nwCorner = gridSquare["nw"].toString(),
            swCorner = gridSquare["sw"].toString(),
            seCorner = gridSquare["se"].toString(),
            processed = processed
        )
    }

    fun avgValueByDataTypeGroupBySquare(
        dataType: DataType,
        processed: Boolean = true
    ): List<CornersAvgValue> {
        return repository.avgValueByDataTypeGroupBySquare(
            dataType = dataType,
            processed = processed
        )
    }

    private fun readMeasurements(
        gridSquare: Map<String, MGRS>,
        processed: Boolean = false
    ) : List<Measurement> {
        return repository.readData(
            dataType = dataType!!,
            neCorner = gridSquare["ne"].toString(),
            nwCorner = gridSquare["nw"].toString(),
            swCorner = gridSquare["sw"].toString(),
            seCorner = gridSquare["se"].toString(),
            processed = processed
        )
    }
    
    // Helper and Formatter Functions
    private fun insertMeasurement(
        value: Double,
        gridSquare: Map<String, MGRS>,
        processed: Boolean = false
    ) {
        val measurement = Measurement(
            dataType = dataType!!,
            neCorner = gridSquare["ne"].toString(),
            nwCorner = gridSquare["nw"].toString(),
            swCorner = gridSquare["sw"].toString(),
            seCorner = gridSquare["se"].toString(),
            value = value,
            processed = processed
        )
        this.upsertMeasurement(measurement)
    }

    suspend fun saveData(
        value: Double,
        latLng: LatLng,
        minNumMeasurements: Int
    ): Pair<Int, Map<String, MGRS>>
    = withContext(Dispatchers.IO) {
        val mgrs = MapHandler.latLngToMgrs(latLng)
        val gridSquare = getGridSquare(mgrs)

        val numMeasurements = async {
            getNumMeasurements(gridSquare)
        }.await()

        val remainingMeasurements = minNumMeasurements - (numMeasurements + 1) // +1 for the upcoming measurement

        val deferredQuery = if (remainingMeasurements >= 0)
            async {
                insertMeasurement(value, gridSquare)
            }
        else
            async {
                insertMeasurement(
                    value,
                    gridSquare,
                    processed = true
                )
            }
        deferredQuery.await()

        return@withContext Pair(remainingMeasurements, gridSquare)
    }

    suspend fun processDataAndCreateSquare(
        gridSquare: Map<String, MGRS>
    ) : Double = withContext(Dispatchers.IO) {

        val unprocessedData = async {
            readMeasurements(gridSquare)
        }.await()

        val deferredList = mutableListOf<Deferred<Job>>()

        for (entry in unprocessedData) {
            val deferred = async {
                val processedMeasurement = entry.copy(processed = true)
                upsertMeasurement(processedMeasurement)
            }
            deferredList.add(deferred)
        }
        deferredList.awaitAll()

        return@withContext getAvgValue(gridSquare)
    }

    suspend fun getAvgValue(
        gridSquare: Map<String, MGRS>
    ) : Double = withContext(Dispatchers.IO) {
        val avgValue = async {
            avgValue(gridSquare)
        }.await()

        return@withContext avgValue
    }


    private fun getGridSquare(
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

    /*
    private fun isCurrentGridSquare(
        gridSquare:Map<String, MGRS>,
        currentMgrs: MGRS,
        mgrs: MGRS
    ): Boolean {
        infix fun Long.isBetweenInclusive(range: LongRange) = this in range

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
