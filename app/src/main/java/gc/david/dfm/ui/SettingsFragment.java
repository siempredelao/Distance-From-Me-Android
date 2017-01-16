package gc.david.dfm.ui;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import javax.inject.Inject;

import gc.david.dfm.DFMApplication;
import gc.david.dfm.DFMPreferences;
import gc.david.dfm.R;
import gc.david.dfm.dagger.DaggerRootComponent;
import gc.david.dfm.dagger.RootModule;
import gc.david.dfm.logger.DFMLogger;
import gc.david.dfm.model.DaoSession;
import gc.david.dfm.model.Distance;
import gc.david.dfm.model.Position;

import static gc.david.dfm.Utils.toastIt;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Inject
    protected DaoSession daoSession;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);

        DaggerRootComponent.builder()
                           .rootModule(new RootModule((DFMApplication) getActivity().getApplication()))
                           .build()
                           .inject(this);

        final Preference bbddPreference = findPreference(DFMPreferences.CLEAR_DATABASE_KEY);
        bbddPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DFMLogger.logMessage(TAG, "onPreferenceClick delete entries");

                // TODO hacerlo en segundo plano
                daoSession.deleteAll(Distance.class);
                daoSession.deleteAll(Position.class);
                toastIt(getString(R.string.toast_distances_deleted), getActivity().getApplicationContext());
                return false;
            }
        });
    }
}
