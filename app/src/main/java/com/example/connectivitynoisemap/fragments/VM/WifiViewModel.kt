package com.example.connectivitynoisemap.fragments.VM

import androidx.lifecycle.ViewModel
import com.example.connectivitynoisemap.databinding.FragmentWifiBinding

class WifiViewModel() : ViewModel() {

    private lateinit var binding: FragmentWifiBinding

    public fun setBinding(value: FragmentWifiBinding){
        binding = value
    }
}