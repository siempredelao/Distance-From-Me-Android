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

package gc.david.dfm.about.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.net.toUri
import gc.david.dfm.about.R
import gc.david.dfm.about.presentation.screen.AboutScreen
import gc.david.dfm.designsystem.DfmTheme
import gc.david.dfm.opensource.presentation.OpenSourceLicensesActivity

class AboutActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
            ?: getString(R.string.version_unknown)

        setContent {
            DfmTheme {
                AboutScreen(
                    versionName = versionName,
                    onBack = { onBackPressedDispatcher.onBackPressed() },
                    onOpenLink = { url -> startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) },
                    onOpenEmail = {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:davidaguiargonzalez@gmail.com".toUri()
                            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.translations_email_subject))
                        }
                        startActivity(emailIntent)
                    },
                    onOpenSourceLicenses = {
                        OpenSourceLicensesActivity.open(this@AboutActivity)
                    },
                )
            }
        }
    }

    companion object {

        fun open(activity: Activity) {
            val intent = Intent(activity, AboutActivity::class.java)
            activity.startActivity(intent)
        }
    }
}
