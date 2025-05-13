package com.mustfaibra.roffu.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.components.DrawableButton
import com.mustfaibra.roffu.components.IconButton
import com.mustfaibra.roffu.models.User
import com.mustfaibra.roffu.models.VirtualCard
import com.mustfaibra.roffu.screens.profile.AddVirtualCardScreen
import com.mustfaibra.roffu.screens.profile.VisaCardDisplay
import com.mustfaibra.roffu.sealed.Screen
import com.mustfaibra.roffu.ui.theme.Dimension
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    user: User,
    onNavigationRequested: (route: String, removePreviousRoute: Boolean) -> Unit,
    onLogoutRequested: () -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    selectedTab: String? = null,
) {
    val generalOptions = remember {
        listOf(Screen.Settings, Screen.Bookmark)
    }
    val personalOptions = remember {
        listOf(Screen.PrivacyPolicies, Screen.TermsConditions)
    }
    var showOrderHistory by remember { mutableStateOf(false) }
    val virtualCard by profileViewModel.virtualCard.collectAsState()
    var showAddCardScreen by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTab) {
        if (selectedTab == Screen.Profile.route) {
            showOrderHistory = false
        }
    }

    LaunchedEffect(user.userId) {
        profileViewModel.loadVirtualCard(user.userId)
    }

    if (showOrderHistory) {
        onNavigationRequested(Screen.OrderManager.route, false)
        showOrderHistory = false
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = Dimension.pagePadding),
        verticalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
    ) {
        item {
            Text(
                text = stringResource(id = R.string.your_profile),
                style = MaterialTheme.typography.button,
                color = MaterialTheme.colors.onBackground,
            )
        }
        /** Header section */
        item {
            ProfileHeaderSection(
                image = user.profile,
                name = user.name,
                email = user.email,
                phone = user.phone,
            )
        }
        /** Add virtual card section */
        item {
            if (virtualCard == null && !showAddCardScreen) {
                Card(
                    modifier = Modifier.clickable { showAddCardScreen = true },
                    shape = MaterialTheme.shapes.medium,
                    backgroundColor = MaterialTheme.colors.secondary,
                    contentColor = MaterialTheme.colors.onSecondary,
                ) {
                    Column(
                        modifier = Modifier.padding(Dimension.pagePadding),
                        verticalArrangement = Arrangement.spacedBy(Dimension.md),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = "Add virtual card",
                                style = MaterialTheme.typography.button,
                            )
                            IconButton(
                                icon = Icons.Rounded.KeyboardArrowRight,
                                backgroundColor = MaterialTheme.colors.background,
                                iconTint = MaterialTheme.colors.onBackground,
                                onButtonClicked = { showAddCardScreen = true },
                                iconSize = Dimension.smIcon,
                                paddingValue = PaddingValues(Dimension.xs),
                                shape = MaterialTheme.shapes.medium,
                            )
                        }
                        Text(
                            text = "Virtual cards allow you to purchase products on the store.",
                            style = MaterialTheme.typography.body2,
                        )
                    }
                }
            } else if (showAddCardScreen) {
                AddVirtualCardScreen(
                    onCardAdded = { cardNumber, month, year, cvv, cardHolder ->
                        profileViewModel.addVirtualCard(
                            VirtualCard(
                                userId = user.userId,
                                cardNumber = cardNumber,
                                expiryMonth = month,
                                expiryYear = year,
                                cvv = cvv,
                                cardHolder = cardHolder
                            )
                        )
                        showAddCardScreen = false
                    },
                    onCancel = { showAddCardScreen = false }
                )
            } else if (virtualCard != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimension.md),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VisaCardDisplay(
                        cardNumber = virtualCard!!.cardNumber,
                        cardHolder = virtualCard!!.cardHolder,
                        expiryMonth = virtualCard!!.expiryMonth,
                        expiryYear = virtualCard!!.expiryYear
                    )
                }
            }
        }
        /** General options */
        item {
            Text(
                text = "General",
                style = MaterialTheme.typography.body1,
            )
        }
        items(generalOptions) { option ->
            ProfileOptionItem(
                icon = option.icon,
                title = option.title,
                onOptionClicked = {
                    if (option is Screen.Bookmark) {
                        onNavigationRequested(Screen.Bookmark.route, false)
                    }
                },
            )
        }
        /** Personal options */
        item {
            Text(
                text = "Personal",
                style = MaterialTheme.typography.body1,
            )
        }
        items(personalOptions) { option ->
            ProfileOptionItem(
                icon = option.icon,
                title = option.title,
                onOptionClicked = {},
            )
        }
        /** Logout option */
        item {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .fillMaxWidth()
                    .clickable {
                        profileViewModel.logout {
                            onNavigationRequested(Screen.Login.route, true)
                        }
                        onNavigationRequested(Screen.Login.route, true)
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
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
                    text = stringResource(id = R.string.logout),
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
        }
    }
}

@Composable
fun ProfileOptionItem(icon: Int?, title: Int?, onOptionClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .fillMaxWidth()
            .clickable { onOptionClicked() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
    ) {
        DrawableButton(
            painter = rememberAsyncImagePainter(model = icon),
            backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.4f),
            iconTint = MaterialTheme.colors.primary,
            onButtonClicked = {},
            iconSize = Dimension.smIcon,
            paddingValue = PaddingValues(Dimension.md),
            shape = CircleShape,
        )
        title?.let {
            Text(
                text = stringResource(id = title),
                style = MaterialTheme.typography.body1,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f),
            )
        }
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
}

@Composable
fun ProfileHeaderSection(image: Int?, name: String, email: String?, phone: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
    ) {
        AsyncImage(
            modifier = Modifier
                .size(Dimension.xlIcon)
                .clip(CircleShape),
            model = image,
            contentDescription = null,
        )

        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.h5,
            )
            Text(
                text = email ?: "",
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f),
            )
            Text(
                text = phone ?: "",
                style = MaterialTheme.typography.caption
                    .copy(fontWeight = FontWeight.Medium),
            )
        }
    }
}
