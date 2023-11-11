package com.example.connectivitynoisemap.main.module.implementation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MapHandlerViewModel(
    val mapHandler: MapHandler
) : ViewModel()

class MapHandlerViewModelFactory(private val mapHandler: MapHandler) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapHandlerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapHandlerViewModel(mapHandler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}