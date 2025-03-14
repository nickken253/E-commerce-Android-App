package dev.vstd.shoppingcart.shopping.ui.order.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import dev.keego.shoppingcart.databinding.ItemOrdersHeaderBinding
import dev.keego.shoppingcart.databinding.ItemOrdersProductBinding
import dev.vstd.shoppingcart.common.ui.DiffUtils
import dev.vstd.shoppingcart.shopping.domain.Order
import dev.vstd.shoppingcart.shopping.domain.ProductOfOrder
import timber.log.Timber

class OrderAdapter:
    ListAdapter<OrderAdapter.DataWrapper, OrderAdapter.ViewHolderWrapper>(DiffUtils.any<DataWrapper>()
) {

    fun submitListt(data: List<Order>) {
        val list = mutableListOf<DataWrapper>()
        data.forEach { order ->
            list.add(DataWrapper.Seller(order))
            order.products.forEach { product ->
                list.add(DataWrapper.Product(product))
            }
        }
        Timber.d("list size=${list.size}")
        submitList(list)
    }

    sealed class DataWrapper {
        data class Seller(val order: Order) : DataWrapper()
        data class Product(val productOfOrder: ProductOfOrder) : DataWrapper()
    }

    sealed class ViewHolderWrapper(binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        class SellerViewHolder(private val binding: ItemOrdersHeaderBinding) :
            ViewHolderWrapper(binding) {
            fun bind(order: Order) {
                binding.tvTitleItemLayout.text = order.sellerName
                binding.tvState.text = order.status.displayName
            }
        }

        class ProductViewHolder(private val binding: ItemOrdersProductBinding) :
            ViewHolderWrapper(binding) {
            fun bind(product: ProductOfOrder) {
                Glide.with(binding.root.context)
                    .load(product.product.image)
                    .into(binding.imageViewItemLayout)
                binding.tvNameItemLayout.text = product.product.title
                binding.tvNumber.text = product.quantity.toString()
                binding.tvPriceItemLayout.text = product.product.price.toString() + "đ"
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (currentList[position]) {
            is DataWrapper.Seller -> 1
            is DataWrapper.Product -> 2
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderWrapper {
        return when (viewType) {
            1 -> ViewHolderWrapper.SellerViewHolder(
                ItemOrdersHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            2 -> ViewHolderWrapper.ProductViewHolder(
                ItemOrdersProductBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: ViewHolderWrapper, position: Int) {
        when (val item = currentList[position]) {
            is DataWrapper.Seller -> {
                (holder as ViewHolderWrapper.SellerViewHolder).bind(item.order)
            }
            is DataWrapper.Product -> {
                (holder as ViewHolderWrapper.ProductViewHolder).bind(item.productOfOrder)
            }
        }
    }
}