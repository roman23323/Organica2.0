package com.example.organica20.data.model

import com.squareup.moshi.Json

data class PageElement(
    val type: Element,
    val content: String? = null,
    val formulaData: FormulaData? = null
)

enum class Element {
    @Json(name = "header") HEADER,
    @Json(name = "text") TEXT,
    @Json(name = "image") IMAGE,
    @Json(name = "formula") FORMULA
}