package com.mustfaibra.roffu.screens.home

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.models.Manufacturer
import com.mustfaibra.roffu.models.dto.Product
import com.mustfaibra.roffu.repositories.BrandsRepository
import com.mustfaibra.roffu.sealed.DataResponse
import com.mustfaibra.roffu.sealed.Error
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.utils.UserPref
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject
import com.mustfaibra.roffu.models.Advertisement

// Thêm lại class ApiResponse vì vẫn được sử dụng trong BrandsRepository
@Serializable
data class ApiResponse(
    val data: List<Product>,
    val page: Int,
    val limit: Int,
    val total: Int,
    val pages: Int
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val brandsRepository: BrandsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    val searchQuery = mutableStateOf("")

    // Thêm các biến cho quảng cáo
    val homeAdvertisementsUiState = mutableStateOf<UiState>(UiState.Idle)
    val advertisements = mutableStateListOf<Advertisement>()

    val brandsUiState = mutableStateOf<UiState>(UiState.Idle)
    val brands: MutableList<Manufacturer> = mutableStateListOf()

    val currentSelectedBrandIndex = mutableStateOf(-1)
    val allProductsUiState = mutableStateOf<UiState>(UiState.Idle)
    val allProducts: MutableList<Product> = mutableStateListOf()

    private var currentPage = 1
    private val pageLimit = 10
    private var totalPages = Int.MAX_VALUE // Giả định ban đầu, sẽ cập nhật từ API

    fun updateCurrentSelectedBrandId(index: Int) {
        currentSelectedBrandIndex.value = index
    }

    fun updateSearchInputValue(value: String) {
        this.searchQuery.value = value
    }

    fun getBrandsWithProducts() {
        if (brands.isNotEmpty()) return

        brandsUiState.value = UiState.Loading
        viewModelScope.launch {
            when (val response = brandsRepository.getBrandsWithProducts()) {
                is DataResponse.Success -> {
                    brandsUiState.value = UiState.Success
                    response.data?.let { responseBrands ->
                        brands.addAll(responseBrands)
                    }
                }
                is DataResponse.Error -> {
                    brandsUiState.value = UiState.Error(error = response.error ?: Error.Network)
                }
            }
        }
    }

    fun getAllProducts() {
        if (allProductsUiState.value == UiState.Loading || currentPage > totalPages) return

        allProductsUiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                // Lấy token từ UserPref
                val token = UserPref.getToken(context ) ?: run {
                    allProductsUiState.value = UiState.Error(error = Error.Unknown)
                    return@launch
                }

                when (val response = brandsRepository.getAllProducts(
                    page = currentPage,
                    limit = pageLimit,
                    token = token
                )) {
                    is DataResponse.Success -> {
                        response.data?.let { apiResponse ->
                            if (currentPage == 1) {
                                allProducts.clear()
                            }
                            allProducts.addAll(apiResponse.data)
                            totalPages = apiResponse.pages
                            allProductsUiState.value = if (allProducts.isEmpty()) {
                                UiState.Error(error = Error.Empty)
                            } else {
                                UiState.Success
                            }
                            currentPage++
                        } ?: run {
                            allProductsUiState.value = UiState.Error(error = Error.Empty)
                        }
                    }
                    is DataResponse.Error -> {
                        allProductsUiState.value = UiState.Error(error = response.error ?: Error.Network)
                    }
                }
            } catch (e: Exception) {
                allProductsUiState.value = UiState.Error(error = Error.Network)
            }
        }
    }

    fun loadMoreProducts() {
        getAllProducts()
    }

    // Thêm phương thức getHomeAdvertisements
    fun getHomeAdvertisements() {
        homeAdvertisementsUiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                // TODO: Implement API call to get advertisements
                // Tạm thời dùng dữ liệu mẫu
                val sampleAds = listOf(
                    Advertisement(1, "Ad 1", "https://example.com/ad1.jpg"),
                    Advertisement(2, "Ad 2", "https://example.com/ad2.jpg")
                )
                advertisements.clear()
                advertisements.addAll(sampleAds)
                homeAdvertisementsUiState.value = UiState.Success
            } catch (e: Exception) {
                homeAdvertisementsUiState.value = UiState.Error(error = Error.Network)
            }
        }
    }
}