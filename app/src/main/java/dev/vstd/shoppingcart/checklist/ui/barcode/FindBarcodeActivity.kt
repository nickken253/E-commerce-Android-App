package dev.vstd.shoppingcart.checklist.ui.barcode

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import dev.keego.shoppingcart.databinding.ActivityFindBarcodeBinding
import dev.keego.shoppingcart.databinding.BotSheetBarcodeSearchBinding
import dev.vstd.shoppingcart.checklist.data.BarcodeItem
import dev.vstd.shoppingcart.checklist.data.BarcodeRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class FindBarcodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFindBarcodeBinding
    private lateinit var barcode: String

    @Inject
    lateinit var barcodeRepo: BarcodeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindBarcodeBinding.inflate(layoutInflater)

        if (intent?.getStringExtra(EXTRA_BARCODE) == null) {
            Toast.makeText(this, "Barcode not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        barcode = intent.getStringExtra(EXTRA_BARCODE)!!

        loadWebView()
        setOnClicks()
        setContentView(binding.root)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebView() {
//        if (WebViewFeature.isFeatureSupported(WebViewFeature.GET_WEB_CHROME_CLIENT)) {
//            val client = WebViewCompat.getWebChromeClient(binding.webView)
//        } else if (WebViewFeature.isFeatureSupported(WebViewFeature.GET_WEB_VIEW_CLIENT)) {
//            val client = WebViewCompat.getWebViewClient(binding.webView)
//        } else {
//            Toast.makeText(this, "WebView not supported", Toast.LENGTH_SHORT).show()
//        }
        binding.webView.apply {
            settings.javaScriptEnabled = true
            loadUrl("https://www.google.com/search?q=$barcode")
            webViewClient = object: WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    binding.webView.loadUrl(url!!)
                    return true
                }
            }
        }
    }

    private fun setOnClicks() {
        binding.btnClose.setOnClickListener {
            finish()
        }
        binding.btnBack.setOnClickListener {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            } else {
                Toast.makeText(this, "Can not go back", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnShowSaveBotSheet.setOnClickListener {
            AddBarcodeBottomSheet { name, note ->
                val barcodeItem = BarcodeItem(barcode = barcode, name = name, note = note)
                Timber.d("Insert BarcodeItem to database: $barcodeItem")
                lifecycleScope.launch {
                    barcodeRepo.insert(barcodeItem)
                    finish()
                }
            }.show(supportFragmentManager, "add_barcode")
        }
    }

    companion object {
        const val EXTRA_BARCODE = "barcode"
        fun start(context: Context, barcode: String) {
            val intent = Intent(context, FindBarcodeActivity::class.java)
            intent.apply {
                putExtra(EXTRA_BARCODE, barcode)
            }
            context.startActivity(intent)
        }
    }

    class AddBarcodeBottomSheet(private val onSave: (String, String) -> Unit) :
        BottomSheetDialogFragment() {
        private lateinit var binding: BotSheetBarcodeSearchBinding

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View {
            binding = BotSheetBarcodeSearchBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            binding.apply {
                btnSave.setOnClickListener {
                    val name = etName.text.toString()
                    val note = etNote.text.toString()
                    onSave(name, note)
                }
            }
        }
    }
}