package com.example.yoursoundtrack

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.yoursoundtrack.managers.FirebaseAuthManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment

        navHostFragment?.let { host ->
            val navController = host.navController
            bottomNavigationView?.setupWithNavController(navController)

            // 1. Check Auth without re-inflating the graph on the main thread
            if (FirebaseAuthManager.isUserLoggedIn()) {
                // If user is already logged in and current screen is Auth, skip straight to Home
                if (navController.currentDestination?.id == R.id.navigation_auth) {
                    navController.navigate(R.id.navigation_home)
                }
            }

            // 2. Control BottomNavigationView visibility based on destination
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.navigation_auth -> {
                        bottomNavigationView?.visibility = View.GONE
                    }
                    else -> {
                        bottomNavigationView?.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}