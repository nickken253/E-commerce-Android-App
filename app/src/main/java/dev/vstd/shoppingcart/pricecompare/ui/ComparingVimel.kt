package dev.vstd.shoppingcart.pricecompare.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vstd.shoppingcart.pricecompare.retrofit.model.Filter
import dev.vstd.shoppingcart.pricecompare.retrofit.model.FilterOption
import dev.vstd.shoppingcart.pricecompare.retrofit.model.SerpProduct
import dev.vstd.shoppingcart.pricecompare.retrofit.model.SerpResult
import dev.vstd.shoppingcart.pricecompare.retrofit.model.ShoppingResult
import dev.vstd.shoppingcart.pricecompare.retrofit.repository.ProductRepository
import dev.vstd.shoppingcart.pricecompare.retrofit.service.ProductService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ComparingVimel @Inject constructor(okHttpClient: OkHttpClient): ViewModel() {
    private val productSerpRepo = ProductRepository(ProductService.build(okHttpClient))
    val products = MutableStateFlow(listOf<ShoppingResult>())
    var serpProductResult: SerpProduct? = null
    val serpResult: MutableSharedFlow<SerpResult?> = MutableSharedFlow()
    val serpResultFilter: MutableSharedFlow<SerpResult?> = MutableSharedFlow()


    val filters = MutableStateFlow(listOf<Filter>())
    val selectedFilters = MutableStateFlow(listOf<Filter>())

    fun isFilterSelecting(filterOption: FilterOption) : Boolean {
        for (filter in selectedFilters.value) {
            if (filter.options.contains(filterOption)) {
                return true
            }
        }
        return false
    }

    fun createFilterData() : List<Filter> {
        val list = mutableListOf<Filter>()
        for (filter in selectedFilters.value) {
            if (filter.options.isEmpty()) {
                list.add(Filter(filter.type,
                    filters.value.find { it.type == filter.type }?.options ?: listOf()))

            }
            else {
                list.add(filter)
            }
        }

        return list
    }

    fun selectFilter(filterOption: FilterOption) {
        val type = getFilterType(filterOption)
        for (filter in selectedFilters.value) {
            if (filter.type == type) {
                val list = filter.options.toMutableList()
                if (list.contains(filterOption)) {
                    list.clear()
                }
                else {
                    list.clear()
                    list.add(filterOption)
                }
                filter.options = list
            }
        }
    }


    private fun getFilterType(filterOption: FilterOption) : String {
        for (filter in filters.value) {
            for (option in filter.options) {
                if (option == filterOption) {
                    return filter.type
                }
            }
        }
        return ""
    }

    private fun createFilterString() : String {
        var filterString = ""
        for (filter in selectedFilters.value) {
            if (filter.options.isNotEmpty()) {
                for (option in filter.options) {
                    filterString += option.tbs + ","
                }
            }
        }
        if (filterString.isEmpty()) {
            return "shop"
        }
        return filterString.slice(0 until filterString.length - 1)
    }

    private fun createSelectedFilters() {
        val list = selectedFilters.value.toMutableList()
        for (filter in filters.value) {
            list.add(Filter(filter.type, listOf()))
        }
        selectedFilters.value = list
    }

    fun searchProduct(productName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            productSerpRepo.search(productName).let {
                if (it.isSuccessful) {
                    Timber.d("Search product completed!" + it.body())
                    serpResult.emit(it.body()!!)
                    filters.value = it.body()!!.filters
                    createSelectedFilters()
                    products.value = it.body()!!.shoppingResults
                }
                else {
                    Timber.d("Search product failed!")
                }
            }
        }
    }

    fun searchProductWithFilter(serpResult: SerpResult?) {
        val productName = serpResult?.searchParameters?.q
        val filter = createFilterString()
        timber.log.Timber.d("Filter: $filter $productName" )
//        return
        viewModelScope.launch(Dispatchers.IO) {
            if (productName != null) {
                productSerpRepo.searchWithFilter(productName, filter).let {
                    if (it.isSuccessful) {
                        Timber.d("Search product with filter completed!" + it.body())
                        serpResultFilter.emit(it.body())
                    } else {
                        Timber.d("Search product with filter failed!")
                    }

                }
            }
        }
    }

    fun getSerpProduct(jsonRequest: String, callback: SerpProductCallback) {
        viewModelScope.launch(Dispatchers.IO) {
            productSerpRepo.getSerpProduct(jsonRequest).let {
                if (it.isSuccessful) {
                    Timber.d("Get seller completed!" + it.body())
                    serpProductResult = it.body()!!
                    callback(serpProductResult!!)
                }
                else {
                    Timber.d("Get seller failed!")
                }
            }
        }
    }

}

typealias SerpProductCallback = (SerpProduct) -> Unit