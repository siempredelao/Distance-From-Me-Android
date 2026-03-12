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

package gc.david.dfm.feedback

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import gc.david.dfm.designsystem.DfmTheme
import timber.log.Timber

class RateAppFallbackDialogFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            setContent {
                DfmTheme {
                    RateAppFallbackDialog(
                        onDismiss = { dismiss() },
                        onCtaClick = {
                            dismiss()
                            openPlayStoreAppPage()
                        },
                    )
                }
            }
        }

    private fun openPlayStoreAppPage() {
        Timber.tag(TAG).d("openPlayStoreAppPage")

        try {
            startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=gc.david.dfm".toUri()))
        } catch (e: ActivityNotFoundException) {
            Timber.tag(TAG).e(e, "Unable to open Play Store, rooted device?")
        }
    }

    companion object {

        private const val TAG = "RateAppFallbackDialog"
    }
}
