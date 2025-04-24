package com.example.organica20.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

fun setupEdgeToEdge(rootView: View, top: Boolean = true, bottom: Boolean = true) {
    ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        rootView.updatePadding(
            top = if (top) systemBars.top else 0,
            bottom = if (bottom) systemBars.bottom else 0
        )
        insets
    }
}