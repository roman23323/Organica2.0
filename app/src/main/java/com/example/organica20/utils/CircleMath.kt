package com.example.organica20.utils

import kotlin.math.pow

fun getCircumcircleRadius(width: Int, height: Int): Float {
    return (width.toFloat().pow(2) + height.toFloat().pow(2)).pow(0.5f).div(2)
}