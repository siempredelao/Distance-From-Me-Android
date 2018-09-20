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

package gc.david.dfm.ui.fragment;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import javax.inject.Inject;

import gc.david.dfm.DFMApplication;
import gc.david.dfm.DFMPreferences;
import gc.david.dfm.R;
import gc.david.dfm.dagger.DaggerSettingsComponent;
import gc.david.dfm.dagger.RootModule;
import gc.david.dfm.dagger.SettingsModule;
import gc.david.dfm.dagger.StorageModule;
import gc.david.dfm.distance.domain.ClearDistancesUseCase;
import gc.david.dfm.logger.DFMLogger;
import gc.david.dfm.settings.presentation.Settings;
import gc.david.dfm.settings.presentation.SettingsPresenter;

import static gc.david.dfm.Utils.toastIt;

public class SettingsFragment extends PreferenceFragmentCompat implements Settings.View {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Inject
    protected ClearDistancesUseCase clearDistancesUseCase;

    private Settings.Presenter presenter;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);

        DaggerSettingsComponent.builder()
                               .rootModule(new RootModule((DFMApplication) requireActivity().getApplication()))
                               .storageModule(new StorageModule())
                               .settingsModule(new SettingsModule())
                               .build()
                               .inject(this);

        presenter = new SettingsPresenter(this, clearDistancesUseCase);

        final Preference bbddPreference = findPreference(DFMPreferences.CLEAR_DATABASE_KEY);
        bbddPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DFMLogger.logMessage(TAG, "onPreferenceClick delete entries");

                presenter.onClearData();
                return false;
            }
        });
    }

    @Override
    public void setPresenter(final Settings.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showClearDataSuccessMessage() {
        toastIt(R.string.toast_distances_deleted, requireContext());
    }

    @Override
    public void showClearDataErrorMessage() {
        DFMLogger.logException(new Exception("Unable to clear database."));
    }
}
