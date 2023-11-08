package com.example.connectivitynoisemap.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.connectivitynoisemap.data.type.DataType

class MeasurementViewModelFactory(
    private val repository: MeasurementRepository,
    private val dataType: DataType?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MeasurementViewModel::class.java)) {
            return MeasurementViewModel(repository, dataType) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}