/*
 * Copyright (c) 2026 David Aguiar Gonzalez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gc.david.dfm.main.presentation.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import gc.david.dfm.R

/**
 * Transparent search bar with gradient background for readability.
 * Default: menu and search icons. Active: back to collapse and an outlined search field.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransparentSearchBar(
    onMenuClick: () -> Unit,
    onSearchQuery: (String) -> Unit,
    modifier: Modifier = Modifier,
    initiallySearchActive: Boolean = false,
) {
    var searchText by rememberSaveable { mutableStateOf("") }
    var isSearchActive by rememberSaveable { mutableStateOf(initiallySearchActive) }
    val focusRequester = remember { FocusRequester() }

    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black,
                        Color.Black.copy(alpha = 0.3F)
                    )
                )
            ),
        contentAlignment = Alignment.TopStart
    ) {
        TopAppBar(
            title = {
                if (isSearchActive) {
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = {
                            Text(stringResource(R.string.action_bar_search_hint))
                        },
                        singleLine = true,
                        trailingIcon = {
                            if (searchText.isNotEmpty()) {
                                IconButton(onClick = { searchText = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.search_bar_clear)
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (searchText.isNotEmpty()) {
                                    onSearchQuery(searchText)
                                    isSearchActive = false
                                    searchText = ""
                                }
                            }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.9f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier
                            .widthIn(max = 300.dp)
                            .focusRequester(focusRequester)
                    )
                }
            },
            navigationIcon = {
                if (isSearchActive) {
                    IconButton(onClick = { isSearchActive = false }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.search_bar_close),
                            tint = Color.White
                        )
                    }
                } else {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(R.string.search_bar_open_menu),
                            tint = Color.White
                        )
                    }
                }
            },
            actions = {
                if (!isSearchActive) {
                    IconButton(onClick = { isSearchActive = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.action_bar_search_title),
                            tint = Color.White
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransparentSearchBarPreview() {
    TransparentSearchBar(
        onMenuClick = {},
        onSearchQuery = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun TransparentSearchBarActivePreview() {
    TransparentSearchBar(
        onMenuClick = {},
        onSearchQuery = {},
        initiallySearchActive = true
    )
}
