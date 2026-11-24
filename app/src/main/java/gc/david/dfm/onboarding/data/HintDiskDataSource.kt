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

package gc.david.dfm.onboarding.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class HintDiskDataSource(private val context: Context) {

    fun getSeenHints(): Set<String> =
        getPreferences().getStringSet(SEEN_HINTS_KEY, emptySet()).orEmpty()

    fun setSeenHints(hints: Set<String>) {
        getPreferences().edit { putStringSet(SEEN_HINTS_KEY, hints) }
    }

    private fun getPreferences(): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    private companion object {

        const val SEEN_HINTS_KEY = "seen_map_hints"
    }
}
