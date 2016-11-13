package gc.david.dfm;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by david on 05.11.16.
 */
public class DFMPreferences {

    private static final String MEASURE_UNIT_KEY            = "unit";
    public static final  String MEASURE_EUROPEAN_UNIT_VALUE = "EU";
    public static final  String MEASURE_AMERICAN_UNIT_VALUE = "US";
    private static final String ELEVATION_CHART_KEY         = "elevation_chart";
    private static final String ANIMATION_KEY               = "animation";
    public static final  String ANIMATION_CENTRE_VALUE      = "CEN";
    public static final  String ANIMATION_DESTINATION_VALUE = "DES";
    public static final  String NO_ANIMATION_DESTINATION_VALUE = "NO";


    public static String getMeasureUnitPreference(final Context context) {
        return getPreferences(context).getString(MEASURE_UNIT_KEY, null);
    }

    public static void setMeasureUnitPreference(final Context context, final String unit) {
        getPreferences(context).edit().putString(MEASURE_UNIT_KEY, unit).apply();
    }

    public static boolean shouldShowElevationChart(final Context context) {
        return getPreferences(context).getBoolean(ELEVATION_CHART_KEY, true);
    }

    public static String getAnimationPreference(final Context context) {
        return getPreferences(context).getString(ANIMATION_KEY, ANIMATION_CENTRE_VALUE);
    }

    private static SharedPreferences getPreferences(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
