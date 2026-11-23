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

package gc.david.dfm.settings.presentation

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import gc.david.dfm.common.collectOnStarted
import gc.david.dfm.settings.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : PreferenceFragmentCompat() {

    val viewModel: SettingsViewModel by viewModel()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)

        val clearDatabasePreference : Preference? = findPreference(CLEAR_DATABASE_KEY)
        clearDatabasePreference?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            viewModel.onClearData()
            false
        }

        collectOnStarted {
            viewModel.uiState.collect { state ->
                state.successMessage?.let { message ->
                    Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
                    viewModel.onMessageShown()
                }
            }
        }
    }

    private companion object {

        const val CLEAR_DATABASE_KEY = "bbdd"
    }
}
