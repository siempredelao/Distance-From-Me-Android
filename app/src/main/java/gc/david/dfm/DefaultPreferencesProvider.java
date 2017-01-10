package gc.david.dfm;

import android.content.Context;

/**
 * Created by david on 10.01.17.
 */
public class DefaultPreferencesProvider implements PreferencesProvider {

    private final Context context;

    public DefaultPreferencesProvider(final Context context) {
        this.context = context;
    }

    @Override
    public boolean shouldShowElevationChart() {
        return DFMPreferences.shouldShowElevationChart(context);
    }
}
