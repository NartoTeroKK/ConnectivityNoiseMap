package com.example.connectivitynoisemap

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.connectivitynoisemap.databinding.FragmentMobileNetworkBinding

class MobileNetworkFragment : Fragment() {

    companion object {
        fun newInstance() = MobileNetworkFragment()
    }

    var _binding: FragmentMobileNetworkBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: MobileNetworkViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMobileNetworkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.lifecycle?.addObserver(ActivityLifecycleObserver {
            viewModel = ViewModelProvider(this).get(MobileNetworkViewModel::class.java)
            // TODO: Use the ViewModel
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}