package com.example.connectivitynoisemap

import android.app.Application
import com.example.connectivitynoisemap.data.MeasurementDatabase
import com.example.connectivitynoisemap.data.MeasurementRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MeasurementApplication: Application(){

    private lateinit var database: MeasurementDatabase
    lateinit var repository: MeasurementRepository

    init {
        val appContext = this
        CoroutineScope(Dispatchers.IO).launch{
            database = MeasurementDatabase.getDatabase(appContext)
            repository = MeasurementRepository(database.measurementDao())
        }
    }
}