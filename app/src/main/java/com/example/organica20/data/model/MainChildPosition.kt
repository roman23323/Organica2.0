package com.example.organica20.data.model

data class MainChildPosition(
    var position: Int,
    var xOffset: Int = 0,
    var yOffset: Int = 0
) {
    init {
        require(position in 0..8) { "Invalid main child position argument: expected from 0 to 8, got $position" }
    }
}