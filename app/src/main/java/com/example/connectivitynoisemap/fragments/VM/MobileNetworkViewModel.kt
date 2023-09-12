package com.example.connectivitynoisemap.fragments.VM

import androidx.lifecycle.ViewModel
import com.example.connectivitynoisemap.databinding.FragmentMobileNetworkBinding

class MobileNetworkViewModel() : ViewModel() {

    private lateinit var binding: FragmentMobileNetworkBinding

    public fun setBinding(value: FragmentMobileNetworkBinding){
        binding = value
    }
}