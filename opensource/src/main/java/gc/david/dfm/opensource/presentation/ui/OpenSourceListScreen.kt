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

package gc.david.dfm.opensource.presentation.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import gc.david.dfm.designsystem.Spacing
import gc.david.dfm.opensource.R
import gc.david.dfm.opensource.presentation.model.OpenSourceLibraryUiModel
import gc.david.dfm.opensource.presentation.model.OpenSourceUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.OpenSourceListScreen(
    uiState: OpenSourceUiState,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onLibraryClick: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.opensource_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.opensource_navigate_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when (uiState) {
            is OpenSourceUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is OpenSourceUiState.Content -> {
                LazyColumn(
                    modifier = Modifier.padding(innerPadding),
                    contentPadding = PaddingValues(Spacing.m),
                ) {
                    itemsIndexed(uiState.libraries) { index, library ->
                        OpenSourceLibraryCard(
                            library = library,
                            index = index,
                            animatedVisibilityScope = animatedVisibilityScope,
                            onClick = { onLibraryClick(index) },
                        )
                    }
                }
            }

            is OpenSourceUiState.Error -> {
                LaunchedEffect(uiState.message) {
                    snackbarHostState.showSnackbar(uiState.message)
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.OpenSourceLibraryCard(
    library: OpenSourceLibraryUiModel,
    index: Int,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.s)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Spacing.xs),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing.xs),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = Spacing.m,
                vertical = Spacing.l,
            ),
        ) {
            Text(
                text = library.name,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.sharedElement(
                    rememberSharedContentState(key = "name_$index"),
                    animatedVisibilityScope = animatedVisibilityScope,
                ),
            )
            Text(
                text = library.version,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = Spacing.s),
            )
            Text(
                text = library.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Spacing.m),
            )
            Text(
                text = library.licenseTitle,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.m)
                    .sharedElement(
                        rememberSharedContentState(key = "license_$index"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    ),
            )
        }
    }
}
