package com.example.connectivitynoisemap

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class ActivityLifecycleObserver(private val update: () -> Unit) :
    DefaultLifecycleObserver {
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        owner.lifecycle.removeObserver(this)
        update()
    }
}


