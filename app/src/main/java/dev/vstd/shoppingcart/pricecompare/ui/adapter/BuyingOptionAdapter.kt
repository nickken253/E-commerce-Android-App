package dev.vstd.shoppingcart.pricecompare.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.keego.shoppingcart.databinding.ItemBuyingOptionBinding
import dev.vstd.shoppingcart.pricecompare.data.model.SellerInfo
import dev.vstd.shoppingcart.pricecompare.retrofit.model.OnlineSeller
import dev.vstd.shoppingcart.pricecompare.retrofit.model.ShoppingResult

class BuyingOptionAdapter(
    private val onClickVisit: (OnlineSeller) -> Unit
) : RecyclerView.Adapter<BuyingOptionAdapter.ViewHolder>() {
    private val onlineSeller: MutableList<OnlineSeller> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(seller: List<OnlineSeller>?) {
        if (seller == null) return
        onlineSeller.clear()
        onlineSeller.addAll(seller)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemBuyingOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(seller: OnlineSeller) {
            binding.apply {

                binding.tvPrice.text = seller.basePrice
                binding.tvSource.text = seller.name
                binding.tvDelivery.text = seller.detailsAndOffers[0].text

                binding.tvOldPrice.text = seller.originalPrice
                binding.tvOldPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvOldPrice.visibility = if (seller.originalPrice.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE

                binding.btnVisitSite.setOnClickListener {
                    onClickVisit(seller)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemBuyingOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return onlineSeller.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(onlineSeller[position])
    }
}