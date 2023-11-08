package com.example.connectivitynoisemap.main.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import com.example.connectivitynoisemap.R
import com.example.connectivitynoisemap.app_manual.AppManualActivity
import com.example.connectivitynoisemap.databinding.FragmentHomeBinding
import com.example.connectivitynoisemap.main.MainActivity
import com.example.connectivitynoisemap.settings.SettingsActivity
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class HomeFragment : Fragment(), LifecycleObserver {

     private var _binding: FragmentHomeBinding? = null
     // This property is only valid between onCreateView and onDestroyView.
     private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Hide the action button
        (requireActivity() as MainActivity).showActionBtn(false)

        binding.btnManual.setOnClickListener {
            goToActivity(AppManualActivity::class.java)
        }

        binding.btnSettings.setOnClickListener {
            goToActivity(SettingsActivity::class.java)
        }

        loadBgImage()

        return binding.root
    }

    private fun goToActivity(activityClass: Class<*> ){
        val intent = Intent(requireActivity(), activityClass)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadBgImage(){
        val imageUrl = "https://techcrunch.com/wp-content/uploads/2015/01/connectivity.jpg"
        val placeholderImg = R.drawable.img_bg_home
        val imageView = binding.imgHomeBg
        Picasso.get()
            .load(imageUrl)
            .placeholder(placeholderImg)
            .error(placeholderImg)
            .into(imageView, object : Callback {
                override fun onSuccess() {
                    Log.d("IMAGE", "Image loaded successfully")
                }

                override fun onError(e: Exception?) {
                    Log.e("IMAGE", "Error loading image: ${e?.message}")
                }
            })
    }

}

