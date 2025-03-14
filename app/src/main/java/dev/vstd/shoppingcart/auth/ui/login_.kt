package dev.vstd.shoppingcart.auth.ui

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.keego.shoppingcart.R
import dev.vstd.shoppingcart.auth.Session
import dev.vstd.shoppingcart.auth.data.UserRepository
import dev.vstd.shoppingcart.auth.ui.destinations.signup_Destination
import dev.vstd.shoppingcart.common.ui.base.InuFullWidthButton
import dev.vstd.shoppingcart.common.ui.base.InuTextField
import dev.vstd.shoppingcart.common.utils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@AuthNavGraph(start = true)
@Destination
@Composable
fun login_(navigator: DestinationsNavigator) {
    val hostState = remember { SnackbarHostState() }
    Scaffold(snackbarHost = {
        SnackbarHost(hostState) {
            Snackbar(
                snackbarData = it,
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = Color.White
            )
        }
    }) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            body_(navigator, hostState)
        }
    }
}

@Composable
private fun body_(navigator: DestinationsNavigator, hostState: SnackbarHostState) {
    var email by remember { mutableStateOf("admin@gmail.com") }
    var password by remember { mutableStateOf("123456") }

    val context = LocalContext.current
    val userRepository = (context as AuthActivity).userRepository
    val scope = rememberCoroutineScope()

    Box {
        Image(
            painter = rememberAsyncImagePainter(model = R.drawable.bg_login),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.displayLarge,
            )
            Text(
                text = "Good to see you back! ❤",
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
            InuTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                value = email,
                onValueChange = { email = it },
                label = { Text(text = "Email") }
            )
            InuTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                value = password,
                onValueChange = { password = it },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                label = { Text(text = "Password") }
            )
            InuFullWidthButton(
                onClick = {
                    val result =
                        LoginValidator.validate(email, password)
                    if (result.success) {
                        login(scope, userRepository, email, password, onFailed = {
                            scope.launch { hostState.showSnackbar(it) }
                        }) {
                            context.toast("Login successful")
                            (context as Activity).finish()
                        }
                    } else {
                        scope.launch { hostState.showSnackbar(result.message) }
                    }
                },
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text(text = "Login", fontSize = 20.sp)
            }
            TextButton(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    navigator.navigate(signup_Destination)
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sign up")
                    Icon(
                        imageVector = Icons.Default.NavigateNext,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

private fun login(
    scope: CoroutineScope,
    repo: UserRepository,
    email: String,
    password: String,
    onFailed: (String) -> Unit,
    onSuccess: () -> Unit,
) {
    scope.launch {
        val resp = repo.login(email, password)
        if (resp.isSuccessful) {
            val loggedInInfo = resp.body()!!
            Timber.d("Login successful, username=${loggedInInfo.username}")
            Session.userEntity.value = loggedInInfo
            onSuccess()
        } else {
            val errorBody = resp.errorBody()?.string()
            Timber.e("Login failed: ${resp.code()}: ${errorBody}")
            onFailed(errorBody ?: "Unknown error")
        }
    }
}
