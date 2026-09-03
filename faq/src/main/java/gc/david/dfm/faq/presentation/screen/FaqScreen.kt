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

package gc.david.dfm.faq.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import gc.david.dfm.designsystem.DfmTheme
import gc.david.dfm.designsystem.Spacing
import gc.david.dfm.faq.R
import gc.david.dfm.faq.domain.model.Faq
import gc.david.dfm.faq.presentation.model.FaqUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(
    uiState: FaqUiState,
    onBack: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.faq_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.faq_navigate_back),
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
            is FaqUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is FaqUiState.Content -> {
                LazyColumn(
                    modifier = Modifier.padding(innerPadding),
                    contentPadding = PaddingValues(Spacing.s),
                ) {
                    items(uiState.faqs) { faq ->
                        FaqCard(faq = faq)
                    }
                }
            }

            is FaqUiState.Error -> {
                LaunchedEffect(uiState.message) {
                    snackbarHostState.showSnackbar(uiState.message)
                }
            }
        }
    }
}

@Composable
private fun FaqCard(faq: Faq) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(Spacing.xs),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = Spacing.m,
                vertical = Spacing.l,
            ),
        ) {
            Text(
                text = faq.question,
                style = MaterialTheme.typography.titleSmall,
            )
            AnimatedVisibility(visible = expanded) {
                Text(
                    text = faq.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = Spacing.m),
                )
            }
        }
    }
}

@Preview
@Composable
private fun FaqScreenLoadingPreview() {
    DfmTheme {
        FaqScreen(
            uiState = FaqUiState.Loading,
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun FaqScreenContentPreview() {
    DfmTheme {
        FaqScreen(
            uiState = FaqUiState.Content(
                faqs = listOf(
                    Faq(
                        question = "How do I measure a distance?",
                        answer = "Tap on the map to set the origin and destination points. The distance will be calculated automatically."
                    ),
                    Faq(
                        question = "Can I save my measurements?",
                        answer = "Yes, you can save your distance measurements by tapping the save button in the info screen."
                    ),
                    Faq(
                        question = "How accurate are the measurements?",
                        answer = "The measurements are based on GPS coordinates and use the Haversine formula for accuracy."
                    ),
                )
            ),
            onBack = {},
        )
    }
}
