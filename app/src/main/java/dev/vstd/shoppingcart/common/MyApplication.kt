package dev.vstd.shoppingcart.common

import android.app.Application
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import dagger.hilt.android.HiltAndroidApp
import dev.keego.shoppingcart.R
import timber.log.Timber
import java.lang.reflect.Field

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        plantLog()
        overrideFonts()
    }

    private fun plantLog() {
        Timber.plant(object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                super.log(priority, tag, ">> $message", t)
            }
        })
    }

    private fun overrideFonts() {
        val typefaces = mutableMapOf<String?, Typeface?>(
            "sans-serif" to ResourcesCompat.getFont(this, R.font.raleway_regular),
            "sans-serif-medium" to ResourcesCompat.getFont(this, R.font.raleway_medium),
            "sans-serif-bold" to ResourcesCompat.getFont(this, R.font.raleway_bold),
            "sans-serif-light" to ResourcesCompat.getFont(this, R.font.raleway_light),
        )
        try {
            val field: Field = Typeface::class.java.getDeclaredField("sSystemFontMap")
            field.isAccessible = true
            var oldFonts = field.get(null) as MutableMap<String?, Typeface?>?
            if (oldFonts != null) {
                oldFonts.putAll(typefaces)
            } else {
                oldFonts = typefaces
            }
            field.set(null, oldFonts)
            field.isAccessible = false
        } catch (e: Exception) {
            Timber.e("Can not set custom fonts")
        }
    }
}