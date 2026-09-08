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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import gc.david.dfm.R
import gc.david.dfm.designsystem.DfmGreen
import gc.david.dfm.designsystem.Spacing
import gc.david.dfm.main.presentation.model.DistanceSelectionUiModel

/**
 * Dialog showing saved distances to load.
 */
@Composable
fun DistanceSelectionDialog(
    list: List<DistanceSelectionUiModel>,
    onDistanceSelected: (id: Long, name: String) -> Unit,
    onRateAppClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_load_distances_title)) },
        text = {
            LazyColumn {
                items(list) { item ->
                    when (item) {
                        is DistanceSelectionUiModel.Distance ->
                            Distance(item, onDistanceSelected, onDismiss)

                        is DistanceSelectionUiModel.RateApp ->
                            RateCta(item, onRateAppClick, onDismiss)
                    }
                }
            }
        },
        confirmButton = {},
        modifier = modifier
    )
}

@Composable
private fun Distance(
    item: DistanceSelectionUiModel.Distance,
    onDistanceSelected: (id: Long, name: String) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onDistanceSelected(item.id, item.name)
                onDismiss()
            }
            .padding(vertical = Spacing.s)
    ) {
        Text(
            text = item.name,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.distance,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = item.date,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun RateCta(
    item: DistanceSelectionUiModel.RateApp,
    onRateAppClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs)
            .clip(RoundedCornerShape(Spacing.l))
            .background(DfmGreen)
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.title,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1F)
                .padding(horizontal = Spacing.s)
        )
        OutlinedButton(
            onClick = {
                onRateAppClick()
                onDismiss()
            },
            colors = ButtonDefaults.outlinedButtonColors().copy(containerColor = Color.White),
            modifier = Modifier.padding(end = Spacing.s)
        ) {
            Text(text = item.ctaText, color = DfmGreen)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DistanceSelectionDialogPreview() {
    DistanceSelectionDialog(
        list = listOf(
            DistanceSelectionUiModel.Distance(
                id = 1,
                name = "Home to Office",
                distance = "5.2 km",
                date = "Sunday 28th November 2025"
            ),
            DistanceSelectionUiModel.Distance(
                id = 2,
                name = "Park to Library",
                distance = "2.8 km",
                date = "Sunday 28th November 2025"
            ),
            DistanceSelectionUiModel.RateApp(
                title = "Do you enjoy using this app?",
                ctaText = "Rate it now"
            ),
            DistanceSelectionUiModel.Distance(
                id = 3,
                name = "Airport to Hotel",
                distance = "15.7 km",
                date = "Sunday 28th November 2025"
            )
        ),
        onDistanceSelected = { _, _ -> },
        onRateAppClick = {},
        onDismiss = {}
    )
}

