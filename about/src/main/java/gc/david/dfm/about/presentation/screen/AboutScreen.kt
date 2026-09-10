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

package gc.david.dfm.about.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import gc.david.dfm.about.R
import gc.david.dfm.designsystem.DfmTheme
import gc.david.dfm.designsystem.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    versionName: String,
    onBack: () -> Unit,
    onOpenLink: (String) -> Unit,
    onOpenEmail: () -> Unit,
    onOpenSourceLicenses: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
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
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Spacing.m),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            item {
                HeaderSection()
            }
            item {
                InfoCard(
                    title = stringResource(R.string.greeting_title),
                    text = stringResource(R.string.greeting_message),
                )
            }
            item {
                InfoCard(
                    title = stringResource(R.string.version_title),
                    text = versionName,
                )
            }
            item {
                InfoCard(
                    title = stringResource(R.string.whats_new_title),
                    text = stringResource(R.string.whats_new_message),
                )
            }
            item {
                LinksCard(
                    onOpenLink = onOpenLink,
                    onOpenSourceLicenses = onOpenSourceLicenses,
                )
            }
            item {
                InfoCard(
                    title = stringResource(R.string.translations_title),
                    text = stringResource(R.string.translations_message),
                    onClick = onOpenEmail,
                )
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(Spacing.l),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = gc.david.dfm.common.R.mipmap.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .height(72.dp),
                contentScale = ContentScale.Fit,
            )

            Spacer(modifier = Modifier.height(Spacing.s))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun CommonCard(
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .let {
                if (onClick != null) it.clickable(enabled = true, onClick = onClick) else it
            },
        shape = RoundedCornerShape(Spacing.m),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.m),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            content()
        }
    }
}

@Composable
private fun InfoCard(title: String, text: String, onClick: (() -> Unit)? = null) {
    CommonCard(onClick = onClick) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun LinksCard(
    onOpenLink: (String) -> Unit,
    onOpenSourceLicenses: () -> Unit,
) {
    CommonCard {
        Text(
            text = stringResource(R.string.links_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        LinkAction(
            label = stringResource(R.string.link_github),
            url = "https://github.com/siempredelao",
            onOpenLink = onOpenLink,
        )

        LinkAction(
            label = stringResource(R.string.link_google_play),
            url = "https://play.google.com/store/apps/details?id=gc.david.dfm",
            onOpenLink = onOpenLink,
        )

        LinkAction(
            label = stringResource(R.string.link_open_source_licenses),
            onClick = onOpenSourceLicenses,
        )
    }
}

@Composable
private fun LinkAction(
    label: String,
    url: String? = null,
    onClick: (() -> Unit)? = null,
    onOpenLink: ((String) -> Unit)? = null,
) {
    val action = if (url != null && onOpenLink != null) {
        { onOpenLink(url) }
    } else {
        onClick
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = action != null) { action?.invoke() }
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Preview
@Composable
private fun AboutScreenPreview() {
    DfmTheme {
        AboutScreen(
            versionName = "1.0.0",
            onBack = {},
            onOpenLink = {},
            onOpenEmail = {},
            onOpenSourceLicenses = {},
        )
    }
}
