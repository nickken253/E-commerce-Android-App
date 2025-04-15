package com.mustfaibra.roffu.screens.admin

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.components.DrawableButton
import com.mustfaibra.roffu.components.IconButton
import com.mustfaibra.roffu.models.User
import com.mustfaibra.roffu.models.Product
import com.mustfaibra.roffu.sealed.Screen
import com.mustfaibra.roffu.ui.theme.Dimension

@Composable
fun AdminScreen(
    onBackRequested: () -> Unit,
    onNavigationRequested: (route: String, removePreviousRoute: Boolean) -> Unit,
    onToastRequested: (message: String, color: Color) -> Unit,
    viewModel: AdminViewModel = hiltViewModel(),
) {
    var selectedTab by remember { mutableStateOf(0) }
    val users by viewModel.users.collectAsState()
    val products by viewModel.products.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Admin Panel",
            style = MaterialTheme.typography.h5,
            color = MaterialTheme.colors.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .clip(CircleShape)
                .fillMaxWidth()
                .clickable {
                    viewModel.logout {
                        onNavigationRequested(Screen.Login.route, true)
                    }
                }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding)
        ) {
            DrawableButton(
                painter = rememberAsyncImagePainter(model = R.drawable.ic_logout),
                backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.2f),
                iconTint = MaterialTheme.colors.error,
                onButtonClicked = {},
                iconSize = Dimension.smIcon,
                paddingValue = PaddingValues(Dimension.md),
                shape = CircleShape,
            )
            Text(
                text = "Register",
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.error,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                icon = Icons.Rounded.KeyboardArrowRight,
                backgroundColor = MaterialTheme.colors.background,
                iconTint = MaterialTheme.colors.onBackground,
                onButtonClicked = {},
                iconSize = Dimension.smIcon,
                paddingValue = PaddingValues(Dimension.md),
                shape = CircleShape,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("User", modifier = Modifier.padding(16.dp))
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Product", modifier = Modifier.padding(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            Button(onClick = { onNavigationRequested(Screen.AddUser.route, false) }, modifier = Modifier.align(Alignment.End)) {
                Text("➕ Add User")
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(users) { user ->
                    UserItem(user = user, viewModel, onToastRequested, onNavigationRequested)
                }
            }
        } else {
            Button(onClick = { onNavigationRequested(Screen.AddProduct.route, false) }, modifier = Modifier.align(Alignment.End)) {
                Text("➕ Add Product")
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(products) { product ->
                    ProductItem(product = product, viewModel, onToastRequested, onNavigationRequested)
                }
            }
        }
    }
}

@Composable
fun UserItem(
    user: User,
    viewModel: AdminViewModel,
    onToastRequested: (String, Color) -> Unit,
    onNavigationRequested: (String, Boolean) -> Unit
) {
    Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("👤 ${user.name}", style = MaterialTheme.typography.subtitle1)
            Text("📧 ${user.email ?: "Không có email"}")
            Text("📞 ${user.phone ?: "Không có số điện thoại"}")
            Text("🏠 ${user.address ?: "Không có địa chỉ"}")
            Text("🔒 Vai trò: ${if (user.isAdmin()) "Admin" else "User"}")

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    onNavigationRequested(
                        Screen.EditUser.route.replace("{userId}", "${user.userId}"),
                        false
                    )
                }) {
                    Text("✏️ Edit")
                }
                OutlinedButton(onClick = {
                    viewModel.deleteUser(user) { msg, success ->
                        onToastRequested(msg, if (success) Color.Green else Color.Red)
                    }
                }, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)) {
                    Text("🗑️ Delete")
                }
            }
        }
    }
}

@Composable
fun ProductItem(
    product: Product,
    viewModel: AdminViewModel,
    onToastRequested: (String, Color) -> Unit,
    onNavigationRequested: (String, Boolean) -> Unit
) {
    Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📦 ${product.name}", style = MaterialTheme.typography.subtitle1)
            Text("💵 ${product.price} $")
            Text("🏷️ Barcode: ${product.barcode}")


            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    viewModel.getProductById(product.id) { foundProduct ->
                        if (foundProduct != null) {
                            Log.d("Product id: ",product.id.toString())
                            onNavigationRequested(
                                Screen.EditProduct.route.replace("{productId}", "${product.id}"),
                                false
                            )
                        } else {
                            onToastRequested("❌ Product is not exist", Color.Red)
                        }
                    }
                }) {
                    Text("✏️ Edit")
                }
                OutlinedButton(onClick = {
                    viewModel.deleteProduct(product) { msg, success ->
                        onToastRequested(msg, if (success) Color.Green else Color.Red)
                    }
                }, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)) {
                    Text("🗑️ Delete")
                }
            }
        }
    }
}