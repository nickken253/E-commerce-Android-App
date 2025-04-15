package com.mustfaibra.roffu.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mustfaibra.roffu.models.ProductColor

@Composable
fun ColorItem(
    color: ProductColor,
    drawableList: List<Pair<String, Int>>,
    onUpdate: (ProductColor) -> Unit,
    onDelete: () -> Unit,
) {
    var colorName by remember { mutableStateOf(color.colorName) }
    var selectedColorImage by remember { mutableStateOf(drawableList.find { it.second == color.image }?.first ?: "") }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = colorName,
                onValueChange = {
                    colorName = it
                    onUpdate(color.copy(colorName = it))
                },
                label = { Text("Name color") },
                modifier = Modifier.weight(1f)
            )
            Box {
                OutlinedTextField(
                    value = selectedColorImage,
                    onValueChange = { /* Không cho phép nhập tay */ },
                    label = { Text("Color image") },
                    enabled = false,
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    drawableList.forEach { (name, resId) ->
                        DropdownMenuItem(
                            onClick = {
                                selectedColorImage = name
                                expanded = false
                                onUpdate(color.copy(image = resId))
                            }
                        ) {
                            Text(name)
                        }
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete color", tint = Color.Red)
            }
        }
    }
}