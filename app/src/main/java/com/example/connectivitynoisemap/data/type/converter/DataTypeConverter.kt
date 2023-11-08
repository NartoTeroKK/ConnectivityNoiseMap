package com.example.connectivitynoisemap.data.type.converter

import androidx.room.TypeConverter
import com.example.connectivitynoisemap.data.type.DataType

class DataTypeConverter {
    @TypeConverter
    fun fromEDataType(type: DataType): String {
        return type.name
    }

    @TypeConverter
    fun toEDataType(type: String): DataType {
        return DataType.valueOf(type)
    }
}