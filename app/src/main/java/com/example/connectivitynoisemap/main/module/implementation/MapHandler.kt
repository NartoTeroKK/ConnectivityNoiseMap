package com.example.connectivitynoisemap.main.module.implementation

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.preference.PreferenceManager
import com.example.connectivitynoisemap.data.type.DataType
import com.example.connectivitynoisemap.main.MainActivity
import com.example.connectivitynoisemap.main.utils.GUI
import com.example.connectivitynoisemap.main.utils.LatLngListener
import com.example.connectivitynoisemap.main.utils.ValueClass
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.TileOverlayOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mil.nga.color.Color
import mil.nga.mgrs.MGRS
import mil.nga.mgrs.grid.GridType
import mil.nga.mgrs.grid.style.Grids
import mil.nga.mgrs.tile.MGRSTileProvider

interface OnMapLoaded{
    fun onMapLoaded()
}

class MapHandler(
    private val activity: MainActivity
) : OnMapReadyCallback
{
    companion object {
        private var instance: MapHandler? = null

        fun getInstance(activity: MainActivity): MapHandler {
            return instance ?: MapHandler( activity).also { instance = it }
        }

        fun mgrsToLatLng(mgrs: MGRS) : LatLng{
            return LatLng(mgrs.toPoint().latitude, mgrs.toPoint().longitude)
        }

        fun latLngToMgrs(latLng: LatLng) : MGRS {
            return MGRS.from(latLng.longitude, latLng.latitude)
        }
    }

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    private val tileProvider: MGRSTileProvider
    by lazy {
        initTileProvider(activity)
    }
    private val mLatLngListener: LatLngListener
    by lazy {
        LatLngListener.getInstance(activity, isLocationPermGranted)
    }
    val currentLatLng: LiveData<LatLng>
        get() = mLatLngListener.currentLatLng

    private var isZoomedIn = false

    private val isLocationEnabled: Boolean
        get() = mLatLngListener.isLocationEnabled

    private val isLocationPermGranted: Boolean
        get() = activity.isLocationPermGranted

    private val mapSquareWithColorList by lazy {
        activity.mapSquareWithColorList
    }
    private var listener: OnMapLoaded? = null

    // METHODS

    private fun initTileProvider(context: Context): MGRSTileProvider {
        val tenMtGrid = GridType.TEN_METER
        val grids = Grids.create(tenMtGrid)


        grids.disableAllLabelers()
        grids.setMaxZoom(tenMtGrid, 100)
        grids.setColor(tenMtGrid, tenMtGrid, Color.black())
        grids.setWidth(tenMtGrid, 3.0)

        return MGRSTileProvider.create(context, grids)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        //if(!isMapReady)
        listener?.onMapLoaded()
        //isMapReady = true
        configGoogleMap()

        if(!isLocationEnabled) {
            CoroutineScope(Dispatchers.Main).launch {
                GUI.showToast(activity, "Enable the GPS on your device")
            }
        }
        if( currentLatLng.value != null) {
            //addLocationMarker(currentLatLng.value!!)
            mapCameraZoom(currentLatLng.value!!)
        }else{
            mLatLngListener.currentLatLng.observe(activity) { latLng ->
                //addLocationMarker(latLng)
                if(!isZoomedIn) {
                    mapCameraZoom(latLng)
                    isZoomedIn = true
                }
            }
        }

    }

    /*
    private fun addLocationMarker(latLng: LatLng){
        googleMap.addMarker(
            MarkerOptions().position(latLng).title("Current Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        )
    }
     */

    private fun createMapGrid(){
        googleMap.addTileOverlay(TileOverlayOptions().tileProvider(tileProvider))
    }

    private fun configGoogleMap() {
        createMapGrid()
        if(isLocationPermGranted) {
            enableGoogleMapLocation()
        }
        googleMap.mapType = getMapTypeSharedPref()

    }

    @SuppressLint("MissingPermission")
    fun enableGoogleMapLocation(){
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        googleMap.isMyLocationEnabled = true
    }

    private fun mapCameraZoom(latLng: LatLng, zoom: Float = 20f){
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom)
        googleMap.animateCamera(cameraUpdate)
    }

    private fun getMapTypeSharedPref(): Int {
        val sp = PreferenceManager.getDefaultSharedPreferences(activity)
        val mapType= sp.getString("map_type", GoogleMap.MAP_TYPE_NONE.toString())

        if (mapType != null) {
            return mapType.toInt()
        }
        return GoogleMap.MAP_TYPE_NORMAL
    }

    fun onLocationPermGranted(){
        if (isLocationPermGranted){
            enableGoogleMapLocation()
            mLatLngListener.onLocationPermGranted()
        }
    }

    /*
    fun onFragmentResumed(){
        if (currentLatLng.value != null) {
            mapCameraZoom(currentLatLng.value!!)
        }
    }
     */

    fun onDestroy() {
        mLatLngListener.removeLocationUpdates()
        mapView.onDestroy()
        //googleMap.clear()
    }

    fun setMapView(mapView: MapView, savedInstanceState: Bundle?) {
        this.mapView = mapView
        this.mapView.getMapAsync(this)
        this.mapView.onCreate(savedInstanceState)
        this.mapView.onStart()
    }

    fun setOnMapLoadedListener(listener: OnMapLoaded){
        this.listener = listener
    }

    fun addOrUpdateMapSquare(dataType: DataType, gridSquare: Map<String, MGRS>, valueClass: Enum<*>) {
        val mapSquare = convertToMapSquare(gridSquare)

        val color = ValueClass.fromClassToColor(valueClass)

        // Get the MutableMap for the specific dataType
        val mutableMapForType = mapSquareWithColorList.getOrNull(dataType.ordinal)
            ?: run {
                mapSquareWithColorList.add(dataType.ordinal, mutableMapOf(mapSquare to 0))
                mapSquareWithColorList[dataType.ordinal]
            }

        mutableMapForType[mapSquare] = color
        drawMapSquare(mapSquare, color)
    }

    private fun convertToMapSquare(
        gridSquare: Map<String, MGRS>
    ) : Map<String, LatLng> {

        val mapSquare = mutableMapOf<String, LatLng>()
        for ((key, mgrs) in gridSquare) {
            val latLng = mgrsToLatLng(mgrs)
            mapSquare[key] = latLng
        }
        return mapSquare
    }

    fun drawMapSquares(dataType: DataType){
        googleMap.clear()

        val mapSquareWithColor =
            mapSquareWithColorList.getOrNull(dataType.ordinal) ?: return
        for ((mapSquare, color) in mapSquareWithColor) {
            drawMapSquare(mapSquare, color)
        }
    }

    private fun drawMapSquare(mapSquare: Map<String, LatLng>, color: Int) {
        val polygonOptions = PolygonOptions()
        polygonOptions.addAll(mapSquare.values)
        //polygonOptions.strokeColor(color)
        polygonOptions.strokeWidth(1f)
        polygonOptions.fillColor(color)
        polygonOptions.geodesic(true)
        this.googleMap.addPolygon(polygonOptions)
    }

}