package com.mustfaibra.roffu.screens.holder

import android.util.Log
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
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.mustfaibra.roffu.components.AppBottomNav
import com.mustfaibra.roffu.components.CustomSnackBar
import com.mustfaibra.roffu.models.CartItem
import com.mustfaibra.roffu.models.ProductResponse
import com.mustfaibra.roffu.models.User
import com.mustfaibra.roffu.models.dto.CartResponse
import com.mustfaibra.roffu.models.dto.Image
import com.mustfaibra.roffu.models.dto.Product
import com.mustfaibra.roffu.providers.LocalNavHost
import com.mustfaibra.roffu.repositories.ProductsRepository
import com.mustfaibra.roffu.screens.admin.AddProductScreen
import com.mustfaibra.roffu.screens.admin.AddUserScreen
import com.mustfaibra.roffu.screens.admin.AdminScreen
import com.mustfaibra.roffu.screens.admin.AdminViewModel
import com.mustfaibra.roffu.screens.admin.EditProductScreen
import com.mustfaibra.roffu.screens.admin.EditUserScreen
import com.mustfaibra.roffu.screens.scanner.BarcodeScannerScreen
import com.mustfaibra.roffu.screens.bookmarks.BookmarksScreen
import com.mustfaibra.roffu.screens.cart.CartScreen
import com.mustfaibra.roffu.screens.cart.CartViewModel
import com.mustfaibra.roffu.screens.checkout.CheckoutScreen
import com.mustfaibra.roffu.screens.home.HomeScreen
import com.mustfaibra.roffu.screens.locationpicker.LocationPickerScreen
import com.mustfaibra.roffu.screens.login.ForgotPasswordScreen
import com.mustfaibra.roffu.screens.login.LoginScreen
import com.mustfaibra.roffu.screens.login.LoginViewModel
import com.mustfaibra.roffu.screens.login.ManHinhDangKy
import com.mustfaibra.roffu.screens.login.OtpVerificationScreen
import com.mustfaibra.roffu.screens.login.ResetPasswordScreen
import com.mustfaibra.roffu.screens.notifications.NotificationScreen
import com.mustfaibra.roffu.screens.onboard.OnboardScreen
import com.mustfaibra.roffu.screens.order.OrderScreen
import com.mustfaibra.roffu.screens.orderhistory.OrdersHistoryScreen
import com.mustfaibra.roffu.screens.productdetails.ProductComparisonScreen
import com.mustfaibra.roffu.screens.productdetails.ProductDetailsScreen
import com.mustfaibra.roffu.screens.productdetails.ProductSelectionScreen
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
        listOf(Screen.Home, Screen.OrderHistory, Screen.Cart, Screen.Profile)
    }

    val cartViewModel: CartViewModel = hiltViewModel()

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
            holderViewModel = holderViewModel,
            onStatusBarColorChange = onStatusBarColorChange,
            bottomNavigationContent = {
                if (
                    currentRouteAsState in destinations.map { it.route }
                    || currentRouteAsState == Screen.BarcodeScanner.route
                    || currentRouteAsState == Screen.OrderManager.route
                    || currentRouteAsState == Screen.Bookmark.route
                ) {
                    AppBottomNav(
                        activeRoute = currentRouteAsState,
                        bottomNavDestinations = destinations,
                        backgroundColor = MaterialTheme.colors.background,
                        onCartOffsetMeasured = { offset ->
                            setCartOffset(offset)
                        },
                        onActiveRouteChange = { route ->
                            if (route != currentRouteAsState) {
                                controller.navigate(route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(controller.graph.startDestinationId) {
                                        saveState = true
                                    }
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
                    Screen.ProductDetails.route.replace("{productId}", productId.toString())
                )
            },
            onUpdateCartRequest = { productId ->
                holderViewModel.updateCart(
                    productId = productId
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
    holderViewModel: HolderViewModel,
    onStatusBarColorChange: (color: Color) -> Unit,
    bottomNavigationContent: @Composable () -> Unit,
    onSplashFinished: (nextDestination: Screen) -> Unit,
    onBoardFinished: () -> Unit,
    onBackRequested: () -> Unit,
    onNavigationRequested: (route: String, removePreviousRoute: Boolean) -> Unit,
    onShowProductRequest: (productId: Int) -> Unit,
    onUpdateCartRequest: (productId: Int) -> Unit,
    onUpdateBookmarkRequest: (productId: Int) -> Unit,
    onUserNotAuthorized: (removeCurrentRoute: Boolean) -> Unit,
    onToastRequested: (message: String, color: Color) -> Unit,
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
                controller.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true } // Xóa toàn bộ back stack
                    launchSingleTop = true
                }
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
                        onDangKyThanhCong = {
                            controller.navigate(Screen.Login.route) {
                                popUpTo(Screen.Register.route) { inclusive = true } // Xóa Register
                            }
                        },
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
                            //onToastRequested("Bạn không có quyền truy cập!", Color.Red)
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
                            //onToastRequested("Bạn không có quyền truy cập!", Color.Red)
                            controller.navigate(Screen.Home.route) {
                                popUpTo(Screen.AddUser.route) { inclusive = true }
                            }
                        }
                    }
                }
                composable(
                    route = Screen.EditUser.route,
                    arguments = listOf()
                ) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    // Chỉ cho phép admin truy cập
                    if (user?.isAdmin() == true) {
                        EditUserScreen(
                            userId = 1, // TODO: Truyền userId động qua route/navArgument, hiện tại tạm truyền cứng để tránh lỗi build
                            onBackRequested = onBackRequested,
                            onToastRequested = onToastRequested
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            // onToastRequested("Bạn không có quyền truy cập!", Color.Red)
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
                            onDone = { onBackRequested() }, // hoặc một logic khác nếu cần
                            onToastRequested = onToastRequested
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            onToastRequested("Bạn không có quyền truy cập!", Color.Red)
                            controller.navigate(Screen.Home.route) {
                                popUpTo(Screen.AddProduct.route) { inclusive = true }
                            }
                        }
                    }
                }
                composable(
                    route = Screen.EditProduct.route,
                    arguments = listOf()
                ) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    if (user?.isAdmin() == true) {

                        val productId = it.arguments?.getInt("productId")
                            ?: throw IllegalArgumentException("Product ID is required")
                        EditProductScreen(
                            productId = 1, // TODO: Truyền productId động qua route/navArgument, hiện tại tạm truyền cứng để tránh lỗi build
                            onBack = onBackRequested,
                            onDone = { onBackRequested() },
                            onToastRequested = onToastRequested
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            onToastRequested("Bạn không có quyền truy cập!", Color.Red)
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
                            onBookmarkStateChanged = onUpdateBookmarkRequest,
//                            onProductClicked = { productId ->
//                                controller.navigate(
//                                    Screen.ProductDetails.route.replace("{productId}", productId.toString())
//                                )
//                            },
//                            onCartStateChanged = { productId ->
//                                holderViewModel.updateCart(productId)
//                            },
//                            onBookmarkStateChanged = { productId ->
//                                holderViewModel.updateBookmarks(
//                                    productId = productId,
//                                    currentlyOnBookmarks = productId in productsOnBookmarksIds
//                                )
//                            },
                            onNavigateToSearch = {
                                controller.navigate(Screen.Search.route)
                            }
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
                    SearchScreen(
                        onNavigateBack = {
                            controller.popBackStack()
                        },
                        onProductClick = onShowProductRequest
                    )
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
                        navController = controller,
                        viewModel = hiltViewModel()
                    )
                }
                composable(Screen.Cart.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    if (user?.isAdmin() != true) {
                        CartScreen(
                            navController = controller,
                            user = user,
                            onProductClicked = onShowProductRequest,
                            onUserNotAuthorized = { onUserNotAuthorized(false) },
                            onCheckoutRequest = {
                                onNavigationRequested(Screen.Checkout.route, false)
                            },
                            onNavigationRequested = onNavigationRequested,
                            onToastRequested = onToastRequested
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
                                    navController = controller,
                                    itemsJson = "",  // Truyền giá trị mặc định
                                    totalAmount = 0.0,  // Truyền giá trị mặc định
                                    onChangeLocationRequested = {
                                        onNavigationRequested(Screen.LocationPicker.route, false)
                                    },
                                    onNavigationRequested = onNavigationRequested,
                                    onToastRequested = onToastRequested,
                                    checkoutViewModel = hiltViewModel(),
                                    profileViewModel = hiltViewModel()
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
                composable(
                    route = Screen.CheckoutWithProducts.route,
                    arguments = listOf(
                        navArgument("items") { type = NavType.StringType },
                        navArgument("totalAmount") { type = NavType.FloatType }
                    )
                ) { backStackEntry ->
                    val itemsJson = backStackEntry.arguments?.getString("items") ?: ""
                    val totalAmount = backStackEntry.arguments?.getFloat("totalAmount")?.toDouble() ?: 0.0
                    if (itemsJson.isEmpty()) {
                        Log.w("NavGraph", "Empty itemsJson, redirecting to Cart")
                        onToastRequested("Không có sản phẩm được chọn", Color.Red)
                        onNavigationRequested(Screen.Cart.route, true)
                    } else {
                        CheckoutScreen(
                            navController = controller,
                            itemsJson = itemsJson,
                            totalAmount = totalAmount,
                            onChangeLocationRequested = {
                                // Giả định route cho màn hình chọn địa chỉ
                                onNavigationRequested("location_picker", false)
                                onToastRequested("Đang mở màn hình chọn địa chỉ", Color.Blue)
                            },
                            onNavigationRequested = onNavigationRequested,
                            onToastRequested = onToastRequested,
                            checkoutViewModel = hiltViewModel(),
                            profileViewModel = hiltViewModel()
                        )
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
                    val context = LocalContext.current
                    val loginViewModel: LoginViewModel = hiltViewModel()
                    // Kiểm tra trạng thái đăng nhập
                    LaunchedEffect(Unit) {
                        if (UserPref.getToken(context) == null) {
                            loginViewModel.logout() // Đặt lại trạng thái LoginViewModel
                            onNavigationRequested(Screen.Login.route, true)
                        }
                    }
                    if (user?.isAdmin() != true) {
                        user.whatIfNotNull(
                            whatIf = {
                                ProfileScreen(
                                    user = it,
                                    onNavigationRequested = onNavigationRequested,
                                    onLogoutRequested = {
                                        loginViewModel.logout()
                                        onNavigationRequested(Screen.Login.route, true)
                                    }
                                )
                            },
                            whatIfNot = {
                                LaunchedEffect(Unit) {
                                    onNavigationRequested(Screen.Login.route, true)
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
                composable(Screen.OrderManager.route) {
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

                    OrderScreen(
                        onBack = { controller.popBackStack() }
                    )
                }
                composable(
                    route = Screen.ProductDetails.route,
                    arguments = listOf(
                        navArgument(name =  "productId") {
                            type = NavType.IntType
                        }
                    )
                ) { backStackEntry ->
                    val productId = backStackEntry.arguments?.getInt("productId") ?: 0
                    onStatusBarColorChange(MaterialTheme.colors.background)
//                    val productId = it.arguments?.getInt("productId")
                        ?: throw IllegalArgumentException("Product id is required")
                    ProductDetailsScreen(
                        productId = productId,
                        cartItemsCount = cartItems.size,
                        isOnCartStateProvider = { productsOnCartIds.contains(productId) },
                        isOnBookmarksStateProvider = { productsOnBookmarksIds.contains(productId) },
                        onUpdateCartState = { productId ->
                            onUpdateCartRequest(productId)
                        },
                        onUpdateBookmarksState = { productId ->
                            onUpdateBookmarkRequest(productId)
                        },
                        onBackRequested = onBackRequested,
                        navController = controller
                    )
                }
                composable(Screen.OrderHistory.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    OrderScreen()
                }
                composable(
                    route = Screen.ProductComparison.route,
                    arguments = listOf(
                        navArgument("productId1") { type = NavType.IntType },
                        navArgument("productId2") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val productId1 = backStackEntry.arguments?.getInt("productId1") ?: 0
                    val productId2 = backStackEntry.arguments?.getInt("productId2") ?: 0
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    ProductComparisonScreen(
                        productId1 = productId1,
                        productId2 = productId2,
                        onNavigateBack = onBackRequested
                    )
                }
                composable(
                    route = Screen.ProductSelection.route,
                    arguments = listOf(
                        navArgument("productId") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val productId = backStackEntry.arguments?.getInt("productId") ?: 0
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    ProductSelectionScreen(
                        currentProductId = productId,
                        onNavigateBack = onBackRequested,
                        navController = controller
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

@Composable
fun ProductItem(
    product: ProductResponse,
    onProductClick: (Int) -> Unit,
    onBookmarkClick: (Int) -> Unit,
    isBookmarked: Boolean
) {
    // ... existing code ...
}
@Composable
fun HolderScreen(
    navController: NavController,
    viewModel: HolderViewModel = hiltViewModel()
) {
    // ... existing code ...

    // Thay thế ProductComparisonScreen và ProductSelectionScreen bằng các màn hình tương ứng
    // hoặc xóa nếu không cần thiết
}
