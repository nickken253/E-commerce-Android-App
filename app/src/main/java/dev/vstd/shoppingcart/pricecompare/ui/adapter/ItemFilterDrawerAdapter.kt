package dev.vstd.shoppingcart.pricecompare.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.keego.shoppingcart.databinding.ItemFilterHorizontalBinding
import dev.keego.shoppingcart.databinding.ItemFilterVerticalBinding
import dev.vstd.shoppingcart.pricecompare.retrofit.model.Filter
import dev.vstd.shoppingcart.pricecompare.retrofit.model.FilterOption
import dev.vstd.shoppingcart.pricecompare.ui.ComparingVimel
import timber.log.Timber

class ItemFilterDrawerAdapter(
    private val onClickFilterOption: (FilterOption) -> Unit,
    var comaparingVimel: ComparingVimel
) :
    RecyclerView.Adapter<ItemFilterDrawerAdapter.ViewHolder>() {
    private val filters: MutableList<Filter> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<Filter>) {
        Timber.d("setData: $data")
        filters.clear()
        filters.addAll(data)
        notifyDataSetChanged()
    }


    inner class ViewHolder(private val binding: ItemFilterVerticalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val nestedAdapter: ItemFilterOptionAdapter = ItemFilterOptionAdapter(onClickFilterOption, comaparingVimel)


        init {
            binding.itemFilter.adapter = nestedAdapter
        }

        fun bind(filter: Filter) {
            binding.apply {
                binding.tvFilterType.text = filter.type
                nestedAdapter.setData(filter.options)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFilterVerticalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return filters.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(filters[position])
    }
}