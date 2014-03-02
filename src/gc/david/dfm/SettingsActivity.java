package gc.david.dfm;

import gc.david.dfm.db.DistancesDataSource;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;


public class SettingsActivity extends PreferenceActivity{
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.settings);
        
        Preference bbdd = (Preference) findPreference("bbdd");
        bbdd.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				DistancesDataSource dDS = new DistancesDataSource(getApplicationContext());
				dDS.open();
				if (dDS != null){
					dDS.deleteAll();
					dDS.close();
					toastIt(getText(R.string.distances_deleted).toString());
				}
				return false;
			}
		});        
    }
	
	
	/**
	 * Makes toasting easy!
	 * 
	 * @param text
	 *            The string to show.
	 */
	private void toastIt(String text) {
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
	}
}
