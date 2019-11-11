/*
 * Copyright (c) 2018 David Aguiar Gonzalez
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
import gc.david.dfm.DFMApplication
import gc.david.dfm.DFMPreferences
import gc.david.dfm.R
import gc.david.dfm.Utils
import gc.david.dfm.dagger.DaggerSettingsComponent
import gc.david.dfm.dagger.RootModule
import gc.david.dfm.dagger.SettingsModule
import gc.david.dfm.dagger.StorageModule
import gc.david.dfm.distance.domain.ClearDistancesUseCase
import gc.david.dfm.logger.DFMLogger
import gc.david.dfm.settings.presentation.Settings
import gc.david.dfm.settings.presentation.SettingsPresenter
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat(), Settings.View {

    @Inject
    lateinit var clearDistancesUseCase: ClearDistancesUseCase

    private lateinit var presenter: Settings.Presenter

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)

        DaggerSettingsComponent.builder()
                .rootModule(RootModule(requireActivity().application as DFMApplication))
                .storageModule(StorageModule())
                .settingsModule(SettingsModule())
                .build()
                .inject(this)

        presenter = SettingsPresenter(this, clearDistancesUseCase)

        val bbddPreference = findPreference(DFMPreferences.CLEAR_DATABASE_KEY)
        bbddPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            DFMLogger.logMessage(TAG, "onPreferenceClick delete entries")

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
        DFMLogger.logException(Exception("Unable to clear database."))
    }

    companion object {

        private val TAG = SettingsFragment::class.java.simpleName
    }
}
