package com.mustfaibra.roffu.screens.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.api.ApiService
import com.mustfaibra.roffu.api.RetrofitClient
import com.mustfaibra.roffu.models.Brand
import com.mustfaibra.roffu.models.Category
import com.mustfaibra.roffu.models.ProductResponse
import com.mustfaibra.roffu.models.SearchFilters
import com.mustfaibra.roffu.utils.SearchHistoryPref
import com.mustfaibra.roffu.utils.UserPref
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context

/**
 * A View model with hiltViewModel annotation that is used to access this view model everywhere needed
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchHistoryPref: SearchHistoryPref,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val userPref = UserPref.getToken(context)

    // Mock data cho gợi ý tìm kiếm
    private val mockSuggestions = listOf(
        "Giày thể thao nam",
        "Giày thể thao nữ",
        "Giày chạy bộ nam",
        "Giày chạy bộ nữ",
        "Giày bóng đá",
        "Giày bóng rổ",
        "Giày tennis",
        "Giày cầu lông",
        "Giày bóng chuyền",
        "Giày đá banh"
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchHistory = MutableStateFlow<List<String>>(searchHistoryPref.getSearchHistory())
    val searchHistory: StateFlow<List<String>> = _searchHistory

    private val _searchSuggestions = MutableStateFlow<List<String>>(mockSuggestions)
    val searchSuggestions: StateFlow<List<String>> = _searchSuggestions

    private val _searchResults = MutableStateFlow<List<ProductResponse>>(emptyList())
    val searchResults: StateFlow<List<ProductResponse>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _showFilters = MutableStateFlow(false)
    val showFilters: StateFlow<Boolean> = _showFilters

    private val _currentFilters = MutableStateFlow(SearchFilters())
    val currentFilters: StateFlow<SearchFilters> = _currentFilters

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage

    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData

    private val PAGE_SIZE = 20

    private val _brands = MutableStateFlow<List<Brand>>(emptyList())
    val brands: StateFlow<List<Brand>> = _brands

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    init {
        // Debounce search query changes
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // 300ms debounce
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotEmpty()) {
                        onSearch(query)
                    }
                }
        }
        loadBrandsAndCategories()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        // Cập nhật gợi ý tìm kiếm dựa trên query
        _searchSuggestions.value = mockSuggestions.filter { 
            it.contains(query, ignoreCase = true) 
        }
    }

    fun toggleFilters() {
        _showFilters.value = !_showFilters.value
    }

    fun updateFilters(filters: SearchFilters) {
        _currentFilters.value = filters
    }

    fun loadNextPage() {
        if (hasMoreData.value && !isLoading.value) {
            _currentPage.value++
            onSearch(_searchQuery.value)
        }
    }

    fun resetSearch() {
        _currentPage.value = 1
        _hasMoreData.value = true
        _searchResults.value = emptyList()
    }

    private fun loadBrandsAndCategories() {
        viewModelScope.launch {
            try {
                ApiService.init(context)
                _brands.value = RetrofitClient.apiService.getBrands()
                _categories.value = RetrofitClient.apiService.getCategories()
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error loading brands/categories", e)
            }
        }
    }

    fun onSearch(query: String) {
        if (query.isNotEmpty()) {
            val currentHistory = _searchHistory.value.toMutableList()
            if (!currentHistory.contains(query)) {
                currentHistory.add(0, query)
                if (currentHistory.size > 10) {
                    currentHistory.removeAt(currentHistory.size - 1)
                }
                _searchHistory.value = currentHistory
                searchHistoryPref.saveSearchHistory(currentHistory)
            }

            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    _error.value = null
                    
                    ApiService.init(context)
                    
                    val filters = currentFilters.value
                    val response = RetrofitClient.apiService.searchProducts(
                        search = query,
                        skip = (currentPage.value - 1) * PAGE_SIZE,
                        limit = PAGE_SIZE,
                        categoryId = filters.categoryId,
                        brandId = filters.brandId,
                        minPrice = filters.minPrice,
                        maxPrice = filters.maxPrice,
                        sortBy = filters.sortBy,
                        sortOrder = filters.sortOrder,
                        page = currentPage.value,
                        pageSize = PAGE_SIZE
                    )
                    
                    Log.d("DEBUG", "Search results: ${response.products}")
                    
                    if (currentPage.value == 1) {
                        _searchResults.value = response.products
                    } else {
                        _searchResults.value = _searchResults.value + response.products
                    }

                    _hasMoreData.value = response.page < response.pages

                } catch (e: retrofit2.HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("SearchAPIError", "HTTP Code: ${e.code()}, Error Body: ${errorBody}")
                    _error.value = "Lỗi ${e.code()}: ${errorBody ?: e.message()}"
                } catch (e: Exception) {
                    Log.e("SearchAPIError", "Generic Exception: ${e.message}", e)
                    _error.value = e.message ?: "Có lỗi xảy ra khi tìm kiếm"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }
}