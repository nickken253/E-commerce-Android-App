package dev.vstd.shoppingcart.setting

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import dev.keego.shoppingcart.R
import dev.keego.shoppingcart.databinding.ViewSettingItemBinding

class SettingItemView(context: Context, attrs: AttributeSet): ConstraintLayout(context, attrs) {
    init {
        val binding = ViewSettingItemBinding.inflate(LayoutInflater.from(context), this)
        binding.root.setBackgroundColor(Color.WHITE)
        context.obtainStyledAttributes(attrs, R.styleable.SettingItemView).use {
            val icon = it.getDrawable(R.styleable.SettingItemView_icon)
            val title = it.getString(R.styleable.SettingItemView_title)

            if (icon != null) {
                binding.ivIcon.setImageDrawable(icon)
            }
            if (title != null) {
                binding.tvTitle.text = title
            }
        }
    }
}