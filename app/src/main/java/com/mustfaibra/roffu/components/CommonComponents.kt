package com.mustfaibra.roffu.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateInt
import androidx.compose.animation.core.animateIntOffset
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.sealed.MenuOption
import com.mustfaibra.roffu.sealed.Screen
import com.mustfaibra.roffu.ui.theme.Dimension
import com.mustfaibra.roffu.utils.getDp
import kotlin.math.roundToInt

@Composable
fun CustomInputField(
    modifier: Modifier = Modifier,
    value: String,
    placeholder: String,
    textStyle: TextStyle = MaterialTheme.typography.body2, // Giảm kích thước chữ
    textColor: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),
    backgroundColor: Color = Color.White,
    requireSingleLine: Boolean = true,
    textShouldBeCentered: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLength: Int = Int.MAX_VALUE,
    imeAction: ImeAction = ImeAction.Done,
    shape: Shape = MaterialTheme.shapes.small,
    padding: PaddingValues = PaddingValues(horizontal = 4.dp, vertical = 2.dp), // Giảm padding
    leadingIcon: @Composable () -> Unit = {},
    trailingIcon: @Composable (() -> Unit)? = null,
    onValueChange: (string: String) -> Unit,
    onFocusChange: (focused: Boolean) -> Unit,
    onKeyboardActionClicked: KeyboardActionScope.() -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = {
            if (it.length <= maxLength) {
                onValueChange(it)
            }
        },
        modifier = modifier
            .background(backgroundColor, shape)
            .padding(padding)
            .height(48.dp) // Giới hạn chiều cao để nhỏ gọn hơn
            .onFocusChanged { onFocusChange(it.isFocused) },
        textStyle = textStyle.copy(
            color = textColor,
            textAlign = if (textShouldBeCentered) TextAlign.Center else TextAlign.Start
        ),
        placeholder = {
            Text(
                text = placeholder,
                style = textStyle,
                color = textColor.copy(alpha = 0.3f),
                maxLines = if (requireSingleLine) 1 else Int.MAX_VALUE,
            )
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            backgroundColor = backgroundColor,
            textColor = textColor,
            placeholderColor = textColor.copy(alpha = 0.3f),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = textColor,
        ),
        shape = shape,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onAny = {
                onKeyboardActionClicked()
            }
        ),
        singleLine = requireSingleLine,
    )
}
@Composable
fun SecondaryTopBar(
    title: String,
    backgroundColor: Color = MaterialTheme.colors.background,
    contentColor: Color = MaterialTheme.colors.onBackground,
    onBackClicked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = Dimension.pagePadding, vertical = Dimension.pagePadding.div(2)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding.times(1.5f)),
    ) {
        IconButton(
            icon = Icons.Rounded.KeyboardArrowLeft,
            backgroundColor = Color.Transparent,
            iconTint = contentColor,
            onButtonClicked = onBackClicked,
            shape = MaterialTheme.shapes.medium,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.button,
            color = contentColor,
            maxLines = 1,
        )
    }
}

@Composable
fun AppBottomNav(
    activeRoute: String,
    bottomNavDestinations: List<Screen>,
    backgroundColor: Color,
    onCartOffsetMeasured: (offset: IntOffset) -> Unit,
    onActiveRouteChange: (route: String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
    ) {
        val (cartOffsetY, setCartOffsetY) = remember {
            mutableStateOf(0)
        }
        Row(
            modifier = Modifier
                .onGloballyPositioned {
                    setCartOffsetY(it.size.height)
                }
                .fillMaxWidth()
                .padding(
                    horizontal = Dimension.pagePadding,
                    vertical = Dimension.pagePadding.div(2),
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            bottomNavDestinations.forEach {
                val isActive = activeRoute.equals(other = it.route, ignoreCase = true)
                AppBottomNavItem(
                    active = isActive,
                    title = stringResource(id = it.title ?: R.string.home),
                    icon = it.icon ?: R.drawable.ic_home_empty,
                    onRouteClicked = {
                        if (!isActive) {
                            onActiveRouteChange(it.route)
                        }
                    }
                )
                if (bottomNavDestinations.indexOf(it)
                        .plus(1) == bottomNavDestinations.size.div(2)
                ) {
                    Spacer(modifier = Modifier.width(Dimension.smIcon))
                }
            }
        }

        DrawableButton(
            modifier = Modifier
                .onGloballyPositioned {
                    it
                        .positionInWindow()
                        .let { offset ->
                            onCartOffsetMeasured(
                                IntOffset(
                                    x = offset.x
                                        .minus(Dimension.xlIcon.value)
                                        .roundToInt(),
                                    y = offset.y
                                        .plus(cartOffsetY.div(2))
                                        .roundToInt(),
                                )
                            )
                        }
                }
                .align(Alignment.TopCenter)
                .offset {
                    IntOffset(
                        y = -cartOffsetY.div(2),
                        x = 0,
                    )
                }
                .border(
                    width = Dimension.sm.div(2),
                    color = MaterialTheme.colors.primary,
                    shape = CircleShape,
                ),
            painter = painterResource(id = R.drawable.ic_barcode),
            backgroundColor = if (activeRoute == Screen.BarcodeScanner.route) MaterialTheme.colors.primary
            else MaterialTheme.colors.background,
            iconSize = Dimension.mdIcon.times(0.8f),
            iconTint = if (activeRoute == Screen.BarcodeScanner.route) MaterialTheme.colors.onPrimary
            else MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
            onButtonClicked = { onActiveRouteChange(Screen.BarcodeScanner.route) },
            shape = CircleShape,
            paddingValue = PaddingValues(Dimension.md),
        )
    }
}

@Composable
fun AppBottomNavItem(
    modifier: Modifier = Modifier,
    active: Boolean,
    title: String,
    icon: Int,
    onRouteClicked: () -> Unit,
) {
    Column(
        modifier = modifier
            .width(IntrinsicSize.Min)
            .clickable {
                onRouteClicked()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(
            modifier = Modifier
                .padding(bottom = Dimension.sm)
                .fillMaxWidth()
                .height(Dimension.xs)
                .clip(MaterialTheme.shapes.medium)
                .background(
                    if (active) MaterialTheme.colors.primary
                    else Color.Transparent,
                )
        )
        Icon(
            painter = painterResource(id = icon),
            contentDescription = title,
            tint = if (active) MaterialTheme.colors.primary
            else MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
            modifier = Modifier
                .size(Dimension.smIcon)
        )
    }
}

@Composable
fun PopupOptionsMenu(
    icon: Painter,
    iconSize: Dp = Dimension.smIcon,
    iconBackgroundColor: Color = MaterialTheme.colors.background,
    menuBackgroundColor: Color = MaterialTheme.colors.surface,
    menuContentColor: Color = MaterialTheme.colors.onSurface,
    options: List<MenuOption>,
    onOptionsMenuExpandChanges: () -> Unit,
    onMenuOptionSelected: (option: MenuOption) -> Unit,
    optionsMenuExpanded: Boolean,
) {
    Box {
        DrawableButton(
            painter = icon,
            onButtonClicked = onOptionsMenuExpandChanges,
            iconTint = menuContentColor,
            backgroundColor = iconBackgroundColor,
            iconSize = iconSize,
        )
        DropdownMenu(
            modifier = Modifier
                .width(intrinsicSize = IntrinsicSize.Max)
                .background(menuBackgroundColor),
            expanded = optionsMenuExpanded,
            onDismissRequest = onOptionsMenuExpandChanges,
        ) {
            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(menuBackgroundColor)
                        .clickable { onMenuOptionSelected(option) }
                        .padding(
                            horizontal = Dimension.pagePadding,
                            vertical = Dimension.xs
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding.div(2)),
                ) {

                    option.icon?.let { icon ->
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = stringResource(id = option.title),
                            modifier = Modifier.size(Dimension.smIcon),
                            tint = menuContentColor,
                        )
                    }
                    Text(
                        text = stringResource(id = option.title),
                        color = menuContentColor,
                        style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                    )
                }
            }
        }
    }
}

@Composable
fun ReactiveCartIcon(
    modifier: Modifier = Modifier,
    isOnCart: Boolean,
    onCartChange: () -> Unit,
) {
    val transition =
        updateTransition(targetState = isOnCart, label = "cart")

    val tint by transition.animateColor(label = "tint") {
        when (it) {
            true -> MaterialTheme.colors.primary
            false -> MaterialTheme.colors.onBackground.copy(alpha = 0.5f)
        }
    }
    val rotation by transition.animateFloat(label = "rotation") { if (it) 360f else -360f }
}

@Composable
fun ReactiveBookmarkIcon(
    modifier: Modifier = Modifier,
    iconSize: Dp = Dimension.smIcon,
    isOnBookmarks: Boolean,
    onBookmarkChange: () -> Unit,
) {
    val transition =
        updateTransition(targetState = isOnBookmarks, label = "bookmark")

    val tint by transition.animateColor(label = "tint") {
        when (it) {
            true -> MaterialTheme.colors.secondary
            false -> MaterialTheme.colors.onBackground.copy(alpha = 0.5f)
        }
    }
    val rotation by transition.animateFloat(label = "rotation") { if (it) 360f else -360f }
    Icon(
        painter = painterResource(id = if (isOnBookmarks) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark),
        contentDescription = null,
        modifier = modifier
            .rotate(rotation)
            .clip(CircleShape)
            .clickable { onBookmarkChange() }
            .padding(Dimension.elevation)
            .size(iconSize),
        tint = tint
    )
}

@Composable
fun ProductItemLayout(
    modifier: Modifier = Modifier,
    cartOffset: IntOffset,
    price: String,
    title: String,
    imageUrl: String,
    onCart: Boolean = false,
    onBookmark: Boolean = false,
    onProductClicked: () -> Unit,
    onChangeCartState: () -> Unit,
    onChangeBookmarkState: () -> Unit,
) {
    var productFloatToCartAnim by remember { mutableStateOf(false) }
    val productTransition = updateTransition(
        targetState = productFloatToCartAnim,
        label = "Product transition"
    )
    var floatingProductOffset by remember { mutableStateOf(cartOffset) }
    val animatedFloatingProductOffset by productTransition.animateIntOffset(
        label = "Product transition - offset",
        targetValueByState = {
            if (it) floatingProductOffset
            else IntOffset(x = 0, y = 0)
        },
        transitionSpec = {
            TweenSpec(
                durationMillis = 1000,
                easing = LinearEasing,
            )
        },
    )

    var floatingProductSize by remember { mutableStateOf(0) }
    val animatedFloatProductSize by productTransition.animateInt(
        label = "Product transition - size",
        targetValueByState = {
            if (it) 0
            else floatingProductSize
        },
        transitionSpec = {
            TweenSpec(
                durationMillis = 1000,
                easing = LinearEasing,
            )
        }
    )
    LaunchedEffect(key1 = onCart) {
        if (!onCart) {
            productFloatToCartAnim = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = MaterialTheme.shapes.small)
            .clickable(
                indication = null,
                interactionSource = MutableInteractionSource(),
                onClick = onProductClicked
            ),
        verticalArrangement = Arrangement.spacedBy(Dimension.pagePadding)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            elevation = 4.dp,
            shape = MaterialTheme.shapes.medium
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    error = painterResource(id = R.drawable.ic_placeholder)
                )
            }
        }
        Column(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(Dimension.xs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.x_VND, price),
                    style = MaterialTheme.typography.body1.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f),
                    ),
                )
                ReactiveCartIcon(
                    isOnCart = onCart,
                    onCartChange = {
                        if (animatedFloatProductSize !in 1..floatingProductSize.dec()) {
                            if (!onCart) {
                                productFloatToCartAnim = true
                            }
                            onChangeCartState()
                        }
                    },
                )
            }
            Text(
                modifier = Modifier,
                text = title,
                style = MaterialTheme.typography.caption,
                maxLines = 2,
            )
        }
    }
    if (productFloatToCartAnim) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .onGloballyPositioned {
                    it.positionInWindow().let { originalOffset ->
                        val requiredX = cartOffset.x - originalOffset.x
                        val requiredY = cartOffset.y - originalOffset.y
                        floatingProductOffset = IntOffset(
                            x = requiredX.roundToInt(),
                            y = requiredY.roundToInt(),
                        )
                    }
                }
                .offset { animatedFloatingProductOffset }
                .size(animatedFloatProductSize.getDp()),
            contentScale = ContentScale.Fit,
            error = painterResource(id = R.drawable.ic_placeholder)
        )
    }
}

@Composable
fun CustomSnackBar(
    modifier: Modifier = Modifier,
    snackHost: SnackbarHostState,
    content: @Composable (data: SnackbarData) -> Unit = {
        Text(
            text = it.message,
            style = MaterialTheme.typography.body1,
        )
    },
    backgroundColorProvider: () -> Color = { Color.White },
    contentColor: Color = MaterialTheme.colors.onPrimary,
) {
    SnackbarHost(hostState = snackHost, modifier = modifier) { data ->
        Snackbar(
            modifier = Modifier
                .padding(Dimension.pagePadding)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            backgroundColor = backgroundColorProvider(),
            contentColor = contentColor,
            content = {
                content(data)
            }
        )
    }
}

@Composable
fun SummaryRow(
    title: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
        )

        Text(
            text = value,
            style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.SemiBold),
            color = valueColor,
        )
    }
}