package com.example.connectivitynoisemap

import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.example.connectivitynoisemap.databinding.FragmentHomeBinding

class HomeViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    fun loadBgImage(binding: FragmentHomeBinding, fragment: HomeFragment){
        val imageUrl = "https://techcrunch.com/wp-content/uploads/2015/01/connectivity.jpg"
        val placeholderImg = R.drawable.img_bg_home
        val imageView = binding.imgHomeBg
        Glide.with(fragment)
            .load(imageUrl)
            .placeholder(placeholderImg)
            .into(imageView)
    }
}