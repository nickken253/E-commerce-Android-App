package com.mustfaibra.roffu.screens.home

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.api.ApiService
import com.mustfaibra.roffu.models.Brand
import com.mustfaibra.roffu.models.Category
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
import com.mustfaibra.roffu.RetrofitClient

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
    
    // Thêm các biến cho filter
    val showFilterOptions = mutableStateOf(false)
    val filterType = mutableStateOf<FilterType?>(null)
    
    // Biến cho danh sách brands và categories từ API
    val apiBrands = mutableStateListOf<Brand>()
    val apiCategories = mutableStateListOf<Category>()
    val brandsApiState = mutableStateOf<UiState>(UiState.Idle)
    val categoriesApiState = mutableStateOf<UiState>(UiState.Idle)
    
    // Biến lưu trữ filter đã chọn
    val selectedBrandId = mutableStateOf<Int?>(null)
    val selectedCategoryId = mutableStateOf<Int?>(null)

    private var currentPage = 1
    private val pageLimit = 10
    private var totalPages = Int.MAX_VALUE // Giả định ban đầu, sẽ cập nhật từ API
    
    init {
        // Gọi API để lấy danh sách brands và categories sau khi tất cả các biến đã được khởi tạo
        viewModelScope.launch {
            Log.d("HomeViewModel", "ViewModel initialized, loading brands and categories")
            loadBrands()
            loadCategories()
        }
    }

    fun updateCurrentSelectedBrandId(index: Int) {
        currentSelectedBrandIndex.value = index
        
        // Khi chọn "Tất cả" (index = -1), ẩn filter options và reset filter
        if (index == -1) {
            filterType.value = null
            selectedBrandId.value = null
            selectedCategoryId.value = null
            // Đảm bảo lấy lại toàn bộ sản phẩm
            resetAndLoadAllProducts()
        }
    }

    fun updateSearchInputValue(value: String) {
        this.searchQuery.value = value
    }
    
    // Phương thức để hiển thị/ẩn filter options
    fun toggleFilterOptions() {
        showFilterOptions.value = !showFilterOptions.value
    }
    
    // Phương thức để reset và tải lại toàn bộ sản phẩm
    private fun resetAndLoadAllProducts() {
        // Xóa danh sách sản phẩm hiện tại
        allProducts.clear()
        // Reset trang về 1
        currentPage = 1
        // Tải lại toàn bộ sản phẩm
        getAllProducts()
    }
    
    // Phương thức để chọn loại filter (Brand hoặc Category)
    fun selectFilterType(type: FilterType?) {
        filterType.value = type
        if (type == FilterType.BRAND) {
            // Luôn gọi loadBrands() khi chọn filter type là BRAND để đảm bảo dữ liệu mới nhất
            loadBrands()
        } else if (type == FilterType.CATEGORY) {
            // Luôn gọi loadCategories() khi chọn filter type là CATEGORY để đảm bảo dữ liệu mới nhất
            loadCategories()
        }
    }
    
    // Phương thức để chọn brand
    fun selectBrand(brandId: Int?) {
        selectedBrandId.value = brandId
        selectedCategoryId.value = null
        filterProducts()
    }
    
    // Phương thức để chọn category
    fun selectCategory(categoryId: Int?) {
        selectedCategoryId.value = categoryId
        selectedBrandId.value = null
        filterProducts()
    }
    
    // Phương thức để lọc sản phẩm theo brand hoặc category
    private fun filterProducts() {
        allProductsUiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val token = UserPref.getToken(context) ?: run {
                    Log.e("HomeViewModel", "Token is null, cannot proceed with API call")
                    allProductsUiState.value = UiState.Error(error = Error.Unknown)
                    return@launch
                }
                
                // Kiểm tra token có hợp lệ không
                Log.d("HomeViewModel", "Token length: ${token.length}")
                Log.d("HomeViewModel", "Token first 10 chars: ${token.take(10)}...")
                
                Log.d("HomeViewModel", "Filtering products with brandId=${selectedBrandId.value}, categoryId=${selectedCategoryId.value}")
                
                val response = when {
                    selectedBrandId.value != null -> {
                        Log.d("HomeViewModel", "Calling API with brandId=${selectedBrandId.value}")
                        brandsRepository.getAllProducts(
                            page = 1,
                            limit = 100, // Lấy nhiều sản phẩm hơn để lọc
                            token = token,
                            brandId = selectedBrandId.value
                        )
                    }
                    selectedCategoryId.value != null -> {
                        // Sử dụng API thông thường với tham số query category_id
                        Log.d("HomeViewModel", "Calling getProductsByCategory with categoryId=${selectedCategoryId.value}")
                        selectedCategoryId.value?.let { categoryId ->
                            Log.d("HomeViewModel", "Using token: $token")
                            val result = brandsRepository.getProductsByCategory(categoryId, token)
                            Log.d("HomeViewModel", "getProductsByCategory call completed, result type: ${result.javaClass.name}")
                            result
                        } ?: run {
                            Log.d("HomeViewModel", "Category ID is null, falling back to regular API")
                            brandsRepository.getAllProducts(
                                page = 1,
                                limit = 100,
                                token = token
                            )
                        }
                    }
                    else -> {
                        Log.d("HomeViewModel", "Calling API without filters")
                        brandsRepository.getAllProducts(
                            page = 1,
                            limit = pageLimit,
                            token = token
                        )
                    }
                }
                
                when (response) {
                    is DataResponse.Success -> {
                        response.data?.let { apiResponse ->
                            Log.d("HomeViewModel", "Received API response with ${apiResponse.data.size} products")
                            if (apiResponse.data.isNotEmpty()) {
                                val firstProduct = apiResponse.data[0]
                                Log.d("HomeViewModel", "First product details in HomeViewModel: ")
                                Log.d("HomeViewModel", "  - ID: ${firstProduct.id}")
                                Log.d("HomeViewModel", "  - Name: ${firstProduct.product_name}")
                                Log.d("HomeViewModel", "  - Price: ${firstProduct.price}")
                                Log.d("HomeViewModel", "  - Images count: ${firstProduct.images.size}")
                                if (firstProduct.images.isNotEmpty()) {
                                    Log.d("HomeViewModel", "  - First image URL: ${firstProduct.images[0].image_url}")
                                }
                                
                                // Kiểm tra loại dữ liệu
                                Log.d("HomeViewModel", "  - Product class: ${firstProduct.javaClass.name}")
                                Log.d("HomeViewModel", "  - Images class: ${firstProduct.images.javaClass.name}")
                                if (firstProduct.images.isNotEmpty()) {
                                    Log.d("HomeViewModel", "  - Image item class: ${firstProduct.images[0].javaClass.name}")
                                }
                            }
                            
                            allProducts.clear()
                            allProducts.addAll(apiResponse.data)
                            Log.d("HomeViewModel", "After adding to allProducts, size: ${allProducts.size}")
                            
                            // Kiểm tra dữ liệu sau khi thêm vào allProducts
                            if (allProducts.isNotEmpty()) {
                                val firstProduct = allProducts[0]
                                Log.d("HomeViewModel", "First product in allProducts after adding: ")
                                Log.d("HomeViewModel", "  - ID: ${firstProduct.id}")
                                Log.d("HomeViewModel", "  - Name: ${firstProduct.product_name}")
                                Log.d("HomeViewModel", "  - Price: ${firstProduct.price}")
                                Log.d("HomeViewModel", "  - Images count: ${firstProduct.images.size}")
                                if (firstProduct.images.isNotEmpty()) {
                                    Log.d("HomeViewModel", "  - First image URL: ${firstProduct.images[0].image_url}")
                                }
                            }
                            
                            totalPages = apiResponse.pages
                            allProductsUiState.value = if (allProducts.isEmpty()) {
                                Log.w("HomeViewModel", "No products to display after filtering")
                                UiState.Error(error = Error.Empty)
                            } else {
                                Log.d("HomeViewModel", "Setting UI state to Success with ${allProducts.size} products")
                                UiState.Success
                            }
                            currentPage = 2
                        } ?: run {
                            Log.e("HomeViewModel", "API response data is null")
                            allProductsUiState.value = UiState.Error(error = Error.Empty)
                        }
                    }
                    is DataResponse.Error -> {
                        Log.e("HomeViewModel", "Error response from API: ${response.error}")
                        allProductsUiState.value = UiState.Error(error = response.error ?: Error.Network)
                    }
                }
            } catch (e: Exception) {
                allProductsUiState.value = UiState.Error(error = Error.Network)
            }
        }
    }
    
    // Phương thức để tải danh sách brands từ API
    private fun loadBrands() {
        brandsApiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                // Lấy token từ UserPref
                val token = UserPref.getToken(context) ?: run {
                    // Nếu không có token, sử dụng một số dữ liệu mẫu
                    Log.d("HomeViewModel", "No token available, using sample brands data")
                    val sampleBrands = listOf(
                        Brand(1, "Apple", "Công ty công nghệ đa quốc gia của Mỹ chuyên sản xuất điện thoại, máy tính và các thiết bị điện tử"),
                        Brand(2, "Samsung", "Tập đoàn đa quốc gia của Hàn Quốc sản xuất điện thoại, thiết bị điện tử"),
                        Brand(3, "Sony", "Công ty công nghệ Trung Quốc sản xuất điện thoại, thiết bị thông minh và đồ gia dụng"),
                        Brand(4, "LG", "Tập đoàn đa quốc gia của Hàn Quốc sản xuất điện thoại, tivi và thiết bị gia dụng"),
                        Brand(5, "Xiaomi", "Công ty công nghệ Trung Quốc sản xuất điện thoại, thiết bị thông minh và đồ gia dụng")
                    )
                    apiBrands.clear()
                    apiBrands.addAll(sampleBrands)
                    brandsApiState.value = UiState.Success
                    return@launch
                }
                
                Log.d("HomeViewModel", "Calling getBrands API with token...")
                val brands = RetrofitClient.apiService.getBrands("Bearer $token")
                Log.d("HomeViewModel", "Received ${brands.size} brands from API")
                apiBrands.clear()
                apiBrands.addAll(brands)
                brandsApiState.value = UiState.Success
                
                // Debug thông tin brands
                brands.forEach { brand ->
                    Log.d("HomeViewModel", "Brand: id=${brand.id}, name=${brand.name}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading brands", e)
                // Sử dụng dữ liệu mẫu khi gặp lỗi
                val sampleBrands = listOf(
                    Brand(1, "Apple", "Công ty công nghệ đa quốc gia của Mỹ chuyên sản xuất điện thoại, máy tính và các thiết bị điện tử"),
                    Brand(2, "Samsung", "Tập đoàn đa quốc gia của Hàn Quốc sản xuất điện thoại, thiết bị điện tử"),
                    Brand(3, "Sony", "Công ty công nghệ Trung Quốc sản xuất điện thoại, thiết bị thông minh và đồ gia dụng"),
                    Brand(4, "LG", "Tập đoàn đa quốc gia của Hàn Quốc sản xuất điện thoại, tivi và thiết bị gia dụng"),
                    Brand(5, "Xiaomi", "Công ty công nghệ Trung Quốc sản xuất điện thoại, thiết bị thông minh và đồ gia dụng")
                )
                apiBrands.clear()
                apiBrands.addAll(sampleBrands)
                brandsApiState.value = UiState.Success
            }
        }
    }
    
    // Phương thức để tải danh sách categories từ API
    private fun loadCategories() {
        categoriesApiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                // Lấy token từ UserPref
                val token = UserPref.getToken(context) ?: run {
                    // Nếu không có token, sử dụng một số dữ liệu mẫu
                    Log.d("HomeViewModel", "No token available, using sample categories data")
                    val sampleCategories = listOf(
                        Category(1, "Sách, báo & tạp chí", "Sản phẩm chứa sách báo và tạp chí"),
                        Category(2, "Thời trang", "Quần áo, giày dép và phụ kiện thời trang"),
                        Category(3, "Nội thất", "Các thiết bị và vật dụng trong gia đình"),
                        Category(4, "Đồ điện tử", "Các sản phẩm điện tử, công nghệ")
                    )
                    apiCategories.clear()
                    apiCategories.addAll(sampleCategories)
                    categoriesApiState.value = UiState.Success
                    return@launch
                }
                
                Log.d("HomeViewModel", "Calling getCategories API with token...")
                val categories = RetrofitClient.apiService.getCategories("Bearer $token")
                Log.d("HomeViewModel", "Received ${categories.size} categories from API")
                apiCategories.clear()
                apiCategories.addAll(categories)
                categoriesApiState.value = UiState.Success
                
                // Debug thông tin categories
                categories.forEach { category ->
                    Log.d("HomeViewModel", "Category: id=${category.id}, name=${category.name}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading categories", e)
                // Sử dụng dữ liệu mẫu khi gặp lỗi
                val sampleCategories = listOf(
                    Category(1, "Sách, báo & tạp chí", "Sản phẩm chứa sách báo và tạp chí"),
                    Category(2, "Thời trang", "Quần áo, giày dép và phụ kiện thời trang"),
                    Category(3, "Nội thất", "Các thiết bị và vật dụng trong gia đình"),
                    Category(4, "Đồ điện tử", "Các sản phẩm điện tử, công nghệ")
                )
                apiCategories.clear()
                apiCategories.addAll(sampleCategories)
                categoriesApiState.value = UiState.Success
            }
        }
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
                
                Log.d("HomeViewModel", "Getting all products with brandId=${selectedBrandId.value}, categoryId=${selectedCategoryId.value}")

                when (val response = brandsRepository.getAllProducts(
                    page = currentPage,
                    limit = pageLimit,
                    token = token,
                    brandId = selectedBrandId.value,
                    categoryId = selectedCategoryId.value
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
                // Không cần hiển thị quảng cáo nữa
                homeAdvertisementsUiState.value = UiState.Success
            } catch (e: Exception) {
                homeAdvertisementsUiState.value = UiState.Error(error = Error.Network)
            }
        }
    }
    
    // Enum class để định nghĩa loại filter
    enum class FilterType {
        BRAND,
        CATEGORY
    }
}