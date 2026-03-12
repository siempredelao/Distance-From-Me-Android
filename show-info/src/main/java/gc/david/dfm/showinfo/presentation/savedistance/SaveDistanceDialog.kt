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

package gc.david.dfm.showinfo.presentation.savedistance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import gc.david.dfm.designsystem.Spacing
import gc.david.dfm.showinfo.R

@Composable
internal fun SaveDistanceDialog(
    onDismiss: () -> Unit,
    onConfirm: (alias: String) -> Unit,
) {
    var alias by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.alias_dialog_title)) },
        text = {
            Column {
                Text(stringResource(R.string.alias_dialog_message))
                Spacer(Modifier.Companion.height(Spacing.s))
                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Companion.Sentences,
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(alias) }) {
                Text(stringResource(R.string.alias_dialog_accept))
            }
        },
    )
}