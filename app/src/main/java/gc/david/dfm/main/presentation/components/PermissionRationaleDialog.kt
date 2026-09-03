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

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import gc.david.dfm.R

/**
 * Dialog explaining why location permission is needed.
 * Shown before requesting permissions to improve user acceptance.
 */
@Composable
fun PermissionRationaleDialog(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.permission_rationale_title)) },
        text = {
            Text(stringResource(R.string.permission_rationale_message))
        },
        confirmButton = {
            TextButton(onClick = onRequestPermission) {
                Text(stringResource(R.string.permission_rationale_grant))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.permission_rationale_cancel))
            }
        },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun PermissionRationaleDialogPreview() {
    PermissionRationaleDialog(
        onRequestPermission = {},
        onDismiss = {}
    )
}

