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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import gc.david.dfm.R
import gc.david.dfm.address.domain.model.Address
import gc.david.dfm.address.domain.model.Coordinates
import gc.david.dfm.designsystem.Spacing

/**
 * Dialog showing address suggestions to select from.
 */
@Composable
fun AddressSuggestionsDialog(
    addresses: List<Address>,
    onAddressSelected: (Address) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_select_address_title)) },
        text = {
            LazyColumn {
                itemsIndexed(addresses) { _, address ->
                    Text(
                        text = address.formattedAddress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onAddressSelected(address)
                                onDismiss()
                            }
                            .padding(vertical = Spacing.m)
                    )
                }
            }
        },
        confirmButton = {},
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun AddressSuggestionsDialogPreview() {
    AddressSuggestionsDialog(
        addresses = listOf(
            Address(
                formattedAddress = "123 Main Street, New York, NY 10001",
                coordinates = Coordinates(40.7128, -74.0060)
            ),
            Address(
                formattedAddress = "456 Park Avenue, New York, NY 10022",
                coordinates = Coordinates(40.7614, -73.9776)
            ),
            Address(
                formattedAddress = "789 Broadway, New York, NY 10003",
                coordinates = Coordinates(40.7308, -73.9973)
            )
        ),
        onAddressSelected = {},
        onDismiss = {}
    )
}

