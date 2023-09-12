package com.example.connectivitynoisemap.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.connectivitynoisemap.utilities.ActivityLifecycleObserver
import com.example.connectivitynoisemap.databinding.FragmentNoiseBinding
import com.example.connectivitynoisemap.fragments.VM.NoiseViewModel

class NoiseFragment : Fragment() {

    companion object {
        fun newInstance() = NoiseFragment()
    }

    var _binding: FragmentNoiseBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: NoiseViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNoiseBinding.inflate(inflater, container, false)
        viewModel.setBinding(this.binding)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.lifecycle?.addObserver(ActivityLifecycleObserver {
            viewModel = ViewModelProvider(this).get(NoiseViewModel::class.java)
            // TODO: Use the ViewModel
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}