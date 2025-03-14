package dev.vstd.shoppingcart.pricecompare.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.keego.shoppingcart.databinding.ItemFilterHorizontalBinding
import dev.vstd.shoppingcart.pricecompare.retrofit.model.Filter
import dev.vstd.shoppingcart.pricecompare.retrofit.model.FilterOption
import dev.vstd.shoppingcart.pricecompare.ui.ComparingVimel
import timber.log.Timber

class ItemFilterBarAdapter(
    private val onClickFilterOption: (FilterOption) -> Unit,
    var comaparingVimel: ComparingVimel
) : RecyclerView.Adapter<ItemFilterBarAdapter.ViewHolder>() {


    private val filters: MutableList<Filter> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<Filter>) {
        filters.clear()
        filters.addAll(data)
        Timber.d("setData: ${filters.size}")
        notifyDataSetChanged()
    }


    inner class ViewHolder(private val binding: ItemFilterHorizontalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val nestedAdapter: ItemFilterOptionAdapter = ItemFilterOptionAdapter(onClickFilterOption, comaparingVimel)

        init {
            binding.itemFilter.adapter = nestedAdapter
        }

        fun bind(filter: Filter) {
            binding.apply {
                nestedAdapter.setData(filter.options)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFilterHorizontalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return filters.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(filters[position])
    }
}