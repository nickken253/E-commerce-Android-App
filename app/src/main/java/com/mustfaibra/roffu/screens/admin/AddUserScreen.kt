package com.mustfaibra.roffu.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mustfaibra.roffu.models.User
@Composable
fun AddUserScreen(
    onBackRequested: () -> Unit,
    onToastRequested: (message: String, color: Color) -> Unit,
    viewModel: AdminViewModel = hiltViewModel(),
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("user") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            elevation = 8.dp,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add new user",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.primary
                )

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

                /** Vai trò */
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RoleButton(title = "User", selected = role == "user") {
                        role = "user"
                    }
                    RoleButton(title = "Admin", selected = role == "admin") {
                        role = "admin"
                    }
                }

                /** Nút hành động */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = onBackRequested) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val newUser = User(
                                userId = 0,
                                name = name,
                                email = email.takeIf { it.isNotBlank() },
                                phone = phone.takeIf { it.isNotBlank() },
                                address = address.takeIf { it.isNotBlank() },
                                password = password.takeIf { it.isNotBlank() },
                                role = role
                            )
                            viewModel.addUser(newUser) { message, success ->
                                onToastRequested(message, if (success) Color.Green else Color.Red)
                                if (success) onBackRequested()
                            }
                        },
                        enabled = name.isNotBlank() && password.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun RoleButton(title: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
            contentColor = if (selected) Color.White else MaterialTheme.colors.onSurface
        )

    ) {
        Text(title)
    }
}
