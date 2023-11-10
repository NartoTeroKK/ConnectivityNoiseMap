package com.example.connectivitynoisemap.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.connectivitynoisemap.data.entity.CornersAvgValue
import com.example.connectivitynoisemap.data.entity.CornersCount
import com.example.connectivitynoisemap.data.entity.Measurement
import com.example.connectivitynoisemap.data.type.DataType
import com.example.connectivitynoisemap.main.utils.MapUtils.Companion.getGridSquare
import com.example.connectivitynoisemap.main.utils.MapUtils.Companion.latLngToMgrs
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mil.nga.mgrs.MGRS
import java.util.Date

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

    private fun avgValue(
        gridSquare: Map<String, MGRS>,
        numMeasurements: Int,
        processed: Boolean = true
    ): Double {
        return repository.avgValue(
            dataType = dataType!!,
            neCorner = gridSquare["ne"].toString(),
            nwCorner = gridSquare["nw"].toString(),
            swCorner = gridSquare["sw"].toString(),
            seCorner = gridSquare["se"].toString(),
            processed = processed,
            numMeasurements = numMeasurements
        )
    }

    fun avgValueByDataTypeGroupBySquare(
        dataType: DataType,
        processed: Boolean = true,
        numMeasurements: Int
    ): List<CornersAvgValue> {
        return repository.avgValueByDataTypeGroupBySquare(
            dataType = dataType,
            processed = processed,
            numMeasurements = numMeasurements
        )
    }

    fun numDataByDataTypeGroupBySquare(
        dataType: DataType,
    ): List<CornersCount> {
        return repository.numDataByDataTypeGroupBySquare(
            dataType = dataType,
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
            processed = processed,
            timestamp = Date().time
        )
        this.upsertMeasurement(measurement)
    }

    fun updateBySquare(
        dataType: DataType,
        processed: Boolean,
        neCorner: String,
        nwCorner: String,
        swCorner: String,
        seCorner: String
    ) {
        repository.updateBySquare(
            dataType = dataType,
            processed = processed,
            neCorner = neCorner,
            nwCorner = nwCorner,
            swCorner = swCorner,
            seCorner = seCorner
        )
    }

    suspend fun saveData(
        value: Double,
        latLng: LatLng,
        minNumMeasurements: Int
    ): Int
    = withContext(Dispatchers.IO) {
        val mgrs = latLngToMgrs(latLng)
        val gridSquare = getGridSquare(mgrs)

        val numMeasurements = async {
            getNumMeasurements(gridSquare)
        }.await()

        val remainingMeasurements = minNumMeasurements - (numMeasurements + 1) // +1 for the upcoming measurement

        val deferredQuery =
            if (remainingMeasurements >= 0)
                async {
                    insertMeasurement(
                        value,
                        gridSquare,
                        processed = false
                    )
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

        return@withContext remainingMeasurements
    }

    suspend fun processDataAndCreateSquare(
        gridSquare: Map<String, MGRS>,
        numMeasurements: Int
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

        return@withContext getAvgValue(gridSquare, numMeasurements)
    }

    suspend fun getAvgValue(
        gridSquare: Map<String, MGRS>,
        numMeasurements: Int
    ) : Double = withContext(Dispatchers.IO) {
        val avgValue = async {
            avgValue(gridSquare, numMeasurements)
        }.await()

        return@withContext avgValue
    }


}
