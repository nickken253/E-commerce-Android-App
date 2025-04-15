package com.mustfaibra.roffu.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mustfaibra.roffu.models.User

@Composable
fun EditUserScreen(
    userId: Int,
    onBackRequested: () -> Unit,
    onToastRequested: (message: String, color: Color) -> Unit,
    viewModel: AdminViewModel = hiltViewModel(),
) {
    val users by viewModel.users.collectAsState()
    val user = users.find { it.userId == userId } ?: return // Thoát nếu không tìm thấy user

    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email ?: "") }
    var phone by remember { mutableStateOf(user.phone ?: "") }
    var address by remember { mutableStateOf(user.address ?: "") }
    var password by remember { mutableStateOf(user.password ?: "") }
    var role by remember { mutableStateOf(user.role) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Edit User", style = MaterialTheme.typography.h6)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { role = "user" },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (role == "user") MaterialTheme.colors.primary else MaterialTheme.colors.surface
                )
            ) {
                Text("User")
            }
            Button(
                onClick = { role = "admin" },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (role == "admin") MaterialTheme.colors.primary else MaterialTheme.colors.surface
                )
            ) {
                Text("Admin")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBackRequested) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    val updatedUser = user.copy(
                        name = name,
                        email = email.takeIf { it.isNotBlank() },
                        phone = phone.takeIf { it.isNotBlank() },
                        address = address.takeIf { it.isNotBlank() },
                        password = password.takeIf { it.isNotBlank() },
                        role = role
                    )
                    viewModel.updateUser(updatedUser) { message, success ->
                        onToastRequested(message, if (success) Color.Green else Color.Red)
                        if (success) onBackRequested()
                    }
                },
                enabled = name.isNotBlank() && password.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }
}