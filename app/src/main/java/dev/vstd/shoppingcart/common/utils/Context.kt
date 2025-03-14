package dev.vstd.shoppingcart.common.utils

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast

fun Context.toast(message: String) {
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

fun intentOf(vararg data: Pair<String, String>) = Intent().apply {
    data.forEach { (key, value) -> putExtra(key, value) }
}