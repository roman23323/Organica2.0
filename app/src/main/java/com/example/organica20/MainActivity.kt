package com.example.organica20

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.organica20.ui.activity.MainScreenActivity
import com.example.organica20.utils.setupEdgeToEdge

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val rootView = findViewById<View>(android.R.id.content)
        setupEdgeToEdge(rootView)

        val intent = Intent(this, MainScreenActivity::class.java)
        startActivity(intent)
    }
}