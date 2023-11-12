package com.example.connectivitynoisemap.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.example.connectivitynoisemap.MeasurementApplication
import com.example.connectivitynoisemap.R
import com.example.connectivitynoisemap.data.MeasurementViewModel
import com.example.connectivitynoisemap.data.MeasurementViewModelFactory
import com.example.connectivitynoisemap.data.entity.CornersAvgValue
import com.example.connectivitynoisemap.data.type.DataType
import com.example.connectivitynoisemap.databinding.ActivityMainBinding
import com.example.connectivitynoisemap.main.fragments.HomeFragment
import com.example.connectivitynoisemap.main.fragments.NoiseFragment
import com.example.connectivitynoisemap.main.interfaces.MeasurementFragmentInterface
import com.example.connectivitynoisemap.main.module.MapModule
import com.example.connectivitynoisemap.main.module.MapModuleImpl
import com.example.connectivitynoisemap.main.module.implementation.MapHandlerViewModel
import com.example.connectivitynoisemap.main.module.implementation.MapHandlerViewModelFactory
import com.example.connectivitynoisemap.main.utils.MapUtils.Companion.getGridSquare
import com.example.connectivitynoisemap.main.utils.MapUtils.Companion.latLngToMgrs
import com.example.connectivitynoisemap.main.utils.MapUtils.Companion.toGridSquareString
import com.example.connectivitynoisemap.main.utils.ValueClass
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mil.nga.mgrs.MGRS

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val LOCATION_PERMISSION_CODE = 100
    private val AUDIO_PERMISSION_CODE = 101

    private val navHostFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    }
    private val navController by lazy {
        this.navHostFragment.navController
    }
    val isLocationPermGranted: Boolean by lazy {
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    // Floating Action Button and background operation state

    private val _enablingState =
        mutableListOf<MutableMap <Map <String, String>, MutableLiveData<Boolean> >>()

    // Shared Preferences

    private val disableTimeSP: Long
        get() {
            return PreferenceManager.getDefaultSharedPreferences(this)
                .getInt("num_minutes", 1)
                .toLong() * 60 * 1000
        }
    val numMeasurementsSP: Int
        get(){
            return PreferenceManager.getDefaultSharedPreferences(this)
                .getInt("num_measurements", 5)
        }

    val isBgOperationEnabledSP: Boolean
        get(){
            return PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("bg_operation", false)
        }
    val noiseMeasurementTimeSP: Long
        get(){
            return PreferenceManager.getDefaultSharedPreferences(this)
                .getInt("noise_measuring_time", 5)
                .toLong() * 1000
        }

    // Map Module
    private var isMapHandlerInitialized: Boolean = false
    lateinit var mapHandlerViewModel: MapHandlerViewModel

    companion object{
        lateinit var mapModule: MapModule
    }

    val mapSquareWithColorList: MutableList< MutableMap <Map <String, LatLng>, Int>>
        = mutableListOf()

    var isMeasuring = false


    init{
        val dataTypes =
            DataType.values()

        dataTypes.forEach { dataType ->
            _enablingState.add(dataType.ordinal, mutableMapOf())
            mapSquareWithColorList.add(dataType.ordinal, mutableMapOf())
        }
    }

    // METHODS
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Generate map squares for all fragments
        lifecycleScope.launch {
            generateMapSquares()
        }
        // setup bottom navigation bar with navigation controller
        val bottomNav = binding.bottomNavigation
        bottomNav.setupWithNavController(navController)
        //

        // MapModule and MapHandlerViewModel initialization
        mapModule = MapModuleImpl(this)

        mapHandlerViewModel = ViewModelProvider(
            this,
            MapHandlerViewModelFactory(mapModule.mapHandler)
        )[MapHandlerViewModel::class.java]

        isMapHandlerInitialized = true

        // Hide the action button because the start destination is HomeFragment
        showActionBtn(false)

        binding.actionButton.setOnClickListener {

            when (val activeFragment = activeFragment()) {
                is MeasurementFragmentInterface -> {
                    activeFragment.measureValue()

                    if(activeFragment is NoiseFragment){
                        Snackbar.make(
                            activeFragment.requireContext(),
                            activeFragment.requireView(),
                            "Recording acoustic noise...",
                            Snackbar.LENGTH_LONG
                        )
                        .setAnchorView(binding.actionButton)
                        .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                        .show()
                    }
                }
                is HomeFragment -> {
                    error("ERROR: BUTTON SHOULD BE HIDDEN in HomeFragment")
                }
                else -> {
                    error("ERROR: Retrieve active fragment")
                }
            }
        }

        // Observe the state on the active fragment
        observeState()
    }

    override fun onResume() {
        super.onResume()
        Log.d("BG OPERATIONS","Background operations enabled: $isBgOperationEnabledSP")
    }

    override fun onRestart() {
        super.onRestart()
        lifecycleScope.launch {
            generateMapSquares()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapModule.mapHandler.onDestroy()
    }

    private suspend fun generateMapSquares() {
        // MeasurementViewModel to access Room query
        val measurementViewModel : MeasurementViewModel
            by viewModels {
                MeasurementViewModelFactory(
                    (application as MeasurementApplication).repository,
                    null
                )
            }
        withContext(Dispatchers.Default){

            for (dataType in DataType.values()){

                processOrDeprocessData(measurementViewModel, dataType)

                val cornersAvgValueList = async {
                    measurementViewModel.avgValueByDataTypeGroupBySquare(
                        dataType = dataType,
                        numMeasurements = numMeasurementsSP,
                        processed = true
                    )
                }.await()
                for (entry in cornersAvgValueList){
                    val color =
                        ValueClass.fromClassToColor(
                            ValueClass.fromValueToClass(
                                dataType,
                                entry.avgValue
                            )
                        )
                    val mapSquare = entry.toCornersLatLngMap()

                    val mutableMapForType = mapSquareWithColorList[dataType.ordinal]
                    mutableMapForType[mapSquare] = color
                }
                // Check integrity: if mapSquareWithColor reflect the database entries
                checkIntegrity(dataType, cornersAvgValueList)
            }

        }
    }

    private suspend fun processOrDeprocessData(
        measurementViewModel: MeasurementViewModel,
        dataType: DataType,
    ) = withContext(Dispatchers.IO) {
        val cornersCountList = async {
            measurementViewModel.numDataByDataTypeGroupBySquare(
                dataType = dataType,
            )
        }.await()
        val updates = mutableListOf<Deferred<Unit>>()
        for (entry in cornersCountList) {
            val bool = entry.count >= numMeasurementsSP
            val update =
                async {
                    measurementViewModel.updateBySquare(
                        dataType = dataType,
                        neCorner = entry.neCorner,
                        nwCorner = entry.nwCorner,
                        swCorner = entry.swCorner,
                        seCorner = entry.seCorner,
                        processed = bool
                    )
                }
            updates.add(update)
        }
        updates.awaitAll()
    }

    private fun checkIntegrity(
        dataType: DataType,
        cornersAvgValueList: List<CornersAvgValue>
    ){
        if(mapSquareWithColorList[dataType.ordinal].size > cornersAvgValueList.size){
            val newMap = mutableMapOf <Map <String, LatLng>, Int>()
            for (entry in cornersAvgValueList){
                val mapSquare = entry.toCornersLatLngMap()
                if(mapSquareWithColorList[dataType.ordinal].containsKey(mapSquare)){
                    newMap[mapSquare] = mapSquareWithColorList[dataType.ordinal][mapSquare]!!
                }
            }
            mapSquareWithColorList[dataType.ordinal] = newMap
        }
    }

    fun showActionBtn(show: Boolean){
        if(show)
            binding.actionButton.show()
        else
            binding.actionButton.hide()
    }

    fun enableActionBtn(enable: Boolean){
        binding.actionButton.isEnabled   = enable
    }

    fun enableBottomNav(enable: Boolean){
        binding.bottomNavigation.isEnabled = enable
        binding.bottomNavigation.isClickable = enable
        binding.bottomNavigation.isActivated = enable
    }

    fun showProgressBar(enable: Boolean){
        val progressBar = binding.progressBar
        val view = binding.root

        if (enable){
            progressBar?.visibility = View.VISIBLE
            view.alpha = 0.5f
        }else{
            progressBar?.visibility = View.GONE
            view.alpha = 1f
        }
    }

    suspend fun tempDisableActionBtn(dataType: DataType, gridSquare: Map<String, MGRS>){
        val currGridSquare = toGridSquareString(gridSquare)
        val fragmentId = dataType.ordinal

        _enablingState[fragmentId].putIfAbsent(currGridSquare, MutableLiveData())
        _enablingState[fragmentId][currGridSquare]?.postValue(false)

        val handler = Handler(Looper.getMainLooper())
        val runnable = Runnable {
            _enablingState[fragmentId][currGridSquare]!!.postValue (true)
            Log.d("TIMER", "Timer expired")
        }
        withContext(Dispatchers.Main) {
            handler.postDelayed(runnable, disableTimeSP)
        }
        // Observe the state on the active fragment
        observeState()
    }

    private fun observeState(){

        if (activeFragment() is MeasurementFragmentInterface) { // No need to observe state in HomeFragment

            val latLngLiveData = mapModule.mapHandler.currentLatLng
            latLngLiveData.observe(this) { latLng ->
                if (latLng != null) {
                    val activeFragment = activeFragment()
                    if(activeFragment is MeasurementFragmentInterface) {
                        val gridSquare = getGridSquare(latLngToMgrs(latLng))
                        val stateData = getEnablingState(activeFragment.dataType, gridSquare)
                        stateData.observe(this) { state ->
                            if (isBgOperationEnabledSP) {
                                val currentLatLng = mapModule.mapHandler.currentLatLng.value!!
                                val currentGridSquare = getGridSquare(latLngToMgrs(currentLatLng))
                                if (toGridSquareString(currentGridSquare) == toGridSquareString(gridSquare)) {
                                    if (state && !isMeasuring) {
                                        activeFragment.measureValue()
                                        //isMeasuring = true
                                    }
                                }
                            } else {
                                enableActionBtn(state)
                            }

                        }
                    }
                }
            }
        }
    }

    private fun getEnablingState(dataType: DataType, gridSquare: Map<String, MGRS>)
            : LiveData<Boolean> {
        val fId = dataType.ordinal
        val gridSquareString = toGridSquareString(gridSquare)
        return _enablingState[fId].getOrDefault(gridSquareString, null)
            ?: run { val mld = MutableLiveData<Boolean>()
                mld.postValue(true)
                mld
            }
    }

    private fun activeFragment(): Fragment {
        val fragment = navHostFragment.childFragmentManager.fragments[0]
        if (fragment != null) {
            return fragment
        }
        return navHostFragment
    }

    fun requestLocationPermission(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            this.LOCATION_PERMISSION_CODE
        )
    }

    fun requestAudioPermission(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            AUDIO_PERMISSION_CODE
        )
    }

    private fun permissionAlert(requestFunction: ()->Unit){
        // Create the object of AlertDialog Builder class
        val builder = AlertDialog.Builder(this)

        builder.setMessage("If you want to use this app you need to grant permission for both microphone and location. \nAre you willing to do that?")

        builder.setTitle("Mandatory permission")
        // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
        builder.setCancelable(false)

        builder.setPositiveButton("Yes") {
            // If user click yes then ap request again the mic permission
                dialog, _ -> dialog.cancel()
            requestFunction()
        }

        builder.setNegativeButton("No") {
            // If the user click no button then app will close
                _, _ -> this.finish()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    permissionAlert { requestLocationPermission() }
                }else{
                    val activeFragment = activeFragment()
                    if (activeFragment is MeasurementFragmentInterface) {
                        activeFragment.onLocationPermGranted()
                    }
                }
            }
            AUDIO_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    permissionAlert { requestAudioPermission() }
                }else{
                    if (!isLocationPermGranted)
                        requestLocationPermission()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


}




