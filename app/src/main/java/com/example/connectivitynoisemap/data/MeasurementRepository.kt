package com.example.connectivitynoisemap.data

import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import com.example.connectivitynoisemap.data.entity.CornersAvgValue
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
        processed: Boolean = true
    ): Double =
        measurementDao.avgValue(dataType, neCorner, nwCorner, swCorner, seCorner, processed)

    fun avgValueByDataTypeGroupBySquare(
        dataType: DataType,
        processed: Boolean = true
    ): List<CornersAvgValue> =
        measurementDao.avgValueByDataTypeGroupBySquare(dataType, processed)

    //
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