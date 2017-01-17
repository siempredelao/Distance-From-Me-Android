/*
 * Copyright (c) 2017 David Aguiar Gonzalez
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

package gc.david.dfm.ui;

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

import static gc.david.dfm.Utils.toastIt;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Inject
    protected ClearDistancesUseCase clearDistancesUseCase;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);

        DaggerSettingsComponent.builder()
                               .rootModule(new RootModule((DFMApplication) getActivity().getApplication()))
                               .storageModule(new StorageModule())
                               .settingsModule(new SettingsModule())
                               .build()
                               .inject(this);

        final Preference bbddPreference = findPreference(DFMPreferences.CLEAR_DATABASE_KEY);
        bbddPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DFMLogger.logMessage(TAG, "onPreferenceClick delete entries");

                // TODO: 16.01.17 move this to presenter
                clearDistancesUseCase.execute(new ClearDistancesUseCase.Callback() {
                    @Override
                    public void onClear() {
                        toastIt(R.string.toast_distances_deleted, getContext());
                    }

                    @Override
                    public void onError() {
                        DFMLogger.logException(new Exception("Unable to clear database."));
                    }
                });
                return false;
            }
        });
    }
}
