package gc.david.dfm;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

import gc.david.dfm.db.DistancesDataSource;

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
				final DistancesDataSource distancesDataSource = new DistancesDataSource(getApplicationContext());
				distancesDataSource.open();
				if (distancesDataSource != null) {
					distancesDataSource.deleteAll();
					distancesDataSource.close();
					Utils.toastIt(getText(R.string.distances_deleted), getApplicationContext());
				}
				return false;
			}
		});
    }
}
