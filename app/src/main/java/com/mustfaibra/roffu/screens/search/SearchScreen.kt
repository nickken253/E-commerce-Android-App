package com.mustfaibra.roffu.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchHistory by viewModel.searchHistory.collectAsState()
    val searchSuggestions by viewModel.searchSuggestions.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Column {
        // Search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    viewModel.onSearchQueryChanged(it)
                },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Tìm kiếm...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        viewModel.onSearch(searchQuery)
                    }
                )
            )
            IconButton(
                onClick = {
                    viewModel.onSearch(searchQuery)
                }
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }

        // Search history and suggestions
        LazyColumn {
            if (searchHistory.isNotEmpty()) {
                item {
                    Text(
                        text = "Lịch sử tìm kiếm",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(searchHistory) { history ->
                    Text(
                        text = history,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                searchQuery = history
                                viewModel.onSearchQueryChanged(history)
                            }
                            .padding(16.dp)
                    )
                }
            }

            item {
                Text(
                    text = "Gợi ý tìm kiếm",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(searchSuggestions) { suggestion ->
                Text(
                    text = suggestion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            searchQuery = suggestion
                            viewModel.onSearchQueryChanged(suggestion)
                        }
                        .padding(16.dp)
                )
            }
        }
    }
}