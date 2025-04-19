package com.example.organica20.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.organica20.data.model.PageElement
import com.example.organica20.data.repository.PageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PageViewModel(private val repository: PageRepository) : ViewModel() {
    private val _pageElements = MutableLiveData<List<PageElement>>()
    val pageElements: LiveData<List<PageElement>> = _pageElements

    fun loadPage(fileName: String) {
        viewModelScope.launch {
            val elements = withContext(Dispatchers.IO) {
                repository.loadPageData(fileName)
            }
            _pageElements.value = elements
        }
    }
}