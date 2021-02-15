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
import gc.david.dfm.DFMPreferences
import gc.david.dfm.R
import gc.david.dfm.Utils
import gc.david.dfm.distance.domain.ClearDistancesInteractor
import gc.david.dfm.settings.presentation.Settings
import gc.david.dfm.settings.presentation.SettingsPresenter
import org.koin.android.ext.android.inject
import timber.log.Timber

class SettingsFragment : PreferenceFragmentCompat(), Settings.View {

    val clearDistancesUseCase: ClearDistancesInteractor by inject()

    private lateinit var presenter: Settings.Presenter

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)

        presenter = SettingsPresenter(this, clearDistancesUseCase)

        val bbddPreference = findPreference(DFMPreferences.CLEAR_DATABASE_KEY)
        bbddPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            Timber.tag(TAG).d("onPreferenceClick delete entries")

            presenter.onClearData()
            false
        }
    }

    override fun setPresenter(presenter: Settings.Presenter) {
        this.presenter = presenter
    }

    override fun showClearDataSuccessMessage() {
        Utils.toastIt(R.string.toast_distances_deleted, requireContext())
    }

    override fun showClearDataErrorMessage() {
        Timber.tag(TAG).e(Exception("Unable to clear database."))
    }

    companion object {

        private const val TAG = "SettingsFragment"
    }
}
