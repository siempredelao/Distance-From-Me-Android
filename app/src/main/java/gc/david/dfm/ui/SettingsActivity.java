package gc.david.dfm.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

import java.util.Locale;

import gc.david.dfm.DFMApplication;
import gc.david.dfm.R;
import gc.david.dfm.logger.DFMLogger;
import gc.david.dfm.model.DaoSession;
import gc.david.dfm.model.Distance;
import gc.david.dfm.model.Position;

import static gc.david.dfm.Utils.toastIt;

public class SettingsActivity extends PreferenceActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    @SuppressWarnings("deprecation")
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

        // Set default unit if not already set
        final SharedPreferences preferences = getSharedPreferences("gc.david.dfm_preferences", Context.MODE_PRIVATE);
        final String defaultUnit = preferences.getString("unit", null);
        if (defaultUnit == null) {
            final SharedPreferences.Editor editor = preferences.edit();

            final Locale locale = getResources().getConfiguration().locale;
            if (locale.equals(Locale.CANADA)
                || locale.equals(Locale.CHINA)
                || locale.equals(Locale.JAPAN)
                || locale.equals(Locale.KOREA)
                || locale.equals(Locale.TAIWAN)
                || locale.equals(Locale.UK)
                || locale.equals(Locale.US)) {
                editor.putString("unit", getString(R.string.preference_unit_entry_value_US));
            } else {
                editor.putString("unit", getString(R.string.preference_unit_entry_value_EU));
            }
            editor.commit();
        }
    }

    private DaoSession getApplicationDaoSession() {
        DFMLogger.logMessage(TAG, "getApplicationDaoSession");

        return ((DFMApplication) getApplicationContext()).getDaoSession();
    }
}
