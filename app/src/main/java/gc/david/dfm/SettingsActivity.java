package gc.david.dfm;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

import gc.david.dfm.model.DaoSession;
import gc.david.dfm.model.Distance;
import gc.david.dfm.model.Position;

import static gc.david.dfm.Utils.toastIt;

public class SettingsActivity extends PreferenceActivity {
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        final Preference bbddPreference = findPreference("bbdd");
        bbddPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO hacerlo en segundo plano
                getApplicationDaoSession().deleteAll(Distance.class);
                getApplicationDaoSession().deleteAll(Position.class);
                toastIt(getText(R.string.distances_deleted), getApplicationContext());
                return false;
            }
        });
    }

    private DaoSession getApplicationDaoSession() {
        return ((DFMApplication) getApplicationContext()).getDaoSession();
    }
}
