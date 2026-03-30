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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import gc.david.dfm.R
import gc.david.dfm.core.distances.domain.model.Distance
import gc.david.dfm.designsystem.Spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dialog showing saved distances to load.
 */
@Composable
fun DistanceSelectionDialog(
    distances: List<Distance>,
    onDistanceSelected: (Distance) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_load_distances_title)) },
        text = {
            LazyColumn {
                itemsIndexed(distances) { _, distance ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDistanceSelected(distance)
                                onDismiss()
                            }
                            .padding(vertical = Spacing.s)
                    ) {
                        Text(
                            text = distance.name,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = distance.distance,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    .format(distance.date),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun DistanceSelectionDialogPreview() {
    DistanceSelectionDialog(
        distances = listOf(
            Distance(
                id = 1,
                name = "Home to Office",
                distance = "5.2 km",
                date = Date()
            ),
            Distance(
                id = 2,
                name = "Park to Library",
                distance = "2.8 km",
                date = Date()
            ),
            Distance(
                id = 3,
                name = "Airport to Hotel",
                distance = "15.7 km",
                date = Date()
            )
        ),
        onDistanceSelected = {},
        onDismiss = {}
    )
}

