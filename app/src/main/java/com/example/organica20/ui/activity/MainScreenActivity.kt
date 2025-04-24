package com.example.organica20.ui.activity

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import com.example.organica20.R
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.organica20.utils.setupEdgeToEdge
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainScreenActivity: AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_screen)
        enableEdgeToEdge()
        setupEdgeToEdge(findViewById(android.R.id.content))

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navController)
    }
}