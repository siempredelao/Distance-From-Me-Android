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

package gc.david.dfm.onboarding.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import gc.david.dfm.R
import gc.david.dfm.designsystem.DfmTheme
import gc.david.dfm.designsystem.Spacing

/**
 * Tells the user what to do next on the map. Without [onDismiss] there is no close button.
 */
@Composable
fun MapHintBanner(
    text: String,
    onDismiss: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 3.dp,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            // As tall as the close button, also without it
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(horizontal = Spacing.m, vertical = Spacing.s)
                    .semantics { liveRegion = LiveRegionMode.Polite }
            )
            if (onDismiss != null) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.map_hint_close)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MapHintBannerPreview() {
    DfmTheme {
        MapHintBanner(text = stringResource(R.string.map_hint_add_or_finish), onDismiss = {})
    }
}

@Preview
@Composable
private fun MapHintBannerWithoutButtonPreview() {
    DfmTheme {
        MapHintBanner(text = stringResource(R.string.map_hint_waiting_for_location), onDismiss = null)
    }
}
