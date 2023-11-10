package com.example.connectivitynoisemap.data.converter

import androidx.room.TypeConverter
import com.example.connectivitynoisemap.data.type.DataType
import java.util.Date

class Converters {
    @TypeConverter
    fun fromDataType(type: DataType): String {
        return type.name
    }
    @TypeConverter
    fun toDataType(type: String): DataType {
        return DataType.valueOf(type)
    }
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}