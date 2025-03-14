package dev.vstd.shoppingcart.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.keego.shoppingcart.R
import dev.vstd.shoppingcart.auth.data.UserRepository
import dev.vstd.shoppingcart.common.theme.ButtonRadius
import dev.vstd.shoppingcart.common.ui.base.InuFullWidthButton
import dev.vstd.shoppingcart.common.ui.base.InuTextField
import dev.vstd.shoppingcart.common.utils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@AuthNavGraph
@Destination
@Composable
fun signup_(navigator: DestinationsNavigator) {
    val hostState = remember { SnackbarHostState() }
    Scaffold(snackbarHost = {
        SnackbarHost(hostState = hostState) {
            Snackbar(
                snackbarData = it,
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = Color.White
            )
        }
    }) {
        Column(Modifier.padding(it)) {
            body_(hostState, navigator)
        }
    }
}

@Composable
private fun body_(hostState: SnackbarHostState, navigator: DestinationsNavigator) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userRepository = (context as AuthActivity).userRepository

    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var reconfirmPassword by remember { mutableStateOf("") }

//    Column {
//        IconButton(onClick = navigator::navigateUp) {
//            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
//        }
//
//        Button(onClick = {
//            val result = SignUpValidator.validate(username, email, password, reconfirmPassword)
//            if (result.success) {
//                signup(userService, email = email, username = username, password)
//            } else {
//                scope.launch { hostState.showSnackbar(result.message) }
//            }
//        }) {
//            Text(text = "Sign Up")
//        }
//    }
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = rememberAsyncImagePainter(model = R.drawable.bg_signup),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Create\nAccount",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(top = 96.dp, start = 16.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .padding(bottom = 16.dp)
                .padding(horizontal = 16.dp)
                .align(Alignment.BottomCenter)
        ) {
            Image(
                painterResource(id = R.drawable.ic_upload_photo),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(0.2f)
            )
            InuTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            InuTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            InuTextField(
                value = password,
                visualTransformation = PasswordVisualTransformation(),
                onValueChange = { password = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )
            InuTextField(
                value = reconfirmPassword,
                visualTransformation = PasswordVisualTransformation(),
                onValueChange = { reconfirmPassword = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                label = { Text("Reconfirm Password") },
                modifier = Modifier.fillMaxWidth()
            )
            InuFullWidthButton(
                onClick = {
                    val result =
                        SignUpValidator.validate(username, email, password, reconfirmPassword)
                    if (result.success) {
                        signup(
                            scope,
                            userRepository,
                            email = email,
                            username = username,
                            password
                        ) {
                            context.toast("Sign up successful. Please sign in.")
                            navigator.navigateUp()
                        }
                    } else {
                        scope.launch { hostState.showSnackbar(result.message) }
                    }
                }
            ) {
                Text(text = "Sign Up", fontSize = 20.sp)
            }
            TextButton(
                onClick = { navigator.navigateUp() },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(ButtonRadius)
            ) {
                Text(text = "Cancel")
            }
        }
    }
}

private fun signup(
    scope: CoroutineScope,
    userService: UserRepository,
    email: String,
    username: String,
    password: String,
    onSuccess: () -> Unit
) {
    scope.launch {
        val resp = userService.signUp(
            username = username,
            email = email,
            password = password
        )
        if (resp.isSuccessful) {
            Timber.d("Signup successful")
            onSuccess()
        } else {
            Timber.e("${resp.code()}: ${resp.errorBody()?.string()}")
        }
    }
}
