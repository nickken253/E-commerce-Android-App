package com.mustfaibra.roffu.screens.search

import androidx.lifecycle.ViewModel
import com.mustfaibra.roffu.utils.SearchHistoryPref
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * A View model with hiltViewModel annotation that is used to access this view model everywhere needed
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchHistoryPref: SearchHistoryPref
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

    private val _searchHistory = MutableStateFlow<List<String>>(searchHistoryPref.getSearchHistory())
    val searchHistory: StateFlow<List<String>> = _searchHistory

    private val _searchSuggestions = MutableStateFlow<List<String>>(mockSuggestions)
    val searchSuggestions: StateFlow<List<String>> = _searchSuggestions

    fun onSearchQueryChanged(query: String) {
        // Cập nhật gợi ý tìm kiếm dựa trên query
        _searchSuggestions.value = mockSuggestions.filter { 
            it.contains(query, ignoreCase = true) 
        }
    }

    fun onSearch(query: String) {
        if (query.isNotEmpty()) {
            val currentHistory = _searchHistory.value.toMutableList()
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
    }
}