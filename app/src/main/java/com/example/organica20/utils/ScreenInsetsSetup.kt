package com.example.organica20.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

fun setupEdgeToEdge(rootView: View) {
    ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        rootView.updatePadding(
            top = systemBars.top,
            bottom = systemBars.bottom
        )
        insets
    }
}