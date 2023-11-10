package com.example.connectivitynoisemap.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.connectivitynoisemap.data.converter.Converters
import com.example.connectivitynoisemap.data.entity.Measurement

@Database(
    entities = [Measurement::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MeasurementDatabase: RoomDatabase() {

    abstract fun measurementDao(): MeasurementDao

    companion object {
        @Volatile
        private var INSTANCE: MeasurementDatabase? = null

        fun getDatabase(context: Context): MeasurementDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MeasurementDatabase::class.java,
                    "measurement_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}