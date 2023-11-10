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
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
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
import com.example.connectivitynoisemap.main.module.implementation.OnMapLoaded
import com.example.connectivitynoisemap.main.utils.GUI
import com.example.connectivitynoisemap.main.utils.MapUtils
import com.example.connectivitynoisemap.main.utils.ValueClass
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import mil.nga.mgrs.MGRS

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
        activity.mapHandlerViewModel.mapHandler
    }
    private val currentLatLng: LiveData<LatLng>
    by lazy { mapHandler.currentLatLng }

    private val currentGridSquare : Map<String, MGRS>
        get() {
            return MapUtils.getGridSquare(MapUtils.latLngToMgrs(currentLatLng.value!!))
        }

    private val isLocationPermGranted: Boolean
        get() = activity.isLocationPermGranted

    override val activity: MainActivity
        get() = requireActivity() as MainActivity

    override val dataType: DataType = DataType.NOISE

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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoiseBinding.inflate(inflater, container, false)

        // Set onMapReady() called in MapHandler Listener
        mapHandler.setOnMapLoadedListener(this)
        // Set the mapView of the MapHandler
        val mapView = binding.mapViewContainer.mapView
        mapHandler.setMapView(mapView, savedInstanceState)
        /*
        currentLatLng.observe(viewLifecycleOwner){ latLng ->
            if(latLng != null){
                val gridSquare = MapUtils.getGridSquare(MapUtils.latLngToMgrs(latLng))
                val stateData = activity.getButtonState(dataType, gridSquare)
                stateData.observe(viewLifecycleOwner){ state ->
                    if(activity.isBgOperationEnabledSP) {
                        if (state)
                            this.measureValue()
                    }else
                        activity.enableActionBtn(state)
                }
            }
        }

        if (currentLatLng.value != null) {
            val stateData = activity.getButtonState(dataType, currentGridSquare)
            stateData.observe(viewLifecycleOwner) { state ->
                if(activity.isBgOperationEnabledSP) {
                    if (state)
                        this.measureValue()
                }else
                    activity.enableActionBtn(state)
            }
        }
        */


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Show the action button if background operations are disabled
        if(!activity.isBgOperationEnabledSP)
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

        activity.showProgressBar(true)
        activity.enableBottomNav(false)
        activity.enableActionBtn(false)
        // acoustic Noise volume measurement
        fragmentViewModel.measureNoise(activity.noiseMeasurementTimeSP)

        val volumeLiveData = fragmentViewModel.volume
        volumeLiveData.observe(viewLifecycleOwner){ volume ->
            if (volume >= 0.0) {
                fragmentViewModel.resetVolume()

                activity.showProgressBar(false)
                activity.enableBottomNav(true)

                val latLngData = currentLatLng
                latLngData.observe(viewLifecycleOwner) { latLng ->
                    if(latLng != null) {
                        latLngData.removeObservers(viewLifecycleOwner)

                        lifecycleScope.launch {
                            val gridSquare = MapUtils.getGridSquare(MapUtils.latLngToMgrs(latLng))

                            // Temporarily disable the action button
                            activity.tempDisableActionBtn(
                                dataType,
                                gridSquare
                            )


                            val remainingMeasurements =
                                measurementViewModel.saveData(
                                    volume,
                                    latLng,
                                    activity.numMeasurementsSP
                                )
                            if(remainingMeasurements > 0){
                                GUI.showToast(
                                    requireContext(),
                                    "$remainingMeasurements measurements remaining",
                                    Toast.LENGTH_SHORT
                                )
                            } else { // remainingMeasurements <= 0
                                // Process the data inserted
                                GUI.showToast(
                                    requireContext(),
                                    "Measurement completed",
                                    Toast.LENGTH_SHORT
                                )
                                val avgValue =
                                    if(remainingMeasurements == 0)
                                    // Process the data in order to create the map square
                                    // for the first time in that grid square
                                        measurementViewModel.processDataAndCreateSquare(
                                            gridSquare,
                                            activity.numMeasurementsSP
                                        )
                                    else
                                    // map square already exists: already processed data inserted
                                        measurementViewModel.getAvgValue(
                                            gridSquare,
                                            activity.numMeasurementsSP
                                        )

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

}
