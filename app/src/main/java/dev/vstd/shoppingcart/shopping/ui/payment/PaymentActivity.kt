package dev.vstd.shoppingcart.shopping.ui.payment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.keego.shoppingcart.R

@AndroidEntryPoint
class PaymentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, PaymentActivity::class.java)
            context.startActivity(intent)
        }
    }
}