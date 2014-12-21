package gc.david.dfm.ui;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

import com.splunk.mint.Mint;

import gc.david.dfm.DFMApplication;
import gc.david.dfm.R;
import gc.david.dfm.model.DaoSession;
import gc.david.dfm.model.Distance;
import gc.david.dfm.model.Position;

import static gc.david.dfm.Utils.toastIt;

public class SettingsActivity extends PreferenceActivity {
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Mint.leaveBreadcrumb("SettingsActivity::onCreate");
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        final Preference bbddPreference = findPreference("bbdd");
        bbddPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Mint.leaveBreadcrumb("SettingsActivity::onPreferenceClick delete entries");
                // TODO hacerlo en segundo plano
                getApplicationDaoSession().deleteAll(Distance.class);
                getApplicationDaoSession().deleteAll(Position.class);
                toastIt(getString(R.string.toast_distances_deleted), getApplicationContext());
                return false;
            }
        });
    }

    private DaoSession getApplicationDaoSession() {
        Mint.leaveBreadcrumb("SettingsActivity::getApplicationDaoSession");
        return ((DFMApplication) getApplicationContext()).getDaoSession();
    }
}
