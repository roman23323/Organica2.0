package com.example.organica20.data.repository

import android.content.Context
import com.example.organica20.data.model.PageElement
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class PageRepository(private val context: Context) {

    fun loadPageData(fileName: String): List<PageElement> {
        val jsonString = context.assets.open(fileName).bufferedReader().use{ it.readText() }
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter<List<PageElement>>(Types.newParameterizedType(List::class.java, PageElement::class.java))
        return adapter.fromJson(jsonString) ?: emptyList()
    }
}