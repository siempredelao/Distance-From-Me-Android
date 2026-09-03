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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gc.david.dfm.R
import gc.david.dfm.designsystem.Spacing
import gc.david.dfm.main.presentation.model.SideNavigationItemId

/**
 * Expandable navigation rail with all main navigation options.
 * Shows icons always, labels only when expanded.
 */
@Composable
fun AppNavigationRail(
    isExpanded: Boolean,
    selectedItemId: SideNavigationItemId?,
    onItemClick: (SideNavigationItemId) -> Unit,
    onClose: () -> Unit,
    showLoadMenuItem: Boolean,
    showCrashMenuItem: Boolean,
    modifier: Modifier = Modifier,
) {
    NavigationRail(
        modifier = modifier
            .animateContentSize()
            .width(if (isExpanded) 256.dp else 72.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        header = {
            NavigationRailHeader(
                isExpanded = isExpanded,
                onClose = onClose
            )
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            val currentPositionLabel =
                stringResource(R.string.navigation_drawer_starting_point_current_position_item)
            NavigationRailItem(
                icon = { Icon(Icons.Default.MyLocation, contentDescription = currentPositionLabel) },
                label = { if (isExpanded) Text(currentPositionLabel) },
                selected = selectedItemId == SideNavigationItemId.CURRENT_POSITION,
                onClick = { onItemClick(SideNavigationItemId.CURRENT_POSITION) }
            )

            val anyPositionLabel =
                stringResource(R.string.navigation_drawer_starting_point_any_position_item)
            NavigationRailItem(
                icon = { Icon(Icons.Default.Place, contentDescription = anyPositionLabel) },
                label = { if (isExpanded) Text(anyPositionLabel) },
                selected = selectedItemId == SideNavigationItemId.ANY_POSITION,
                onClick = { onItemClick(SideNavigationItemId.ANY_POSITION) }
            )

            if (showLoadMenuItem) {
                Spacer(modifier = Modifier.height(Spacing.m))

                val loadDistancesLabel = stringResource(R.string.menu_load_title)
                NavigationRailItem(
                    icon = { Icon(Icons.Default.FolderOpen, contentDescription = loadDistancesLabel) },
                    label = { if (isExpanded) Text(loadDistancesLabel) },
                    selected = false,
                    onClick = { onItemClick(SideNavigationItemId.LOAD) }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.m))

            val rateAppLabel = stringResource(R.string.menu_rate_app_title)
            NavigationRailItem(
                icon = { Icon(Icons.Default.Star, contentDescription = rateAppLabel) },
                label = { if (isExpanded) Text(rateAppLabel) },
                selected = false,
                onClick = { onItemClick(SideNavigationItemId.RATE_APP) }
            )

            val settingsLabel = stringResource(R.string.menu_settings_title)
            NavigationRailItem(
                icon = { Icon(Icons.Default.Settings, contentDescription = settingsLabel) },
                label = { if (isExpanded) Text(settingsLabel) },
                selected = false,
                onClick = { onItemClick(SideNavigationItemId.SETTINGS) }
            )

            val faqLabel = stringResource(R.string.menu_help_feedback_title)
            NavigationRailItem(
                icon = { Icon(Icons.AutoMirrored.Filled.Help, contentDescription = faqLabel) },
                label = { if (isExpanded) Text(faqLabel) },
                selected = false,
                onClick = { onItemClick(SideNavigationItemId.HELP_FEEDBACK) }
            )

            val openSourceLabel = stringResource(R.string.menu_about_title)
            NavigationRailItem(
                icon = { Icon(Icons.Default.Info, contentDescription = openSourceLabel) },
                label = { if (isExpanded) Text(openSourceLabel) },
                selected = false,
                onClick = { onItemClick(SideNavigationItemId.ABOUT) }
            )

            if (showCrashMenuItem) {
                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.m))
                Text(text = "Debug section", textAlign = TextAlign.Center, color = Color.Red)
                Spacer(modifier = Modifier.height(Spacing.m))
                val crashLabel = "Crash"
                NavigationRailItem(
                    icon = { Icon(Icons.Default.Bolt, tint = Color.Red, contentDescription = crashLabel) },
                    label = { if (isExpanded) Text(crashLabel, color = Color.Red) },
                    selected = false,
                    onClick = { onItemClick(SideNavigationItemId.CRASH) }
                )
            }
        }
    }
}

@Composable
private fun NavigationRailHeader(
    isExpanded: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.navigation_view_header_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit
                )

                if (isExpanded) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = stringResource(R.string.app_name),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuOpen,
                contentDescription = "Close Navigation"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppNavigationRailCollapsedPreview() {
    AppNavigationRail(
        isExpanded = false,
        selectedItemId = SideNavigationItemId.CURRENT_POSITION,
        onItemClick = {},
        onClose = {},
        showLoadMenuItem = true,
        showCrashMenuItem = true
    )
}

@Preview(showBackground = true)
@Composable
private fun AppNavigationRailExpandedPreview() {
    AppNavigationRail(
        isExpanded = true,
        selectedItemId = SideNavigationItemId.ANY_POSITION,
        onItemClick = {},
        onClose = {},
        showLoadMenuItem = true,
        showCrashMenuItem = true
    )
}

