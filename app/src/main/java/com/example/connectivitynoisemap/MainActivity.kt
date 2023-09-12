package com.example.connectivitynoisemap

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.example.connectivitynoisemap.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val RECORD_AUDIO_PERMISSION_CODE = 123

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        //val navController = navHostFragment.navController

        //val navController: NavController = findNavController(R.id.nav_host_fragment_content_main)
        //navController.setGraph(R.id.nav_graph)

        val bottomNav = binding.bottomNavigation
        bottomNav.selectedItemId = R.id.homeFragment
        bottomNav.setOnItemSelectedListener { item ->
            // By using switch we can easily get the selected fragment by using there id
            lateinit var selectedFragment: Fragment
            when (item.itemId) {
                R.id.home -> {
                    selectedFragment = HomeFragment()
                }

                R.id.mobile_network -> {
                    selectedFragment = MobileNetworkFragment()
                }

                R.id.wifi -> {
                    selectedFragment = WifiFragment()
                }

                R.id.noise -> {
                    selectedFragment = NoiseFragment()
                }
            }
            // It will help to replace the one fragment to other.
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_content_main, selectedFragment).commit()
            true
        }

        binding.actionButton.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.action_button)
                .setAction("Action", null).show()
        }

        //Request mic permission
        this.requestMicPermission()

    }

    private fun requestMicPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            // Richiedi il permesso se non è stato già concesso
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                this.RECORD_AUDIO_PERMISSION_CODE
            )
        }
    }

    private fun createAndShowMicPermissionAlert(){
        // Create the object of AlertDialog Builder class
        val builder = AlertDialog.Builder(this)

        // Set the message show for the Alert time
        builder.setMessage("If you want to use this App you have to grant microphone permission. You up for that?")

        // Set Alert Title
        builder.setTitle("Mic Permission")

        // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
        builder.setCancelable(false)

        // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
        builder.setPositiveButton("Yes") {
            // If user click no then dialog box is canceled.
            dialog, which -> dialog.cancel()
            this.requestMicPermission()

        }

        // Set the Negative button with No name Lambda OnClickListener method is use of DialogInterface interface.
        builder.setNegativeButton("No") {
            // When the user click no button then app will close
            dialog, which -> finish()
        }

        // Create the Alert dialog
        val alertDialog = builder.create()
        // Show the Alert Dialog box
        alertDialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permesso concesso, puoi utilizzare il microfono.
                println("Permesso microfono concesso")
            } else {
                // Permesso negato, devi gestire questo caso di conseguenza.
                println("Permesso microfono NON concesso")
                this.createAndShowMicPermissionAlert()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return false
    }
}
