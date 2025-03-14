package dev.vstd.shoppingcart.shopping.ui.payment.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import coil.compose.AsyncImage
import dev.keego.shoppingcart.R
import dev.vstd.shoppingcart.common.theme.startPadding
import dev.vstd.shoppingcart.common.utils.toast
import dev.vstd.shoppingcart.shopping.domain.PaymentMethod
import dev.vstd.shoppingcart.shopping.domain.Product

@Composable
fun checkout_(navController: NavController, vimel: CheckoutVimel, onBack: () -> Unit) {
    val address by vimel.address.collectAsState()
    val products by vimel.products.collectAsState()
    val paymentMethod by vimel.paymentMethod.collectAsState()
    val ableToPurchase by vimel.ableToPurchase.collectAsState(false)
    val shipFee by vimel.shipFee.collectAsState()
    var productPrice by remember { mutableLongStateOf(0) }
    val context = LocalContext.current

    LaunchedEffect(products) {
        productPrice = products.fold(0L) { sum, it -> sum + it.price }
    }

    LaunchedEffect(true) {
        vimel.fetch()
    }

    Scaffold(topBar = topBar(onBack = onBack)) { paddingValues ->
        Box(
            Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(bottom = 64.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                _shipping_address(
                    highlight = address == null,
                    text = address ?: stringResource(R.string.no_ship_address_available)
                ) {
                    navController.navigate(R.id.action_checkoutFragment_to_updateAddressFragment)
                }

                _purchasing_list(products = products)

                _shipping_option()

                _voucher_and_purchase_option(paymentMethod) {
                    navController.navigate(R.id.action_checkoutFragment_to_selectPaymentMethodFragment)
                }

                _total_totalmary(
                    "$productPrice đ",
                    "$shipFee đ",
                    "${productPrice + shipFee} đ"
                )
            }
            _cta(
                Modifier
                    .height(64.dp)
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                total = productPrice + shipFee,
                enabled = ableToPurchase,
                onClickCta = {
                    vimel.createOrder(
                        onError = context::toast
                    ) { type, orderId ->
                        when (type) {
                            PaymentMethod.Type.COD -> navController.navigate(R.id.action_checkoutFragment_to_successMakeOrderFragment)
                            PaymentMethod.Type.CREDIT_CARD -> navController.navigate(
                                R.id.action_checkoutFragment_to_askForCreditCardCredentialFragment,
                                bundleOf("orderId" to orderId)
                            )

                            PaymentMethod.Type.MOMO -> throw IllegalStateException("Momo is not supported")
                        }
                    }
                })
        }
    }
}

@Composable
private fun _total_totalmary(tienHang: String, tienVanChuyen: String, tongThanhToan: String) {
    Column(Modifier.padding(startPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.EventNote, contentDescription = null)
            Text(text = "Chi tiet thanh toan", modifier = Modifier.padding(start = 8.dp))
        }
        Row {
            Text(text = "Tổng tiền hàng")
            Spacer(Modifier.weight(1f))
            Text(text = tienHang)
        }
        Row {
            Text(text = "Phí vận chuyển")
            Spacer(Modifier.weight(1f))
            Text(text = tienVanChuyen)
        }
        Row {
            Text(text = "Tổng thanh toán")
            Spacer(Modifier.weight(1f))
            Text(
                text = tongThanhToan,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun _voucher_and_purchase_option(paymentMethod: PaymentMethod, onClick: () -> Unit) {
    Row(
        Modifier
            .clickable(onClick = onClick)
            .padding(startPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Rounded.MonetizationOn, contentDescription = null)
        Text(text = "Phương thức thanh toán", modifier = Modifier.padding(start = 8.dp))
        Spacer(Modifier.weight(1f))
        Text(text = paymentMethod.type.title)
        Icon(Icons.Default.ArrowForwardIos, null, tint = Color.LightGray)
    }
}

@Composable
private fun _shipping_option() {

}

@Composable
private fun _purchasing_list(products: List<Product>) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(startPadding)
        ) {
            Text(
                text = "Yêu thích",
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(vertical = 2.dp, horizontal = 4.dp),
                color = Color.White
            )

            Text(
                text = "Trendfronts.vn",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column {
            for (product in products) {
                Row(
                    modifier = Modifier
                        .background(Color.LightGray)
                        .padding(12.dp)
                ) {
                    AsyncImage(
                        model = product.image,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp)
                    )
                    Column(Modifier.padding(start = 12.dp)) {
                        Text(text = product.title)
                        Text(text = product.description)
                        Row {
                            Text(text = "${product.price} đ")
                            Spacer(modifier = Modifier.weight(1f))
                            Text(text = "TODO" /*TODO*/)
                        }
                    }
                }
                Divider()
            }
        }
    }
}

@Composable
private fun _shipping_address(
    highlight: Boolean,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(startPadding),
    ) {
        Icon(
            imageVector = Icons.Default.PinDrop,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Column(Modifier.padding(start = 8.dp)) {
            Text(text = "Địa chỉ nhận hàng")
            Text(text = text, color = if (highlight) Color.Red else Color.Black)
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            Icons.Default.ArrowForwardIos,
            null,
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterVertically)
        )
    }
}

@Composable
private fun _cta(
    modifier: Modifier = Modifier,
    total: Long,
    enabled: Boolean,
    onClickCta: () -> Unit
) {
    Row(modifier) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(end = 16.dp)
                .weight(7f)
                .fillMaxHeight()
        ) {
            Text(text = "Tổng thanh toán")
            Text(text = "$total đ", style = MaterialTheme.typography.titleLarge)
        }
        Box(
            modifier = Modifier
                .weight(3f)
                .clickable(enabled = enabled, onClick = onClickCta)
                .background(if (enabled) MaterialTheme.colorScheme.primary else Color.DarkGray)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Đặt hàng",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun topBar(onBack: () -> Unit) = @Composable {
    TopAppBar(title = { Text(text = "Checkout") }, navigationIcon = {
        IconButton(
            onClick = onBack
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
        }
    })
}