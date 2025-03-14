package dev.vstd.shoppingcart.pricecompare.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import dev.keego.shoppingcart.databinding.ItemFilterHorizontalBinding
import dev.keego.shoppingcart.databinding.ItemImageDetailBinding
import dev.vstd.shoppingcart.pricecompare.retrofit.model.Media
import timber.log.Timber


class DetailViewPagerAdapter(
) : RecyclerView.Adapter<DetailViewPagerAdapter.ViewHolder>() {

    private val medias: MutableList<Media> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(newList: List<Media>?) {
        if (newList == null) return
        medias.clear()
        medias.addAll(newList)
        Timber.d("Set image list: $newList")
        notifyDataSetChanged()
    }


    inner class ViewHolder(val binding: ItemImageDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(imageUrl: String) {
            Glide.with(binding.root.context)
                .load(imageUrl)
                .into(binding.ivImageProduct)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemImageDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return medias.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(medias[position].link)
    }
}
