package com.example.connectivitynoisemap

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.example.connectivitynoisemap.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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
    }

    override fun onSupportNavigateUp(): Boolean {
        return false
    }
}
