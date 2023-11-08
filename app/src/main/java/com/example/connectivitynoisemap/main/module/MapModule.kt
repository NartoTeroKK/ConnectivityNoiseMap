package com.example.connectivitynoisemap.main.module

import com.example.connectivitynoisemap.main.MainActivity
import com.example.connectivitynoisemap.main.module.implementation.MapHandler

interface MapModule {
    val mapHandler: MapHandler
}

class MapModuleImpl(
    private val activity: MainActivity
): MapModule {

    override val mapHandler: MapHandler by lazy {
        MapHandler.getInstance(activity)
    }
}