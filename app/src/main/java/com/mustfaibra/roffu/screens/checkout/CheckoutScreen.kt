package com.mustfaibra.roffu.screens.checkout

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.components.CustomButton
import com.mustfaibra.roffu.components.DrawableButton
import com.mustfaibra.roffu.components.IconButton
import com.mustfaibra.roffu.components.SecondaryTopBar
import com.mustfaibra.roffu.components.SummaryRow
import com.mustfaibra.roffu.models.CartItem
import com.mustfaibra.roffu.models.UserPaymentProviderDetails
import com.mustfaibra.roffu.models.VirtualCard
import com.mustfaibra.roffu.screens.profile.ProfileViewModel
import com.mustfaibra.roffu.sealed.UiState
import com.mustfaibra.roffu.ui.theme.Dimension
import com.mustfaibra.roffu.utils.encryptCardNumber
import com.skydoves.whatif.whatIf
import com.skydoves.whatif.whatIfNotNull

@Composable
fun CheckoutScreen(
    cartItems: List<CartItem>,
    onChangeLocationRequested: () -> Unit,
    onBackRequested: () -> Unit,
    onCheckoutSuccess: () -> Unit,
    onToastRequested: (message: String, color: Color) -> Unit,
    checkoutViewModel: CheckoutViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
) {
    LaunchedEffect(key1 = cartItems) {
        checkoutViewModel.setUserCart(cartItems = cartItems)
    }

    val checkoutUiState by remember { checkoutViewModel.checkoutState }
    val context = LocalContext.current
    val isVirtualCardAdded by profileViewModel.isVirtualCardAdded.collectAsState()

    if (checkoutUiState is UiState.Loading) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnClickOutside = false,
                dismissOnBackPress = false,
            )
        ) {
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colors.surface)
                    .fillMaxWidth()
                    .padding(Dimension.pagePadding.times(2))
            ) {
                val composition by rememberLottieComposition(
                    spec = LottieCompositionSpec.RawRes(R.raw.world_rounding),
                )

                /** to control the animation speed */
                val progress by animateLottieCompositionAsState(
                    composition,
                    iterations = LottieConstants.IterateForever,
                    speed = 1f,
                    restartOnPlay = true,
                )

                LottieAnimation(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    progress = { progress },
                    composition = composition,
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        /** Secondary top bar */
        SecondaryTopBar(
            title = stringResource(id = R.string.checkout),
            onBackClicked = onBackRequested,
        )
        Column(
            modifier = Modifier
                .weight(weight = 1f)
                .verticalScroll(state = rememberScrollState()),
        ) {
            val selectedPaymentMethodId by remember {
                checkoutViewModel.selectedPaymentMethodId
            }
            val subTotal by remember { checkoutViewModel.subTotalPrice }
            /** Delivery Location */
            val location by remember {
                checkoutViewModel.deliveryAddress
            }
            location?.whatIfNotNull(
                whatIf = {
                    DeliveryLocationSection(
                        address = it.address,
                        city = "${it.city}, ${it.country}",
                        onChangeLocationRequested = {
//                          onChangeLocationRequested()
                        },
                    )
                }
            )
            /** Payment methods */
            PaymentMethodsSection(
                isVirtualCardAdded = isVirtualCardAdded,
                selectedPayment = selectedPaymentMethodId,
                onPaymentSelected = { newMethodId ->
                    newMethodId.whatIf(
                        given = { it == selectedPaymentMethodId },
                        whatIfNot = {
                            checkoutViewModel.updateSelectedPaymentMethod(id = newMethodId)
                        },
                        whatIf = {},
                    )
                }
            )
            /** My minimized cart items */
            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
                contentPadding = PaddingValues(Dimension.pagePadding),
            ) {
                items(cartItems) { item ->
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colors.surface)
                            .aspectRatio(1f),
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = item.product?.image),
                            contentDescription = null,
                            modifier = Modifier
                                .size(Dimension.xlIcon)
                                .clip(MaterialTheme.shapes.medium),
                        )
                    }
                }
            }
            /** Checkout summary */
            Column(
                modifier = Modifier
                    .shadow(
                        elevation = Dimension.elevation.div(2),
                        shape = RoundedCornerShape(
                            topStartPercent = 15,
                            topEndPercent = 15,
                        ),
                        spotColor = MaterialTheme.colors.primary,
                    )
                    .clip(shape = MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colors.background)
                    .padding(all = Dimension.pagePadding),
                verticalArrangement = Arrangement.spacedBy(Dimension.sm)
            ) {
                /** sub total cost row */
                SummaryRow(
                    title = stringResource(id = R.string.sub_total),
                    value = "$$subTotal",
                    valueColor = Color(0xFF0052CC)
                )
                /** shipping cost row */
                SummaryRow(
                    title = stringResource(id = R.string.shipping),
                    value = "$15",
                    valueColor = Color(0xFF0052CC)
                )
                Divider()
                /** total cost row */
                SummaryRow(
                    title = stringResource(id = R.string.total),
                    value = "$${subTotal.plus(15)}",
                    valueColor = Color(0xFF0052CC)
                )
                CustomButton(
                    modifier = Modifier
                        .padding(top = Dimension.pagePadding)
                        .fillMaxWidth(),
                    text = stringResource(R.string.pay_now),
                    textStyle = MaterialTheme.typography.body1,
                    buttonColor = MaterialTheme.colors.primary,
                    shape = MaterialTheme.shapes.medium,
                    padding = PaddingValues(
                        all = Dimension.md.times(0.8f),
                    ),
                    onButtonClicked = {
                        checkoutViewModel.makeTransactionPayment(
                            items = cartItems,
                            total = subTotal.plus(15),
                            onCheckoutSuccess = onCheckoutSuccess,
                            onCheckoutFailed = { message ->
                                onToastRequested(
                                    context.getString(message),
                                    Color.Red,
                                )
                            }
                        )
                    },
                    contentColor = MaterialTheme.colors.onPrimary,
                )
            }
        }
    }
}

@Composable
fun DeliveryLocationSection(
    address: String,
    city: String,
    onChangeLocationRequested: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(Dimension.pagePadding),
        verticalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
    ) {
        Text(
            text = stringResource(R.string.delivery_address),
            style = MaterialTheme.typography.button,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
        ) {
            DrawableButton(
                painter = painterResource(id = R.drawable.ic_map_pin),
                onButtonClicked = {},
                backgroundColor = MaterialTheme.colors.surface,
                iconTint = MaterialTheme.colors.onSurface,
                paddingValue = PaddingValues(Dimension.sm),
            )
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = address,
                    style = MaterialTheme.typography.body1,
                )
                Text(
                    text = city,
                    style = MaterialTheme.typography.caption,
                )
            }
            IconButton(
                icon = Icons.Rounded.KeyboardArrowRight,
                backgroundColor = MaterialTheme.colors.background,
                iconTint = MaterialTheme.colors.onBackground,
                onButtonClicked = onChangeLocationRequested,
                iconSize = Dimension.mdIcon,
                paddingValue = PaddingValues(Dimension.sm),
                shape = MaterialTheme.shapes.medium,
            )
        }
    }
}

@Composable
fun PaymentMethodsSection(
    isVirtualCardAdded: Boolean,
    selectedPayment: String?,
    onPaymentSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(Dimension.pagePadding),
        verticalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
    ) {
        Text(
            text = "Phương thức thanh toán",
            style = MaterialTheme.typography.button,
        )
        // Visa option
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(if (selectedPayment == "visa") MaterialTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent)
                .clickable(enabled = isVirtualCardAdded) { onPaymentSelected("visa") },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
        ) {
            Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = null,
                tint = if (isVirtualCardAdded) MaterialTheme.colors.primary else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Visa",
                style = MaterialTheme.typography.body1,
                color = if (isVirtualCardAdded) MaterialTheme.colors.onSurface else Color.Gray,
                modifier = Modifier.weight(1f)
            )
            RadioButton(
                selected = selectedPayment == "visa",
                onClick = { if (isVirtualCardAdded) onPaymentSelected("visa") },
                enabled = isVirtualCardAdded,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colors.secondary,
                    unselectedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                )
            )
        }
        // Cash option
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(if (selectedPayment == "cash") MaterialTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent)
                .clickable { onPaymentSelected("cash") },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
        ) {
            Icon(
                imageVector = Icons.Default.AttachMoney,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Tiền mặt (thanh toán khi nhận hàng)",
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.weight(1f)
            )
            RadioButton(
                selected = selectedPayment == "cash",
                onClick = { onPaymentSelected("cash") },
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colors.secondary,
                    unselectedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                )
            )
        }
    }
}
