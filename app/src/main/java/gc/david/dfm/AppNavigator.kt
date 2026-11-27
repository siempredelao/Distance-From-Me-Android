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

package gc.david.dfm

import android.app.Activity
import gc.david.dfm.about.presentation.AboutActivity
import gc.david.dfm.faq.presentation.FaqActivity
import gc.david.dfm.opensource.presentation.OpenSourceLicensesActivity
import gc.david.dfm.settings.presentation.SettingsActivity
import gc.david.dfm.showinfo.presentation.ShowInfoActivity
import gc.david.dfm.support.navigation.Navigator
import gc.david.dfm.support.navigation.Route

class AppNavigator(private val activity: Activity) : Navigator {

    override fun navigate(route: Route) {
        when (route) {
            Route.About -> AboutActivity.open(activity)
            Route.Faq -> FaqActivity.open(activity)
            Route.OpenSourceLicenses -> OpenSourceLicensesActivity.open(activity)
            Route.Settings -> SettingsActivity.open(activity)
            Route.ShowInfo -> ShowInfoActivity.open(activity)
        }
    }
}
