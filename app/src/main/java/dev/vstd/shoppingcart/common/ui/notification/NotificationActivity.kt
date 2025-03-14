package dev.vstd.shoppingcart.common.ui.notification

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import dev.keego.shoppingcart.R

class NotificationActivity : AppCompatActivity() {
    private lateinit var newRecyclerView: RecyclerView
    private lateinit var newArrayList: MutableList<NewNotification>
    lateinit var imageId: List<Int>
    lateinit var heading: List<String>
    lateinit var content: List<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_notification)
        InitData()
    }

    private fun InitData() {
        imageId = listOf(
            R.drawable.ic_book,
            R.drawable.ic_mes,
            R.drawable.ic_gift,
            R.drawable.ic_price
        )
        heading = listOf(
            "Live and Video",
            "Message",
            "Award shop",
            "Promotion"
        )
        content = listOf(
            "Live Shop",
            "Not paid full",
            "Spin to win",
            "All products are reduced by 50000"
        )
        newRecyclerView = findViewById(R.id.recycleView)
        newRecyclerView.setHasFixedSize(true)
        newArrayList = mutableListOf()
        getData()
    }
    private fun getData(){
        for(i in imageId.indices){
            val news= NewNotification(imageId[i],heading[i],content[i])
            newArrayList.add(news)
        }
        newRecyclerView.adapter= MyNotificationAdapter(newArrayList)
    }
}