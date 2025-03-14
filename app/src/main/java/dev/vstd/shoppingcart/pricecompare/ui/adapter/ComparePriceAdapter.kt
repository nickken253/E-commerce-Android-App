package dev.vstd.shoppingcart.pricecompare.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.keego.shoppingcart.databinding.ItemViewBinding
import dev.vstd.shoppingcart.pricecompare.data.model.ComparingProduct
import dev.vstd.shoppingcart.pricecompare.retrofit.model.ShoppingResult
import dev.vstd.shoppingcart.pricecompare.toVietnameseCurrencyFormat

class ComparePriceAdapter(
    private val onClick: (ShoppingResult) -> Unit
) : RecyclerView.Adapter<ComparePriceAdapter.MyViewHolder>() {
    private val products: MutableList<ShoppingResult> = mutableListOf()

    inner class MyViewHolder(private val binding: ItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ShoppingResult) {
            binding.apply {
                Glide.with(itemView).load(product.thumbnail).into(binding.ivProduct)
                binding.tvTitle.text = product.title
                binding.tvPrice.text = product.extractedPrice.toVietnameseCurrencyFormat()
                binding.tvSource.text = product.source
                binding.tvDelivery.text = product.delivery
                binding.reviews.text = product.reviews?.toString() ?: "0"
                binding.rating.rating = product.rating?.toFloat() ?: 0f
                binding.ratingText.text = product.rating?.toString() ?: "0"

                binding.tvOldPrice.text = product.extractedOldPrice?.toVietnameseCurrencyFormat() ?: ""
                binding.tvOldPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvOldPrice.visibility = if (product.oldPrice.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE

                binding.comparisonsShop.visibility = if (product.numberOfComparisons != null) View.VISIBLE else View.GONE
                binding.comparisonsShopNumber.text = "So sánh giá của ${product.numberOfComparisons} cửa hàng khác"

                binding.root.setOnClickListener {
                    onClick(product)
                }
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun setData(newList: List<ShoppingResult>) {
        this.products.clear()
        this.products.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
//        return products.
        return products.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//        holder.bind(products[position])
        holder.bind(products[position])
    }
}