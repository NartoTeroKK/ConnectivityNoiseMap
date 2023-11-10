package com.example.connectivitynoisemap.app_manual

import android.os.Bundle
import android.text.Html
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.connectivitynoisemap.R
import com.google.android.material.appbar.MaterialToolbar

@Suppress("DEPRECATION")
class AppManualActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_manual)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val topAppBar = findViewById<MaterialToolbar>(R.id.manualTopAppBar)
        //When the arrow back is clicked the Settings Activity is destroyed and the app return to Main Activity
        topAppBar.setNavigationOnClickListener {
            this.finish()
        }

        val text =
            "This app allows you to measure the quality of the mobile network signal, the WiFi signal and the acoustic noise in your area. " +
            "The measurements are displayed on a map and can be used to identify areas with poor signal or noise pollution.<br/><br/>" +
            "The app can be used in two ways:" +
            "<ul><li><b> Manual</b>: the measurements are run manually by the user.</li>" +
            "<li><b> Automatic</b>: the measurements are run automatically in the background.</li></ul>" +
            "The app is divided in three sections: <b>Main</b>, <b>Settings</b> and <b>App Manual</b>." +
            "The Main section is divided itself into four views among which you can navigate with the bottom navigation bar:" +
            "<ul><li><b> Home</b>: the landing view of the app. Contains 2 buttons to navigate to the Settings and Manual sections of the app.</li>" +
            "<li><b> Mobile</b>: this view contains a map with the measurements of the mobile network signal strength.</li>" +
            "<li><b> WiFi</b>: this view contains a map with the measurements of the WiFi signal strength.</li>" +
            "<li><b> Noise</b>: this view contains a map with the measurements of the acoustic noise.</li></ul>" +
            "The other two section are accessible from the Home menu:" +
            "<ul><li><b> Settings</b>: this section allows you to manage settings for the app's features.</li>" +
            "<li><b> App Manual</b>: this current section. It contains the manual of the app.</li></ul>" +
            "<i>NOTE: the app requires the location and microphone permission to run.</i>"

        findViewById<TextView>(R.id.manualTextView).text = Html.fromHtml(text)

    }
}