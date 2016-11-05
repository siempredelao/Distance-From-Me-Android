package gc.david.dfm.ui;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

import gc.david.dfm.DFMApplication;
import gc.david.dfm.R;
import gc.david.dfm.logger.DFMLogger;
import gc.david.dfm.model.DaoSession;
import gc.david.dfm.model.Distance;
import gc.david.dfm.model.Position;

import static gc.david.dfm.Utils.toastIt;

public class SettingsActivity extends PreferenceActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        DFMLogger.logMessage(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        final Preference bbddPreference = findPreference("bbdd");
        bbddPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DFMLogger.logMessage(TAG, "onPreferenceClick delete entries");

                // TODO hacerlo en segundo plano
                getApplicationDaoSession().deleteAll(Distance.class);
                getApplicationDaoSession().deleteAll(Position.class);
                toastIt(getString(R.string.toast_distances_deleted), getApplicationContext());
                return false;
            }
        });
    }

    private DaoSession getApplicationDaoSession() {
        DFMLogger.logMessage(TAG, "getApplicationDaoSession");

        return ((DFMApplication) getApplicationContext()).getDaoSession();
    }
}
