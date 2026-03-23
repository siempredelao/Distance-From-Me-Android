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

package gc.david.dfm.opensource.presentation.screen

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import gc.david.dfm.designsystem.DfmTheme
import gc.david.dfm.designsystem.Spacing
import gc.david.dfm.opensource.R
import gc.david.dfm.opensource.presentation.model.OpenSourceLibraryUiModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.OpenSourceDetailScreen(
    library: OpenSourceLibraryUiModel,
    index: Int,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit,
    onOpenInBrowser: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(library.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.opensource_navigate_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onOpenInBrowser(library.link) }) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = stringResource(R.string.opensource_open_in_browser),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Spacing.m)
                .verticalScroll(rememberScrollState()),
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
                text = library.licenseDescription,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(top = Spacing.m)
                    .sharedElement(
                        rememberSharedContentState(key = "license_$index"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    ),
            )
        }
    }
}

@Preview
@Composable
private fun OpenSourceDetailScreenPreview() {
    DfmTheme {
        androidx.compose.animation.SharedTransitionLayout {
            androidx.compose.animation.AnimatedVisibility(visible = true) {
                OpenSourceDetailScreen(
                    library = OpenSourceLibraryUiModel(
                        name = "Kotlin",
                        description = "The Kotlin Programming Language",
                        author = "JetBrains",
                        version = "1.9.0",
                        link = "https://kotlinlang.org/",
                        licenseTitle = "Apache License 2.0",
                        licenseDescription = "Licensed under the Apache License, Version 2.0 (the \"License\");",
                        year = "2024"
                    ),
                    index = 0,
                    animatedVisibilityScope = this,
                    onBack = {},
                    onOpenInBrowser = {},
                )
            }
        }
    }
}
