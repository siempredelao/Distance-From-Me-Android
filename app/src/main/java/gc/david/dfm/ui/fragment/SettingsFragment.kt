/*
 * Copyright (c) 2021 David Aguiar Gonzalez
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

package gc.david.dfm.ui.fragment

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import gc.david.dfm.DFMPreferences
import gc.david.dfm.R
import gc.david.dfm.settings.presentation.SettingsViewModel
import org.koin.android.ext.android.inject

class SettingsFragment : PreferenceFragmentCompat() {

    val viewModel: SettingsViewModel by inject()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)

        val bbddPreference = findPreference(DFMPreferences.CLEAR_DATABASE_KEY)
        bbddPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            viewModel.onClearData()
            false
        }

        with(viewModel) {
            resultMessage.observe(this@SettingsFragment, {
                Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
            })
        }
    }
}
