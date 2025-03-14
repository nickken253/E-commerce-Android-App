package dev.vstd.shoppingcart.checklist.ui.barcode

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import dagger.hilt.android.AndroidEntryPoint
import dev.keego.shoppingcart.databinding.ActivityBarcodeBinding
import dev.vstd.shoppingcart.checklist.data.BarcodeItem
import dev.vstd.shoppingcart.checklist.data.BarcodeRepository
import dev.vstd.shoppingcart.common.utils.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class BarcodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBarcodeBinding
    private val barcodeSF = MutableStateFlow<String?>("8938508475056")
    private val barcodeItemSF = MutableStateFlow<BarcodeItem?>(null)

    @Inject
    lateinit var barcodeRepo: BarcodeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeBinding.inflate(layoutInflater)

        setOnClicks()
        observeStates()
        setContentView(binding.root)
    }

    private fun observeStates() {
        lifecycleScope.launch {
            launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    barcodeSF.collect { barcode ->
                        binding.textBarcode.text = barcode ?: "Not found in database"
                        binding.btnUseThisBarcode.isEnabled = barcode != null
                        loadProductDetail(barcode)
                    }
                }
            }
            launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    barcodeItemSF.collect { barcodeItem ->
                        Timber.d("BarcodeItem changed: $barcodeItem")
                        if (barcodeItem == null) {
                            binding.productInfoContainer.beGone()
                        } else {
                            binding.productInfoContainer.beVisible()
                            binding.textProductName.text = barcodeItem.name
                            binding.textProductNote.text = barcodeItem.note
                        }
                    }
                }
            }
        }
    }

    private fun loadProductDetail(barcode: String?) {
        lifecycleScope.launch {
            if (barcode == null) {
                barcodeItemSF.value = null
                return@launch
            }
            val barcodeItem = barcodeRepo.findByBarcode(barcode)
            barcodeItemSF.value = barcodeItem
        }
    }

    private fun setOnClicks() {
        binding.apply {
            btnScanNow.setOnClickListener {
                val options = GmsBarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                        Barcode.FORMAT_ALL_FORMATS
                    )
                    .build()
                val scanner = GmsBarcodeScanning.getClient(this@BarcodeActivity, options)
                scanner.startScan()
                    .addOnSuccessListener { barcode ->
                        if (barcode.valueType != Barcode.TYPE_PRODUCT) {
                            Timber.d("Barcode not from product: value=${barcode.rawValue} valueType=${barcode.valueType}")
                            this@BarcodeActivity.toast("This barcode is not from a product")
                            return@addOnSuccessListener
                        }
                        this@BarcodeActivity.barcodeSF.value = barcode.rawValue
                    }
                    .addOnCanceledListener {
                        Timber.d("Barcode scanning canceled")
                    }
                    .addOnFailureListener { e ->
                        Timber.e(e, "Barcode scanning failed")
                    }
            }
            btnCopy.setOnClickListener {
                barcodeSF.value?.let {
                    Clipboard.copyToClipboard(this@BarcodeActivity, it)
                    Toast.makeText(this@BarcodeActivity, "Copied to clipboard", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            btnSearchInInternet.setOnClickListener {
                barcodeSF.value?.let {
                    FindBarcodeActivity.start(this@BarcodeActivity, it)
                }
            }
            appBar.setNavigationOnClickListener {
                finish()
            }
            btnUseThisBarcode.setOnClickListener {
                setResult(
                    RESULT_OK, intentOf(
                        EXTRA_BARCODE_DESCRIPTION to (barcodeItemSF.value?.name ?: "undefined")
                    )
                )
                finish()
            }
            binding.productInfoContainer.setOnClickListener {
                Toast.makeText(this@BarcodeActivity, "Product info clicked", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    companion object {
        const val RESULT_OK = 200
        const val RESULT_ERROR = 400
        const val EXTRA_BARCODE_DESCRIPTION = "barcode_description"

        fun start(context: Context) {
            Intent(context, BarcodeActivity::class.java).also {
                context.startActivity(it)
            }
        }
    }
}