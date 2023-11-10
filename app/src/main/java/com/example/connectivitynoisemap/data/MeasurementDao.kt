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

    @Query("UPDATE measurement_table" +
            " SET processed = :processed" +
            " WHERE dataType = :dataType AND neCorner = :neCorner AND nwCorner = :nwCorner AND swCorner = :swCorner AND seCorner = :seCorner")
    fun updateBySquare(
        dataType: DataType,
        processed: Boolean,
        neCorner: String,
        nwCorner: String,
        swCorner: String,
        seCorner: String
    )

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
            " WHERE dataType = :dataType AND neCorner = :neCorner AND nwCorner = :nwCorner AND swCorner = :swCorner AND seCorner = :seCorner AND processed = :processed" +
            " ORDER BY timestamp DESC LIMIT :numMeasurements")
    fun avgValue(
        dataType: DataType,
        neCorner: String,
        nwCorner: String,
        swCorner: String,
        seCorner: String,
        numMeasurements: Int,
        processed: Boolean = true
    ): Double

    // Retrieve all processed average value grouped by grid square corners and data type
    @Query("SELECT neCorner, nwCorner, swCorner, seCorner, AVG(value) as avgValue" +
            " FROM measurement_table " +
            " WHERE dataType = :dataType AND processed = :processed " +
            " GROUP BY neCorner, nwCorner, swCorner, seCorner" +
            " ORDER BY timestamp DESC LIMIT :numMeasurements")
    fun avgValueByDataTypeGroupBySquare(
        dataType: DataType,
        numMeasurements: Int,
        processed: Boolean = true
    ): List<CornersAvgValue>

    @Query("SELECT * FROM measurement_table" +
            " WHERE dataType = :dataType AND processed = :processed" +
            " ORDER BY timestamp DESC")
    fun getProcessedData(
        dataType: DataType,
        processed: Boolean = true
    ): List<Measurement>

    @Query("SELECT * FROM measurement_table" +
            " WHERE dataType = :dataType" +
            " ORDER BY timestamp DESC")
    fun getFilteredData(
        dataType: DataType,
    ): List<Measurement>

    /* DELETE ***/
    // Delete all entries
    @Query("DELETE FROM measurement_table")
    fun deleteAllData()



}