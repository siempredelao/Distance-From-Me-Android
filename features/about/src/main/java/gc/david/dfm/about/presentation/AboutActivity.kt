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
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.core.net.toUri
import gc.david.dfm.about.R
import gc.david.dfm.about.presentation.screen.AboutScreen
import gc.david.dfm.support.android.R as SupportAndroidR
import gc.david.dfm.designsystem.DfmTheme
import gc.david.dfm.support.android.UiUtils
import gc.david.dfm.support.android.tryStartActivity
import gc.david.dfm.support.navigation.Navigator
import gc.david.dfm.support.navigation.Route
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class AboutActivity : ComponentActivity() {

    private val navigator: Navigator by inject { parametersOf(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The top bar is always green and the content always light, whatever the system theme
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )

        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
            ?: getString(R.string.version_unknown)

        setContent {
            DfmTheme {
                AboutScreen(
                    versionName = versionName,
                    onBack = { onBackPressedDispatcher.onBackPressed() },
                    onOpenLink = { url ->
                        openOrToast(
                            Intent(Intent.ACTION_VIEW, url.toUri()),
                            SupportAndroidR.string.toast_no_browser_app
                        )
                    },
                    onOpenEmail = {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:davidaguiargonzalez@gmail.com".toUri()
                            putExtra(
                                Intent.EXTRA_SUBJECT,
                                getString(R.string.translations_email_subject)
                            )
                        }
                        openOrToast(emailIntent, SupportAndroidR.string.toast_no_email_app)
                    },
                    onOpenSourceLicenses = {
                        navigator.navigate(Route.OpenSourceLicenses)
                    },
                )
            }
        }
    }

    private fun openOrToast(intent: Intent, @StringRes noAppMessage: Int) {
        if (!tryStartActivity(intent)) UiUtils.toastIt(noAppMessage, this)
    }

    companion object {

        fun open(activity: Activity) {
            val intent = Intent(activity, AboutActivity::class.java)
            activity.startActivity(intent)
        }
    }
}
