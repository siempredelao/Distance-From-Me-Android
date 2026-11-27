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

import android.app.Activity
import android.content.Intent
import androidx.core.net.toUri
import gc.david.dfm.support.android.UiUtils
import gc.david.dfm.support.android.tryStartActivity
import gc.david.dfm.support.android.R as SupportAndroidR

private const val PLAY_STORE_APP_URI = "market://details?id=gc.david.dfm"
private const val PLAY_STORE_WEB_URI = "https://play.google.com/store/apps/details?id=gc.david.dfm"

/**
 * Opens the app's page in the Play Store app, or in a browser on devices without it.
 */
internal fun Activity.openPlayStorePage() {
    val isOpen = tryStartActivity(Intent(Intent.ACTION_VIEW, PLAY_STORE_APP_URI.toUri())) ||
            tryStartActivity(Intent(Intent.ACTION_VIEW, PLAY_STORE_WEB_URI.toUri()))
    if (!isOpen) UiUtils.toastIt(SupportAndroidR.string.toast_no_browser_app, this)
}
