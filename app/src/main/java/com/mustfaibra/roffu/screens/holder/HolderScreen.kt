package com.mustfaibra.roffu.screens.holder

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.mustfaibra.roffu.components.AppBottomNav
import com.mustfaibra.roffu.components.CustomSnackBar
import com.mustfaibra.roffu.models.CartItem
import com.mustfaibra.roffu.models.User
import com.mustfaibra.roffu.providers.LocalNavHost
import com.mustfaibra.roffu.screens.admin.AddProductScreen
import com.mustfaibra.roffu.screens.admin.AddUserScreen
import com.mustfaibra.roffu.screens.admin.AdminScreen
import com.mustfaibra.roffu.screens.admin.EditProductScreen
import com.mustfaibra.roffu.screens.admin.EditUserScreen
import com.mustfaibra.roffu.screens.barcode.BarcodeScannerScreen
import com.mustfaibra.roffu.screens.bookmarks.BookmarksScreen
import com.mustfaibra.roffu.screens.cart.CartScreen
import com.mustfaibra.roffu.screens.checkout.CheckoutScreen
import com.mustfaibra.roffu.screens.home.HomeScreen
import com.mustfaibra.roffu.screens.locationpicker.LocationPickerScreen
import com.mustfaibra.roffu.screens.login.ForgotPasswordScreen
import com.mustfaibra.roffu.screens.login.LoginScreen
import com.mustfaibra.roffu.screens.login.ManHinhDangKy
import com.mustfaibra.roffu.screens.login.OtpVerificationScreen
import com.mustfaibra.roffu.screens.login.ResetPasswordScreen
import com.mustfaibra.roffu.screens.notifications.NotificationScreen
import com.mustfaibra.roffu.screens.onboard.OnboardScreen
import com.mustfaibra.roffu.screens.orderhistory.OrdersHistoryScreen
import com.mustfaibra.roffu.screens.productdetails.ProductDetailsScreen
import com.mustfaibra.roffu.screens.profile.ProfileScreen
import com.mustfaibra.roffu.screens.search.SearchScreen
import com.mustfaibra.roffu.screens.signup.SignupScreen
import com.mustfaibra.roffu.screens.splash.SplashScreen
import com.mustfaibra.roffu.sealed.Screen
import com.mustfaibra.roffu.utils.UserPref
import com.mustfaibra.roffu.utils.getDp
import com.skydoves.whatif.whatIfNotNull
import kotlinx.coroutines.launch

@Composable
fun HolderScreen(
    onStatusBarColorChange: (color: Color) -> Unit,
    holderViewModel: HolderViewModel = hiltViewModel(),
) {
    val destinations = remember {
        listOf(Screen.Home, Screen.Bookmark, Screen.Cart, Screen.Profile)
    }

    /** Our navigation controller that the MainActivity provides */
    val controller = LocalNavHost.current

    /** The current active navigation route */
    val currentRouteAsState = getActiveRoute(navController = controller)

    /** The cart items list */
    val cartItems = holderViewModel.cartItems

    /** The ids of all the products on user's cart */
    val productsOnCartIds = holderViewModel.productsOnCartIds

    /** The ids of all the bookmarked products on user's bookmarks */
    val productsOnBookmarksIds = holderViewModel.productsOnBookmarksIds

    /** The current logged user, which is null by default */
    val user by UserPref.user

    /** The main app's scaffold state */
    val scaffoldState = rememberScaffoldState()

    /** The coroutine scope */
    val scope = rememberCoroutineScope()

    /** Dynamic snack bar color */
    val (snackBarColor, setSnackBarColor) = remember {
        mutableStateOf(Color.White)
    }

    /** SnackBar appear/disappear transition */
    val snackBarTransition = updateTransition(
        targetState = scaffoldState.snackbarHostState,
        label = "SnackBarTransition"
    )

    /** SnackBar animated offset */
    val snackBarOffsetAnim by snackBarTransition.animateDp(
        label = "snackBarOffsetAnim",
        transitionSpec = {
            TweenSpec(
                durationMillis = 300,
                easing = LinearEasing,
            )
        }
    ) {
        when (it.currentSnackbarData) {
            null -> {
                100.getDp()
            }
            else -> {
                0.getDp()
            }
        }
    }

    Box {
        /** Cart offset on the screen */
        val (cartOffset, setCartOffset) = remember {
            mutableStateOf(IntOffset(0, 0))
        }
        ScaffoldSection(
            controller = controller,
            scaffoldState = scaffoldState,
            cartOffset = cartOffset,
            user = user,
            cartItems = cartItems,
            productsOnCartIds = productsOnCartIds,
            productsOnBookmarksIds = productsOnBookmarksIds,
            onStatusBarColorChange = onStatusBarColorChange,
            bottomNavigationContent = {
                if (
                    currentRouteAsState in destinations.map { it.route }
                    || currentRouteAsState == Screen.BarcodeScanner.route
                ) {
                    AppBottomNav(
                        activeRoute = currentRouteAsState,
                        backgroundColor = MaterialTheme.colors.surface,
                        bottomNavDestinations = destinations,
                        onCartOffsetMeasured = { offset ->
                            setCartOffset(offset)
                        },
                        onActiveRouteChange = {
                            if (it != currentRouteAsState) {
                                /** We should navigate to that new route */
                                controller.navigate(it) {
                                    popUpTo(Screen.Home.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            },
            onSplashFinished = { nextDestination ->
                controller.navigate(nextDestination.route) {
                    popUpTo(Screen.Splash.route) {
                        inclusive = true
                    }
                }
            },
            onBoardFinished = {
                controller.navigate(Screen.Home.route) {
                    popUpTo(Screen.Onboard.route) {
                        inclusive = true
                    }
                }
            },
            onBackRequested = {
                controller.popBackStack()
            },
            onNavigationRequested = { route, removePreviousRoute ->
                if (removePreviousRoute) {
                    controller.popBackStack()
                }
                controller.navigate(route)
            },
            onShowProductRequest = { productId ->
                controller.navigate(
                    Screen.ProductDetails.route
                        .replace("{productId}", "$productId")
                )
            },
            onUpdateCartRequest = { productId ->
                holderViewModel.updateCart(
                    productId = productId,
                    currentlyOnCart = productId in productsOnCartIds,
                )
            },
            onUpdateBookmarkRequest = { productId ->
                holderViewModel.updateBookmarks(
                    productId = productId,
                    currentlyOnBookmarks = productId in productsOnBookmarksIds,
                )
            },
            onUserNotAuthorized = { removeCurrentRoute ->
                if (removeCurrentRoute) {
                    controller.popBackStack()
                }
                controller.navigate(Screen.Login.route)
            },
            onToastRequested = { message, color ->
                scope.launch {
                    /** dismiss the previous one if its exist */
                    scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                    /** Update the snack bar color */
                    setSnackBarColor(color)
                    scaffoldState.snackbarHostState
                        .showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Short,
                        )
                }
            }
        )

        /** The snack bar UI */
        CustomSnackBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = snackBarOffsetAnim),
            snackHost = scaffoldState.snackbarHostState,
            backgroundColorProvider = { snackBarColor },
        )
    }
}

@Composable
fun ScaffoldSection(
    controller: NavHostController,
    scaffoldState: ScaffoldState,
    cartOffset: IntOffset,
    user: User?,
    cartItems: List<CartItem>,
    productsOnCartIds: List<Int>,
    productsOnBookmarksIds: List<Int>,
    onStatusBarColorChange: (color: Color) -> Unit,
    onSplashFinished: (nextDestination: Screen) -> Unit,
    onBoardFinished: () -> Unit,
    onNavigationRequested: (route: String, removePreviousRoute: Boolean) -> Unit,
    onBackRequested: () -> Unit,
    onUpdateCartRequest: (productId: Int) -> Unit,
    onUpdateBookmarkRequest: (productId: Int) -> Unit,
    onShowProductRequest: (productId: Int) -> Unit,
    onUserNotAuthorized: (removeCurrentRoute: Boolean) -> Unit,
    onToastRequested: (message: String, color: Color) -> Unit,
    bottomNavigationContent: @Composable () -> Unit,
) {
    // Kiểm tra vai trò người dùng và điều hướng
    LaunchedEffect(user) {
        user?.let {
            if (it.isAdmin()) {
                // Nếu là admin, điều hướng đến AdminScreen
                controller.navigate(Screen.Admin.route) {
                    popUpTo(0) { inclusive = true } // Xóa toàn bộ stack
                    launchSingleTop = true
                }
            } else {
                // Nếu là user bình thường, điều hướng đến HomeScreen (trừ khi đang ở các màn hình chính)
                if (controller.currentDestination?.route !in listOf(
                        Screen.Home.route,
                        Screen.Bookmark.route,
                        Screen.Cart.route,
                        Screen.Profile.route,
                        Screen.ProductDetails.route,
                        Screen.Search.route,
                        Screen.Notifications.route,
                        Screen.BarcodeScanner.route,
                        Screen.Checkout.route,
                        Screen.OrderHistory.route,
                        Screen.LocationPicker.route
                    )
                ) {
                    controller.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        } ?: run {
            // Nếu chưa đăng nhập, điều hướng đến LoginScreen (trừ các màn hình khởi tạo)
            if (controller.currentDestination?.route !in listOf(
                    Screen.Splash.route,
                    Screen.Onboard.route,
                    Screen.Login.route,
                    Screen.Signup.route,
                    Screen.Register.route,
                    Screen.ResetPassword.route,
                    "forgot-password",
                    "otp-verification/{email}" // Thêm route mới
                )
            ) {
                onUserNotAuthorized(true)
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = { scaffoldState.snackbarHostState },
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            NavHost(
                modifier = Modifier.weight(1f),
                navController = controller,
                startDestination = Screen.Splash.route
            ) {
                composable(Screen.Splash.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    SplashScreen(onSplashFinished = onSplashFinished)
                }
                composable(Screen.Onboard.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    OnboardScreen(onBoardFinished = onBoardFinished)
                }
                composable(Screen.Signup.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    SignupScreen()
                }
                composable(Screen.Login.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    LoginScreen(
                        navController = controller,
                        onUserAuthenticated = onBackRequested,
                        onToastRequested = onToastRequested,
                        onNavigateToRegister = { controller.navigate(Screen.Register.route) }
                    )
                }
                composable(Screen.Register.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    ManHinhDangKy(
                        onQuayLaiDangNhap = { controller.popBackStack() },
                        onDangKyThanhCong = { controller.navigate(Screen.Home.route) },
                        onYeuCauToast = onToastRequested
                    )
                }
                composable("forgot-password") {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    ForgotPasswordScreen(
                        navController = controller,
                        onToastRequested = onToastRequested
                    )
                }
                composable(
                    route = "otp-verification/{email}",
                    arguments = listOf(navArgument("email") { type = NavType.StringType })
                ) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    val email = it.arguments?.getString("email") ?: ""
                    OtpVerificationScreen(
                        navController = controller,
                        email = email,
                        onToastRequested = onToastRequested
                    )
                }
                composable(
                    route = Screen.ResetPassword.route,
                    arguments = listOf(navArgument("email") { type = NavType.StringType })
                ) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    val email = it.arguments?.getString("email") ?: ""
                    ResetPasswordScreen(
                        navController = controller,
                        email = email,
                        onToastRequested = onToastRequested
                    )
                }
                composable(Screen.Admin.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    // Chỉ cho phép admin truy cập
                    if (user?.isAdmin() == true) {
                        AdminScreen(
                            onBackRequested = onBackRequested,
                            onNavigationRequested = onNavigationRequested,
                            onToastRequested = onToastRequested
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            controller.navigate(Screen.Home.route) {
                                popUpTo(Screen.Admin.route) { inclusive = true }
                            }
                        }
                    }
                }
                composable(Screen.AddUser.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    // Chỉ cho phép admin truy cập
                    if (user?.isAdmin() == true) {
                        AddUserScreen(
                            onBackRequested = onBackRequested,
                            onToastRequested = onToastRequested
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            controller.navigate(Screen.Home.route) {
                                popUpTo(Screen.AddUser.route) { inclusive = true }
                            }
                        }
                    }
                }
                composable(
                    route = Screen.EditUser.route,
                    arguments = listOf(navArgument(name = "userId") { type = NavType.IntType })
                ) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    // Chỉ cho phép admin truy cập
                    if (user?.isAdmin() == true) {
                        val userId = it.arguments?.getInt("userId")
                            ?: throw IllegalArgumentException("User ID is required")
                        EditUserScreen(
                            userId = userId,
                            onBackRequested = onBackRequested,
                            onToastRequested = onToastRequested
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            controller.navigate(Screen.Home.route) {
                                popUpTo(Screen.EditUser.route) { inclusive = true }
                            }
                        }
                    }
                }
                composable(Screen.AddProduct.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    if (user?.isAdmin() == true) {
                        AddProductScreen(
                            onBack = onBackRequested,
                            onDone = { onBackRequested() },
                            onToastRequested = onToastRequested
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            controller.navigate(Screen.Home.route) {
                                popUpTo(Screen.AddProduct.route) { inclusive = true }
                            }
                        }
                    }
                }
                composable(
                    route = Screen.EditProduct.route,
                    arguments = listOf(navArgument(name = "productId") { type = NavType.IntType })
                ) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    if (user?.isAdmin() == true) {
                        val productId = it.arguments?.getInt("productId")
                            ?: throw IllegalArgumentException("Product ID is required")
                        EditProductScreen(
                            productId = productId,
                            onBack = onBackRequested,
                            onDone = { onBackRequested() },
                            onToastRequested = onToastRequested
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            controller.navigate(Screen.Home.route) {
                                popUpTo(Screen.EditProduct.route) { inclusive = true }
                            }
                        }
                    }
                }
                composable(Screen.Home.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    // Chỉ cho phép user bình thường hoặc khi chưa đăng nhập
                    if (user?.isAdmin() != true) {
                        HomeScreen(
                            cartOffset = cartOffset,
                            cartProductsIds = productsOnCartIds,
                            bookmarkProductsIds = productsOnBookmarksIds,
                            onProductClicked = onShowProductRequest,
                            onCartStateChanged = onUpdateCartRequest,
                            onBookmarkStateChanged = onUpdateBookmarkRequest
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            controller.navigate(Screen.Admin.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    }
                }
                composable(Screen.Notifications.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    NotificationScreen()
                }
                composable(Screen.Search.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    SearchScreen()
                }
                composable(Screen.Bookmark.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    if (user?.isAdmin() != true) {
                        BookmarksScreen(
                            cartOffset = cartOffset,
                            cartProductsIds = productsOnCartIds,
                            onProductClicked = onShowProductRequest,
                            onCartStateChanged = onUpdateCartRequest,
                            onBookmarkStateChanged = onUpdateBookmarkRequest
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            controller.navigate(Screen.Admin.route) {
                                popUpTo(Screen.Bookmark.route) { inclusive = true }
                            }
                        }
                    }
                }
                composable(Screen.BarcodeScanner.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    BarcodeScannerScreen(
                        navController = controller
                    )
                }
                composable(Screen.Cart.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    if (user?.isAdmin() != true) {
                        CartScreen(
                            user = user,
                            onProductClicked = onShowProductRequest,
                            onUserNotAuthorized = { onUserNotAuthorized(false) },
                            onCheckoutRequest = {
                                onNavigationRequested(Screen.Checkout.route, false)
                            }
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            controller.navigate(Screen.Admin.route) {
                                popUpTo(Screen.Cart.route) { inclusive = true }
                            }
                        }
                    }
                }
                composable(Screen.Checkout.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    if (user?.isAdmin() != true) {
                        user.whatIfNotNull(
                            whatIf = {
                                CheckoutScreen(
                                    cartItems = cartItems,
                                    onBackRequested = onBackRequested,
                                    onCheckoutSuccess = {
                                        onNavigationRequested(Screen.OrderHistory.route, true)
                                    },
                                    onToastRequested = onToastRequested,
                                    onChangeLocationRequested = {
                                        onNavigationRequested(Screen.LocationPicker.route, false)
                                    }
                                )
                            },
                            whatIfNot = {
                                LaunchedEffect(Unit) {
                                    onUserNotAuthorized(true)
                                }
                            }
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            controller.navigate(Screen.Admin.route) {
                                popUpTo(Screen.Checkout.route) { inclusive = true }
                            }
                        }
                    }
                }
                composable(Screen.LocationPicker.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    LocationPickerScreen(
                        onLocationRequested = {},
                        onLocationPicked = {}
                    )
                }
                composable(Screen.Profile.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    if (user?.isAdmin() != true) {
                        user.whatIfNotNull(
                            whatIf = {
                                ProfileScreen(
                                    user = it,
                                    onNavigationRequested = onNavigationRequested,
                                    onLogoutRequested = {
                                        UserPref.logout()
                                        onNavigationRequested(Screen.Login.route, true)
                                    }
                                )
                            },
                            whatIfNot = {
                                LaunchedEffect(Unit) {
                                    onUserNotAuthorized(false)
                                }
                            }
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            controller.navigate(Screen.Admin.route) {
                                popUpTo(Screen.Profile.route) { inclusive = true }
                            }
                        }
                    }
                }
                composable(Screen.OrderHistory.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    if (user?.isAdmin() != true) {
                        user.whatIfNotNull(
                            whatIf = {
                                OrdersHistoryScreen(
                                    onBackRequested = onBackRequested
                                )
                            },
                            whatIfNot = {
                                LaunchedEffect(Unit) {
                                    onUserNotAuthorized(true)
                                }
                            }
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            controller.navigate(Screen.Admin.route) {
                                popUpTo(Screen.OrderHistory.route) { inclusive = true }
                            }
                        }
                    }
                }
                composable(
                    route = Screen.ProductDetails.route,
                    arguments = listOf(
                        navArgument(name = "productId") { type = NavType.IntType }
                    )
                ) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    val productId = it.arguments?.getInt("productId")
                        ?: throw IllegalArgumentException("Product id is required")
                    ProductDetailsScreen(
                        productId = productId,
                        cartItemsCount = cartItems.size,
                        isOnBookmarksStateProvider = { productId in productsOnBookmarksIds },
                        onUpdateBookmarksState = onUpdateBookmarkRequest,
                        onBackRequested = onBackRequested
                    )
                }
            }
            // Chỉ hiển thị bottom navigation cho user bình thường
            if (user?.isAdmin() != true) {
                bottomNavigationContent()
            }
        }
    }
}

@Composable
fun getActiveRoute(navController: NavHostController): String {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route ?: "splash"
}