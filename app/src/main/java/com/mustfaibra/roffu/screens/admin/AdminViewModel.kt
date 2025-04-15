package com.mustfaibra.roffu.screens.admin

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.roffu.data.local.RoomDao
import com.mustfaibra.roffu.models.Manufacturer
import com.mustfaibra.roffu.models.Product
import com.mustfaibra.roffu.models.ProductColor
import com.mustfaibra.roffu.models.User
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.utils.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.repositories.ProductsRepository

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val roomDao: RoomDao,
    private val userSessionManager: UserSessionManager,
    private val repository: ProductsRepository
) : ViewModel() {
    val manufacturers = mutableStateListOf<Manufacturer>()
    private val _addManufacturerState = mutableStateOf<UiState>(UiState.Idle)
    val addManufacturerState: State<UiState> = _addManufacturerState
    init {
        getManufacturers()
    }
    private fun getManufacturers() {
        viewModelScope.launch {
            repository.getManufacturers().collect { manufacturersList ->
                manufacturers.clear()
                manufacturers.addAll(manufacturersList)
            }
        }
    }
    // Danh sách người dùng
    val users: StateFlow<List<User>> = roomDao.getAllUsers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    fun addUser(user: User, onResult: (String, Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                roomDao.saveUser(user)
                onResult("✅ Thêm người dùng thành công!", true)
            } catch (e: Exception) {
                onResult("❌ Lỗi: ${e.message}", false)
            }
        }
    }

    fun updateUser(user: User, onResult: (String, Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                roomDao.updateUser(user)
                onResult("✅ Cập nhật người dùng thành công!", true)
            } catch (e: Exception) {
                onResult("❌ Lỗi: ${e.message}", false)
            }
        }
    }

    fun deleteUser(user: User, onResult: (String, Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                roomDao.deleteUser(user)
                onResult("✅ Delete user successful!", true)
            } catch (e: Exception) {
                onResult("❌ Lỗi: ${e.message}", false)
            }
        }
    }
    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            userSessionManager.logout()
            onLoggedOut()
        }
    }
    // Danh sách sản phẩm
    val products: StateFlow<List<Product>> = roomDao.getAllProducts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    fun addProduct(product: Product, onResult: (String, Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                roomDao.insertProduct(product)
                onResult("✅ Thêm sản phẩm thành công!", true)
            } catch (e: Exception) {
                onResult("❌ Lỗi khi thêm sản phẩm: ${e.message}", false)
            }
        }
    }
    fun addManufacturer(name: String, onResult: (String, Boolean) -> Unit) {
        if (name.isBlank()) {
            onResult("Tên nhà sản xuất không được để trống", false)
            return
        }
        viewModelScope.launch {
            _addManufacturerState.value = UiState.Loading
            try {

                val newManufacturer = Manufacturer(
                    id = (manufacturers.maxOfOrNull { it.id } ?: 0) + 1, // Tạo id mới
                    name = name,
                    icon = R.drawable.ic_manufacturer_default
                )
                repository.addManufacturer(newManufacturer)
                _addManufacturerState.value = UiState.Success
                onResult("Thêm nhà sản xuất thành công", true)
            } catch (e: Exception) {
                onResult("Lỗi khi thêm nhà sản xuất: ${e.message}", false)
            }
        }
    }

    fun updateProduct(product: Product, onResult: (String, Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                roomDao.updateProduct(product)
                onResult("✅ Cập nhật sản phẩm thành công!", true)
            } catch (e: Exception) {
                onResult("❌ Lỗi khi cập nhật sản phẩm: ${e.message}", false)
            }
        }
    }

    fun deleteProduct(product: Product, onResult: (String, Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                roomDao.deleteProduct(product)
                onResult("✅ Delete product successful!", true)
            } catch (e: Exception) {
                onResult("❌ Lỗi khi xóa sản phẩm: ${e.message}", false)
            }
        }
    }
    fun getProductById(productId: Int, onResult: (Product?) -> Unit) {
        viewModelScope.launch {
            try {
                val product = roomDao.getProductById(productId)
                onResult(product)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }




}
