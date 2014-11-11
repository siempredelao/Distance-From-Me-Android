package gc.david.dfm.map;

public final class LocationUtils {
    // Debugging tag for the application
    public static final String APPTAG = "LocationSample";

    /**
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*
     * Constants for location update parameters
     */
    // Milliseconds per second
    public static final int  MILLISECONDS_PER_SECOND               = 1000;
    // The update interval
    public static final int  UPDATE_INTERVAL_IN_SECONDS            = 5;
    // Update interval in milliseconds
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS       = MILLISECONDS_PER_SECOND *
                                                                     UPDATE_INTERVAL_IN_SECONDS;
    // A fast interval ceiling
    public static final int  FAST_CEILING_IN_SECONDS               = 1;
    // A fast ceiling of update intervals, used when the app is visible
    public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;
}
