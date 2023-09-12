package com.example.connectivitynoisemap.fragments.VM

import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.example.connectivitynoisemap.R
import com.example.connectivitynoisemap.databinding.FragmentHomeBinding
import com.example.connectivitynoisemap.fragments.HomeFragment

class HomeViewModel : ViewModel() {

    private lateinit var binding: FragmentHomeBinding

    public fun setBinding(value: FragmentHomeBinding){
        binding = value
    }

    fun loadBgImage(fragment: HomeFragment){
        val imageUrl = "https://techcrunch.com/wp-content/uploads/2015/01/connectivity.jpg"
        val placeholderImg = R.drawable.img_bg_home
        val imageView = this.binding.imgHomeBg
        Glide.with(fragment)
            .load(imageUrl)
            .placeholder(placeholderImg)
            .into(imageView)
    }
}