package com.example.organica20.data.model

data class FormulaData(
    val nodes: String,
    val mainChildPosition: MainChildPosition,
    val placement: String,
    val additionalLines: String? = null
)