package dev.vstd.shoppingcart.common.ui

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

object DiffUtils {
    fun <T : Any> any() = object : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T) = oldItem == newItem

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: T, newItem: T) = oldItem == newItem
    }
}