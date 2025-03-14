package dev.vstd.shoppingcart.pricecompare.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.keego.shoppingcart.databinding.ItemFilterOptionBinding
import dev.vstd.shoppingcart.pricecompare.retrofit.model.FilterOption
import dev.vstd.shoppingcart.pricecompare.ui.ComparingVimel
import timber.log.Timber

class ItemFilterOptionAdapter(
    private val onClickFilter: (FilterOption) -> Unit,
    private var comparingVimel: ComparingVimel

    ) : RecyclerView.Adapter<ItemFilterOptionAdapter.ViewHolder>() {
    private val filterOptions: MutableList<FilterOption> = mutableListOf()


    @SuppressLint("NotifyDataSetChanged")
    fun setData(options: List<FilterOption>?) {
        if (options != null) {
            filterOptions.clear()
            filterOptions.addAll(options)
        }
        Timber.d("setDataFi: $filterOptions")
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemFilterOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("NotifyDataSetChanged")
        fun bind(filterOption: FilterOption) {
            binding.apply {
                val isFilterOptionSelecting = comparingVimel.isFilterSelecting(filterOption)

                binding.tvFilter.setBackgroundColor(if (isFilterOptionSelecting) Color.parseColor("#ADD8E6") else Color.WHITE)


                binding.tvFilter.text = filterOption.text
                binding.itemFilterOption.setOnClickListener() {
                    onClickFilter(filterOption)
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemFilterOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return filterOptions.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(filterOptions[position])
    }
}