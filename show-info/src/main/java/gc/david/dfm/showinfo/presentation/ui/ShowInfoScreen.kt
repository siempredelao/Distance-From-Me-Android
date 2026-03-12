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

package gc.david.dfm.showinfo.presentation.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import gc.david.dfm.designsystem.Spacing
import gc.david.dfm.showinfo.R
import gc.david.dfm.showinfo.presentation.ShowInfoUiState

private val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(Color(0x50526D02), Color(0x00526D02)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShowInfoScreen(
    uiState: ShowInfoUiState,
    saveUserMessage: String?,
    onBackPress: () -> Unit,
    onShare: () -> Unit,
    onRefresh: () -> Unit,
    onSave: () -> Unit,
    onUserMessageShown: () -> Unit,
    onShareDialogShown: () -> Unit,
    onSaveUserMessageShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onUserMessageShown()
        }
    }

    LaunchedEffect(uiState.shareIntentData) {
        uiState.shareIntentData?.let { data ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, data.subject)
                putExtra(Intent.EXTRA_TEXT, data.message)
            }
            context.startActivity(Intent.createChooser(shareIntent, data.title))
            onShareDialogShown()
        }
    }

    LaunchedEffect(saveUserMessage) {
        saveUserMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            onSaveUserMessageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                title = { Text(stringResource(R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.action_bar_item_social_share_title),
                        )
                    }
                    if (uiState.isLoading) {
                        IconButton(onClick = {}) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                        }
                    } else {
                        IconButton(onClick = onRefresh) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.action_bar_item_refresh_title),
                            )
                        }
                    }
                    IconButton(onClick = onSave) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = stringResource(R.string.action_bar_item_save_title),
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGradient)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.m),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.info_current_position_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(Spacing.xxs))
            Text(
                text = uiState.originAddress,
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(Modifier.height(30.dp))

            Text(
                text = stringResource(R.string.info_destination_position_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(Spacing.xxs))
            Text(
                text = uiState.destinationAddress,
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(Modifier.height(Spacing.l))

            Text(
                text = uiState.distanceMessage,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

