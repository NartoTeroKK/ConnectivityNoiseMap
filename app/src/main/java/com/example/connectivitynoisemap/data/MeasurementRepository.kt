package com.example.connectivitynoisemap.data

import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import com.example.connectivitynoisemap.data.entity.CornersAvgValue
import com.example.connectivitynoisemap.data.entity.CornersCount
import com.example.connectivitynoisemap.data.entity.Measurement
import com.example.connectivitynoisemap.data.type.DataType

class MeasurementRepository(private val measurementDao: MeasurementDao): ViewModel() {

    //
    fun readData(
        dataType: DataType,
        neCorner: String,
        nwCorner: String,
        swCorner: String,
        seCorner: String,
        processed: Boolean = false
    ): List<Measurement> =
        measurementDao.readData(dataType, neCorner, nwCorner, swCorner, seCorner, processed)

    //
    fun numData(
        dataType: DataType,
        neCorner: String,
        nwCorner: String,
        swCorner: String,
        seCorner: String,
    ): Int =
        measurementDao.numData(dataType, neCorner, nwCorner, swCorner, seCorner)

    fun avgValue(
        dataType: DataType,
        neCorner: String,
        nwCorner: String,
        swCorner: String,
        seCorner: String,
        numMeasurements: Int,
        processed: Boolean = true
    ): Double =
        measurementDao.avgValue(dataType, neCorner, nwCorner, swCorner, seCorner, numMeasurements, processed )

    fun avgValueByDataTypeGroupBySquare(
        dataType: DataType,
        numMeasurements: Int,
        processed: Boolean = true
    ): List<CornersAvgValue> {
        val measurements = measurementDao.getProcessedData(dataType, processed)

        val cornersAvgValueList = measurements.groupBy {
            listOf(it.neCorner, it.nwCorner, it.swCorner, it.seCorner)
        }.filter { (_, measurements) ->
            measurements.size >= numMeasurements
        }.map { (corners, measurements) ->
            val selectedMeasurements = measurements.take(numMeasurements)
            val avgValue = selectedMeasurements.map { it.value }.average()
            CornersAvgValue(corners[0], corners[1], corners[2], corners[3], avgValue)
        }

        return cornersAvgValueList
    }

    fun numDataByDataTypeGroupBySquare(
        dataType: DataType,
    ): List<CornersCount> {
        val measurements = measurementDao.getFilteredData(dataType)
        val groupedM = measurements.groupBy {
            listOf(it.neCorner, it.nwCorner, it.swCorner, it.seCorner)
        }
        val cornersCountList = groupedM.map { (corners, measurements) ->
            val count = measurements.size
            CornersCount(corners[0], corners[1], corners[2], corners[3], count)
        }

        return cornersCountList
    }

    //
    @WorkerThread
    fun updateBySquare(
        dataType: DataType,
        neCorner: String,
        nwCorner: String,
        swCorner: String,
        seCorner: String,
        processed: Boolean
    ){
        measurementDao.updateBySquare(dataType, processed, neCorner, nwCorner, swCorner, seCorner)
    }

    @WorkerThread
    fun upsertMeasurement(measurement: Measurement){
        measurementDao.upsertMeasurement(measurement)
    }

    //
    @WorkerThread
    fun deleteAllData(){
        measurementDao.deleteAllData()
    }

}