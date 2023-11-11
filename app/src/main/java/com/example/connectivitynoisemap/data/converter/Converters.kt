package com.example.connectivitynoisemap.data.converter

import androidx.room.TypeConverter
import com.example.connectivitynoisemap.data.type.DataType

class Converters {
    @TypeConverter
    fun fromDataType(type: DataType): String {
        return type.name
    }
    @TypeConverter
    fun toDataType(type: String): DataType {
        return DataType.valueOf(type)
    }
}