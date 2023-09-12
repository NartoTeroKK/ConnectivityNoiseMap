package com.example.connectivitynoisemap.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.connectivitynoisemap.utilities.ActivityLifecycleObserver
import com.example.connectivitynoisemap.databinding.FragmentWifiBinding
import com.example.connectivitynoisemap.fragments.VM.WifiViewModel

class WifiFragment : Fragment() {

    companion object {
        fun newInstance() = WifiFragment()
    }

    var _binding: FragmentWifiBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: WifiViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWifiBinding.inflate(inflater, container, false)
        viewModel.setBinding(this.binding)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.lifecycle?.addObserver(ActivityLifecycleObserver {
            viewModel = ViewModelProvider(this).get(WifiViewModel::class.java)
            // TODO: Use the ViewModel
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}