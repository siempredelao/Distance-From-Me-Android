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

package gc.david.dfm.connectivity

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun ConnectionIssuesDialog(
    onPositiveButtonClick: () -> Unit,
    onNegativeButtonClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onNegativeButtonClick,
        title = { Text(stringResource(R.string.dialog_connection_problems_title)) },
        text = { Text(stringResource(R.string.dialog_connection_problems_message)) },
        confirmButton = {
            TextButton(onClick = onPositiveButtonClick) {
                Text(stringResource(R.string.dialog_connection_problems_positive_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onNegativeButtonClick) {
                Text(stringResource(R.string.dialog_connection_problems_negative_button))
            }
        },
    )
}
