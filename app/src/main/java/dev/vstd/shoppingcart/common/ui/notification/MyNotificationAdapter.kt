package dev.vstd.shoppingcart.common.ui.notification

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import dev.keego.shoppingcart.R

class MyNotificationAdapter(private var newList: List<NewNotification>) :
    RecyclerView.Adapter<MyNotificationAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_notification, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return newList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = newList[position]
        holder.titleImage.setImageResource(currentItem.titleImage)
        holder.tvHeading.text = currentItem.heading
        holder.tvContent.text = currentItem.content
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleImage: ShapeableImageView = itemView.findViewById(R.id.title_image)
        var tvHeading: TextView = itemView.findViewById(R.id.tvHeading)
        var tvContent: TextView = itemView.findViewById(R.id.tvContent)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<NewNotification>) {
        this.newList = newList
        notifyDataSetChanged()
    }
}