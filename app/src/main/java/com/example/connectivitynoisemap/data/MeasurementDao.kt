package com.example.connectivitynoisemap.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.connectivitynoisemap.data.entity.CornersAvgValue
import com.example.connectivitynoisemap.data.entity.Measurement
import com.example.connectivitynoisemap.data.type.DataType

@Dao
interface MeasurementDao {

    /* UPSERT ***/
    // INSERT and UPDATE an Entry
    @Upsert
    fun upsertMeasurement(measurement: Measurement)

    /* SELECT */
    // Select all unprocessed data by grid square corners
    @Query("SELECT * FROM measurement_table " +
            "WHERE dataType = :dataType AND neCorner = :neCorner AND nwCorner = :nwCorner AND swCorner = :swCorner AND seCorner = :seCorner AND processed = :processed")
    fun readData(
        dataType: DataType,
        neCorner: String,
        nwCorner: String,
        swCorner: String,
        seCorner: String,
        processed: Boolean = false,
    ): List<Measurement>

    // Retrieve total number of unprocessed or processed measurements by grid square corners
    @Query("SELECT COUNT(*) FROM measurement_table " +
            "WHERE dataType = :dataType AND neCorner = :neCorner AND nwCorner = :nwCorner AND swCorner = :swCorner AND seCorner = :seCorner ")
    fun numData(
        dataType: DataType,
        neCorner: String,
        nwCorner: String,
        swCorner: String,
        seCorner: String,
    ): Int

    // Retrieve average value of processed data by grid square corners
    @Query("SELECT AVG(value) FROM measurement_table " +
            "WHERE dataType = :dataType AND neCorner = :neCorner AND nwCorner = :nwCorner AND swCorner = :swCorner AND seCorner = :seCorner AND processed = :processed")
    fun avgValue(
        dataType: DataType,
        neCorner: String,
        nwCorner: String,
        swCorner: String,
        seCorner: String,
        processed: Boolean = true
    ): Double

    // Retrieve all processed average value grouped by grid square corners and data type
    @Query("SELECT neCorner, nwCorner, swCorner, seCorner, AVG(value) as avgValue" +
            " FROM measurement_table " +
            " WHERE dataType = :dataType AND processed = :processed " +
            " GROUP BY neCorner, nwCorner, swCorner, seCorner")
    fun avgValueByDataTypeGroupBySquare(
        dataType: DataType,
        processed: Boolean = true
    ): List<CornersAvgValue>

    /* DELETE ***/
    // Delete all entries
    @Query("DELETE FROM measurement_table")
    fun deleteAllData()



}