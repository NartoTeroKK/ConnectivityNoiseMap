package com.example.connectivitynoisemap.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModelProvider
import com.example.connectivitynoisemap.utilities.ActivityLifecycleObserver
import com.example.connectivitynoisemap.databinding.FragmentHomeBinding
import com.example.connectivitynoisemap.fragments.VM.HomeViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), LifecycleObserver {

    companion object {
        fun newInstance() = HomeFragment()
    }

     var _binding: FragmentHomeBinding? = null
     // This property is only valid between onCreateView and onDestroyView.
     private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View ? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel.setBinding(this.binding)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadBgImage(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.lifecycle?.addObserver(ActivityLifecycleObserver {
            viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
            // TODO: Use the ViewModel
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

