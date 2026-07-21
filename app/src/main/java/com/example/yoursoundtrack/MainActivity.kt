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

            // Inflate navigation graph programmatically
            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

            // Determine initial destination based on auth status
            if (FirebaseAuthManager.isUserLoggedIn()) {
                navGraph.setStartDestination(R.id.navigation_home)
            } else {
                navGraph.setStartDestination(R.id.navigation_auth)
            }

            // Set the graph dynamically
            navController.graph = navGraph

            // Connect BottomNavigationView to NavController
            bottomNavigationView?.setupWithNavController(navController)

            // Dynamic bottom nav visibility
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