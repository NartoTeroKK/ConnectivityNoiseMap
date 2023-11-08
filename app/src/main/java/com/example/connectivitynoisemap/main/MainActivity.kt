package com.example.connectivitynoisemap.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
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
import com.example.connectivitynoisemap.MeasurementApplication
import com.example.connectivitynoisemap.R
import com.example.connectivitynoisemap.data.MeasurementViewModel
import com.example.connectivitynoisemap.data.MeasurementViewModelFactory
import com.example.connectivitynoisemap.data.type.DataType
import com.example.connectivitynoisemap.databinding.ActivityMainBinding
import com.example.connectivitynoisemap.main.fragments.HomeFragment
import com.example.connectivitynoisemap.main.fragments.NoiseFragment
import com.example.connectivitynoisemap.main.interfaces.MeasurementFragmentInterface
import com.example.connectivitynoisemap.main.module.MapModule
import com.example.connectivitynoisemap.main.module.MapModuleImpl
import com.example.connectivitynoisemap.main.module.implementation.MapHandlerViewModel
import com.example.connectivitynoisemap.main.module.implementation.MapHandlerViewModelFactory
import com.example.connectivitynoisemap.main.utils.ValueClass
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    // Floating Action Button state
    private val fragmentTimers =
        mutableMapOf<Int, CountDownTimer>()

    private val _buttonStateLiveData =
        mutableMapOf<Int, MutableLiveData<Boolean>>()
        init{
            val dataTypes =
                DataType.values()

            dataTypes.forEach { dataType ->
                _buttonStateLiveData[dataType.ordinal] = MutableLiveData(true)
            }
        }
    val buttonState: LiveData<Boolean>
        get() {
            val activeFragment = activeFragment() as MeasurementFragmentInterface
            return _buttonStateLiveData[activeFragment.fId]!!
        }
    // Shared Preferences
    private val disableTimeSharedPref: Long by lazy {
        100
        /*
        this.getPreferences(Context.MODE_PRIVATE)
            .getInt("num_minutes", 1)
            .toLong() * 60 * 1000
         */
    }
    val minNumMeasurementsSharedPref: Int by lazy {
        this.getPreferences(Context.MODE_PRIVATE)
            .getInt("num_measurements", 3)
    }

    // Map Module
    private var isMapHandlerInitialized: Boolean = false

    companion object{
        lateinit var mapModule: MapModule
    }

    val mapSquareWithColorList: MutableList< MutableMap <Map <String, LatLng>, Int>>
        = mutableListOf()

    // METHODS
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Generate map squares for all fragments
        lifecycleScope.launch(Dispatchers.IO) {
            generateMapSquares()
        }
        // setup bottom navigation bar with navigation controller
        val bottomNav = binding.bottomNavigation
        bottomNav.setupWithNavController(navController)
        //

        navController.addOnDestinationChangedListener{  _, destination, _ ->
            if((destination.id == R.id.mobileNetworkFragment
                || destination.id == R.id.wifiFragment
                || destination.id == R.id.noiseFragment
                ) && !isMapHandlerInitialized
            ) {
                // MapModule and MapHandlerViewModel initialization
                mapModule = MapModuleImpl(this)

                ViewModelProvider(
                    this,
                    MapHandlerViewModelFactory(mapModule.mapHandler)
                )[MapHandlerViewModel::class.java]

                isMapHandlerInitialized = true
            }

        }

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
                            Snackbar.LENGTH_SHORT
                        )
                        .setAnchorView(binding.actionButton)
                        .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
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
    }

    override fun onDestroy() {
        super.onDestroy()
        mapModule.mapHandler.onDestroy()
    }

    private suspend fun generateMapSquares() =
    withContext(Dispatchers.IO){
        // MeasurementViewModel to access Room query
        val measurementViewModel : MeasurementViewModel
        by viewModels {
            MeasurementViewModelFactory(
                (application as MeasurementApplication).repository,
                null
            )
        }
        for (dataType in DataType.values()){
            val roomJob = async {
                measurementViewModel.avgValueByDataTypeGroupBySquare(
                    dataType = dataType
                )
            }
            val data = roomJob.await()
            for (entry in data){
                val color =
                    ValueClass.fromClassToColor(
                        ValueClass.fromValueToClass(
                            dataType,
                            entry.avgValue
                        )
                    )
                val mapSquare = entry.toCornersLatLngMap()

                val mutableMapForType = mapSquareWithColorList.getOrNull(dataType.ordinal)
                    ?: run {
                        mapSquareWithColorList.add(dataType.ordinal, mutableMapOf(mapSquare to 0))
                        mapSquareWithColorList[dataType.ordinal]
                    }
                mutableMapForType[mapSquare] = color
            }
        }

    }

    fun showActionBtn(show: Boolean){
        if(show)
            binding.actionButton.show()
        else
            binding.actionButton.hide()
    }

    fun enableActionBtn(enable: Boolean){
        binding.actionButton.isEnabled = enable
    }

    fun tempDisableActionBtn(fragmentId: Int) {

        _buttonStateLiveData[fragmentId]!!.postValue(false)

        fragmentTimers[fragmentId] =
            object : CountDownTimer(disableTimeSharedPref, 500) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    _buttonStateLiveData[fragmentId]?.value = true
                    fragmentTimers[fragmentId]?.cancel()
                }
            }.start()
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
            this.LOCATION_PERMISSION_CODE)

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




