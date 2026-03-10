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

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import gc.david.dfm.designsystem.DfmTheme
import timber.log.Timber

object ConnectionIssuesDialogHandler {

    private const val TAG = "ConnectionIssuesDialog"

    fun show(activity: Activity) {
        Timber.tag(TAG).d("show")

        val contentView = activity.findViewById<ViewGroup>(android.R.id.content)
        val composeView = ComposeView(activity).apply {
            setContent {
                DfmTheme {
                    ConnectionIssuesDialog(
                        onNegativeButtonClick = { contentView.removeView(this@apply) },
                        onPositiveButtonClick = {
                            contentView.removeView(this@apply)
                            activity.startActivity(Intent(Settings.ACTION_SETTINGS))
                        },
                    )
                }
            }
        }
        contentView.addView(composeView)
    }
}
