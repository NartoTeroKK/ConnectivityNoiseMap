package com.example.connectivitynoisemap.main.utils

import android.content.Context
import android.widget.Toast

class GUI {
    companion object{
        fun showToast(context: Context, msg: String, length: Int = Toast.LENGTH_LONG){
            Toast.makeText(context, msg, length).show()
        }
    }

}