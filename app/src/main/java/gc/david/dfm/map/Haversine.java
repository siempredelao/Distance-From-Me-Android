package gc.david.dfm.map;

import java.text.DecimalFormat;
import java.util.Locale;

import gc.david.dfm.logger.DFMLogger;

/**
 * Haversine class implements static methods to calculate distances between two
 * points on a sphere, in this case, the Earth, from their longitudes and
 * latitudes. It also defines static methods to calculate elevation points.
 *
 * @author David
 * @see <a href="http://en.wikipedia.org/wiki/Haversine_formula">Haversine</a>
 */
public final class Haversine {

    private static final String TAG = Haversine.class.getSimpleName();

    private static final double EARTH_RADIUS_IN_METRES = 6371000;
    private static final double MILE_IN_METRES         = 1609.344;
    private static final double KILOMETRE_IN_METRES    = 1000.0;
    private static final double YARD_IN_METRES         = 1.093613298337708;
    private static final double FEAT_IN_METRES         = 0.3048;

    /**
     * Calculates distance between two positions in metres.
     *
     * @param latitudeA  Current position latitude in degrees.
     * @param longitudeA Current position longitude in degrees.
     * @param latitudeB  Destination position latitude in degrees.
     * @param longitudeB Destination position longitude in degrees.
     * @return Distance in meters.
     */
    public static double getDistance(final double latitudeA,
                                     final double longitudeA,
                                     final double latitudeB,
                                     final double longitudeB) {
        DFMLogger.logMessage(TAG, "getDistance (" + latitudeA + "," + longitudeA
                                  + ")-(" + latitudeB + "," + longitudeB + ")");

        final double latitudeAInRadians = Math.toRadians(latitudeA);
        final double longitudeAInRadians = Math.toRadians(longitudeA);
        final double latitudeBInRadians = Math.toRadians(latitudeB);
        final double longitudeBInRadians = Math.toRadians(longitudeB);

        final double distanceLatitudes = latitudeBInRadians - latitudeAInRadians;
        final double distanceLongitudes = longitudeBInRadians - longitudeAInRadians;

        final double sinLatitude = Math.sin(distanceLatitudes / 2);
        final double sinLongitude = Math.sin(distanceLongitudes / 2);

        final double a = (sinLatitude * sinLatitude) +
                         Math.cos(latitudeAInRadians) * Math.cos(latitudeBInRadians) * (sinLongitude * sinLongitude);
        final double c = 2 * Math.asin(Math.min(1.0, Math.sqrt(a)));

        return EARTH_RADIUS_IN_METRES * c;
    }

    /**
     * Normalizes distance corresponding to its unit and the device locale and with only two decimal
     * digits. This static function distinguish between metric and imperial and US customary units.
     *
     * @param distanceInMetres Unformatted distance in metres.
     * @param locale           The current locale of the device.
     * @return A String with the amount and the unit.
     */
    public static String normalizeDistance(final double distanceInMetres, final Locale locale) {
        DFMLogger.logMessage(TAG, "normalizeDistance " +
                                  distanceInMetres +
                                  " with locale " +
                                  locale.toString());

        final String normalizedDistance;
        final String measureUnit;
        final double distanceByLocale;
        // Uso del formatter para dar con mayor precisión que Math.round
        // aunque gaste más, pero es una única medida la que se normalizará
        final DecimalFormat decimalFormat = new DecimalFormat("##,##0.00");

        if (locale.equals(Locale.CANADA)
            || locale.equals(Locale.CHINA)
            || locale.equals(Locale.JAPAN)
            || locale.equals(Locale.KOREA)
            || locale.equals(Locale.TAIWAN)
            || locale.equals(Locale.UK)
            || locale.equals(Locale.US)) {

            if (distanceInMetres >= MILE_IN_METRES) { // Hay al menos una milla
                measureUnit = "mi";
                distanceByLocale = distanceInMetres / MILE_IN_METRES;
            } else { // No llega a una milla, medimos en yardas
                measureUnit = "yd";
                distanceByLocale = distanceInMetres * YARD_IN_METRES;
            }
        } else {
            if (distanceInMetres >= KILOMETRE_IN_METRES) { // Hay al menos un kilómetro
                measureUnit = "km";
                distanceByLocale = distanceInMetres / KILOMETRE_IN_METRES;
            } else { // No llega a un kilómetro, medimos en metros
                measureUnit = "m";
                distanceByLocale = distanceInMetres;
            }
        }
        normalizedDistance = decimalFormat.format(distanceByLocale);
        return normalizedDistance + " " + measureUnit;
    }

    /**
     * Normalizes altitude corresponding to its unit and the device locale and with only two decimal
     * digits. This static function distinguish between metric and imperial and US customary units.
     *
     * @param altitude Unformatted altitude in metres.
     * @param locale   The current locale of the device.
     * @return A double with only the normalized amount.
     */
    public static double normalizeAltitudeByLocale(final double altitude, final Locale locale) {
        DFMLogger.logMessage(TAG, "normalizeAltitudeByLocale " +
                                  altitude +
                                  " with locale " +
                                  locale.toString());

        final double measure;

        if (locale.equals(Locale.CANADA)
            || locale.equals(Locale.UK)
            || locale.equals(Locale.US)) {
            measure = altitude / FEAT_IN_METRES;
        } else {
            measure = altitude;
        }
        // Two decimal digits
        return Math.round(measure * 1e2) / 1e2;
    }

    /**
     * Returns a string with the altitude unit, m or ft, based in the current locale.
     *
     * @param locale Current locale.
     * @return String with altitude unit.
     */
    public static String getAltitudeUnitByLocale(final Locale locale) {
        DFMLogger.logMessage(TAG, "getAltitudeUnitByLocale " + locale.toString());

        if (locale.equals(Locale.CANADA)
            || locale.equals(Locale.UK)
            || locale.equals(Locale.US)) {
            return "ft";
        } else {
            return "m";
        }
    }
}
