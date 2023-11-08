package com.example.connectivitynoisemap.main.fragments.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class FragmentViewModelFactory: ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MobileNetworkViewModel::class.java)) {
            return MobileNetworkViewModel() as T
        }
        if (modelClass.isAssignableFrom(NoiseViewModel::class.java)) {
            return NoiseViewModel() as T
        }
        if (modelClass.isAssignableFrom(WifiViewModel::class.java)) {
            return WifiViewModel() as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}