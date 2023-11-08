package com.example.connectivitynoisemap.main.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.example.connectivitynoisemap.MeasurementApplication
import com.example.connectivitynoisemap.data.MeasurementViewModel
import com.example.connectivitynoisemap.data.MeasurementViewModelFactory
import com.example.connectivitynoisemap.data.type.DataType
import com.example.connectivitynoisemap.databinding.FragmentNoiseBinding
import com.example.connectivitynoisemap.main.MainActivity
import com.example.connectivitynoisemap.main.fragments.viewmodels.FragmentViewModelFactory
import com.example.connectivitynoisemap.main.fragments.viewmodels.NoiseViewModel
import com.example.connectivitynoisemap.main.interfaces.MeasurementFragmentInterface
import com.example.connectivitynoisemap.main.module.implementation.MapHandler
import com.example.connectivitynoisemap.main.module.implementation.MapHandlerViewModel
import com.example.connectivitynoisemap.main.module.implementation.OnMapLoaded
import com.example.connectivitynoisemap.main.utils.GUI
import com.example.connectivitynoisemap.main.utils.ValueClass
import kotlinx.coroutines.launch

class NoiseFragment :
    Fragment(),
    MeasurementFragmentInterface,
    OnMapLoaded
{

    private var _binding: FragmentNoiseBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val measurementViewModel: MeasurementViewModel
        by viewModels {
            MeasurementViewModelFactory(
                (activity.application as MeasurementApplication).repository,
                dataType
            )
        }
    private val fragmentViewModel: NoiseViewModel
        by viewModels{
            FragmentViewModelFactory()
        }

    private val mapHandler: MapHandler by lazy {
        ViewModelProvider(activity)[MapHandlerViewModel::class.java].mapHandler
    }

    private val dataType: DataType = DataType.NOISE
    private val isLocationPermGranted: Boolean
        get() = activity.isLocationPermGranted
    private val buttonState
        get() = activity.buttonState

    override val activity: MainActivity
        get() = requireActivity() as MainActivity
    override val fId: Int by lazy { dataType.ordinal }

    // METHODS

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            audioPermission()
        }
        TODO("test all cases of the permissions requests")
        /*
        if (!isLocationPermGranted) {
            locationPermission()
        }
         */
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoiseBinding.inflate(inflater, container, false)

        // Set the mapView of the MapHandler
        val mapView = binding.mapViewContainer.mapView
        mapHandler.setMapView(mapView, savedInstanceState)

        // Clear the map from all the squares if the map is ready
        /*
        if(mapHandler.isMapReady) {
            mapHandler.clearMap()
            mapHandler.drawMapSquares(dataType)
        }else
         */
        mapHandler.setOnMapLoadedListener(this)


        // Enable the action button or wait for the timer to finish
        if (buttonState.value!!)
            activity.enableActionBtn(true)
        else{
            activity.enableActionBtn(false)
        }
        buttonState.observe(viewLifecycleOwner) { state ->
            if (state == true)
                activity.enableActionBtn(true)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Show the action button
        activity.showActionBtn(true)
    }

    override fun onResume() {
        super.onResume()
        //mapHandler.onFragmentResumed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun audioPermission(){
        activity.requestAudioPermission()
    }

    override fun locationPermission(){
        activity.requestLocationPermission()
    }

    override fun onLocationPermGranted() {
        mapHandler.onLocationPermGranted()
    }

    override fun onMapLoaded() {
        // Draw the map squares for this fragment and data type
        mapHandler.drawMapSquares(dataType)
    }

    override fun measureValue(){
        // Temporarily disable the action button
        activity.tempDisableActionBtn(fId)
        activity.enableActionBtn(false)

        // acoustic Noise volume measurement
        fragmentViewModel.measureNoise(getDelaySharedPref())
        val volumeLiveData = fragmentViewModel.volume
        volumeLiveData.observe(viewLifecycleOwner){ volume ->
            if (volume != null && !volume.isNaN()) {
                fragmentViewModel.resetVolume()

                val latLngData = mapHandler.currentLatLng
                latLngData.observe(viewLifecycleOwner) { latLng ->
                    if(latLng != null) {
                        latLngData.removeObservers(viewLifecycleOwner)

                        lifecycleScope.launch {
                            val (remainingMeasurements, gridSquare) =
                                measurementViewModel.saveData(
                                    volume,
                                    latLng,
                                    activity.minNumMeasurementsSharedPref
                                )
                            if(remainingMeasurements > 0){
                                GUI.showToast(
                                    requireContext(),
                                    "$remainingMeasurements measurements remaining",
                                    Toast.LENGTH_SHORT
                                )
                            } else {
                                // Process the data inserted
                                GUI.showToast(
                                    requireContext(),
                                    "Processing Data...",
                                    Toast.LENGTH_SHORT
                                )
                                val avgValue =
                                    if(remainingMeasurements == 0)
                                    // Process the data in order to create the map square
                                    // for the first time in that grid square
                                        measurementViewModel.processDataAndCreateSquare(gridSquare)
                                    else
                                    // map square already exists: already processed data inserted
                                        measurementViewModel.getAvgValue(gridSquare)

                                val signalClass =
                                    ValueClass.fromValueToClass(dataType, avgValue)

                                mapHandler.addOrUpdateMapSquare(dataType, gridSquare, signalClass)
                            }
                        }
                    }

                }
            }
        }
    }

    private fun getDelaySharedPref(): Long {
        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val delayInSec = sp.getInt("noise_meter_time", 3).toLong()
        return delayInSec * 1000
    }

}
