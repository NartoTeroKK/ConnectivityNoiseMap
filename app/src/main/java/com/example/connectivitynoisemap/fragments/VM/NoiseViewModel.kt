package com.example.connectivitynoisemap.fragments.VM

import androidx.lifecycle.ViewModel
import com.example.connectivitynoisemap.databinding.FragmentNoiseBinding

class NoiseViewModel() : ViewModel() {

    private lateinit var binding: FragmentNoiseBinding

    public fun setBinding(value: FragmentNoiseBinding){
        binding = value
    }
}