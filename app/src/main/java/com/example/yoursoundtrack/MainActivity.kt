package com.example.yoursoundtrack

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.yoursoundtrack.managers.DataLoader
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

            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

            // determine initial destination based on if we logged in on this phone or not
            if (FirebaseAuthManager.isUserLoggedIn()) {
                navGraph.setStartDestination(R.id.navigation_home)
            } else {
                navGraph.setStartDestination(R.id.navigation_auth)
            }

            navController.graph = navGraph
            bottomNavigationView?.setupWithNavController(navController)
            
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