package com.mustfaibra.roffu.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.api.ProductApi
import com.mustfaibra.roffu.models.Product
import com.mustfaibra.roffu.models.SearchFilters
import com.mustfaibra.roffu.utils.SearchHistoryPref
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A View model with hiltViewModel annotation that is used to access this view model everywhere needed
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchHistoryPref: SearchHistoryPref,
    private val productApi: ProductApi
) : ViewModel() {
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

    private val _searchResults = MutableStateFlow<List<Product>>(emptyList())
    val searchResults: StateFlow<List<Product>> = _searchResults

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

    fun onSearch(query: String) {
        if (query.isNotEmpty()) {
            val currentHistory = _searchHistory.value.toMutableList()
            if (!currentHistory.contains(query)) {
                // Thêm vào đầu danh sách
                currentHistory.add(0, query)
                // Giới hạn lịch sử tối đa 10 mục
                if (currentHistory.size > 10) {
                    currentHistory.removeAt(currentHistory.size - 1)
                }
                _searchHistory.value = currentHistory
                // Lưu lịch sử tìm kiếm
                searchHistoryPref.saveSearchHistory(currentHistory)
            }

            // Thực hiện tìm kiếm với bộ lọc
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    _error.value = null
                    
                    val filters = currentFilters.value
                    val response = productApi.searchProducts(
                        query = query,
                        minPrice = filters.minPrice,
                        maxPrice = filters.maxPrice,
                        sortBy = filters.sortBy,
                        sortOrder = filters.sortOrder,
                        page = currentPage.value,
                        pageSize = PAGE_SIZE
                    )
                    
                    val products = response.products.map { productResponse ->
                        Product(
                            id = productResponse.id,
                            name = productResponse.product_name,
                            description = productResponse.description,
                            price = productResponse.price,
                            barcode = productResponse.barcode,
                            imagePath = productResponse.images.firstOrNull()?.image_url,
                            manufacturerId = productResponse.brand_id,
                            basicColorName = "", // Default value as it's not in response
                            image = 0 // Default value as we're using imagePath instead
                        )
                    }

                    if (currentPage.value == 1) {
                        _searchResults.value = products
                    } else {
                        _searchResults.value = _searchResults.value + products
                    }

                    _hasMoreData.value = products.size >= PAGE_SIZE

                } catch (e: Exception) {
                    _error.value = e.message ?: "Có lỗi xảy ra khi tìm kiếm"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun resetSearch() {
        _currentPage.value = 1
        _hasMoreData.value = true
        _searchResults.value = emptyList()
    }
}