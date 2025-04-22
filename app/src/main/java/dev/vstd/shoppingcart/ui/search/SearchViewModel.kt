package dev.vstd.shoppingcart.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.vstd.shoppingcart.model.*
import dev.vstd.shoppingcart.repository.SearchRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _searchFilter = MutableStateFlow(SearchFilter())
    val searchFilter = _searchFilter.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Product>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _popularCategories = MutableStateFlow<List<String>>(emptyList())
    val popularCategories = _popularCategories.asStateFlow()

    private val _popularTags = MutableStateFlow<List<String>>(emptyList())
    val popularTags = _popularTags.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var currentPage = 0
    private val pageSize = 20

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            searchRepository.getPopularCategories()
                .collect { categories ->
                    _popularCategories.value = categories
                }
        }
        viewModelScope.launch {
            searchRepository.getPopularSearchTags()
                .collect { tags ->
                    _popularTags.value = tags
                }
        }
    }

    fun search(resetPage: Boolean = true) {
        if (resetPage) {
            currentPage = 0
            _searchResults.value = emptyList()
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                searchRepository.searchProducts(_searchFilter.value, currentPage, pageSize)
                    .collect { products ->
                        if (resetPage) {
                            _searchResults.value = products
                        } else {
                            _searchResults.value = _searchResults.value + products
                        }
                        currentPage++
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateFilter(update: (SearchFilter) -> SearchFilter) {
        _searchFilter.value = update(_searchFilter.value)
    }

    fun resetFilter() {
        _searchFilter.value = SearchFilter()
        search()
    }

    fun loadMore() {
        if (!_isLoading.value) {
            search(resetPage = false)
        }
    }
} 