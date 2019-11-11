/*
 * Copyright (c) 2019 David Aguiar Gonzalez
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

package gc.david.dfm.deviceinfo

import android.content.Context
import android.content.Intent

/**
 * Created by david on 07.12.16.
 */
class DefaultPackageManager(private val context: Context) : PackageManager {

    private val packageManager: android.content.pm.PackageManager = context.packageManager

    override val versionName: String
        get() {
            return try {
                packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
                "Version name not found"
            }

        }

    override fun isThereAnyActivityForIntent(intent: Intent): Boolean {
        val resolveInfos = packageManager.queryIntentActivities(intent, 0)
        return resolveInfos.isNotEmpty()
    }
}
