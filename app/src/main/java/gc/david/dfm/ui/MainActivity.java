package gc.david.dfm.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.collect.Lists;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiBanner;
import com.inmobi.ads.InMobiBanner.BannerAdListener;
import com.inmobi.sdk.InMobiSdk;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.BindView;
import gc.david.dfm.BuildConfig;
import gc.david.dfm.DFMPreferences;
import gc.david.dfm.R;
import gc.david.dfm.Utils;
import gc.david.dfm.adapter.MarkerInfoWindowAdapter;
import gc.david.dfm.dialog.AddressSugestionsDialogFragment;
import gc.david.dfm.dialog.DistanceSelectionDialogFragment;
import gc.david.dfm.logger.DFMLogger;
import gc.david.dfm.map.Haversine;
import gc.david.dfm.map.LocationUtils;
import gc.david.dfm.model.DaoSession;
import gc.david.dfm.model.Distance;
import gc.david.dfm.model.Position;
import gc.david.dfm.service.GeofencingService;

import static butterknife.ButterKnife.bind;
import static gc.david.dfm.Utils.isOnline;
import static gc.david.dfm.Utils.showAlertDialog;
import static gc.david.dfm.Utils.toastIt;

/**
 * Implements the app main Activity.
 *
 * @author David
 */
public class MainActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener,
                                                          OnMapReadyCallback,
                                                          OnMapLongClickListener,
                                                          OnMapClickListener,
                                                          OnInfoWindowClickListener {

    private static final String TAG                     = MainActivity.class.getSimpleName();
    private static final int    ELEVATION_SAMPLES       = 100;

    @BindView(R.id.elevationchart)
    protected RelativeLayout rlElevationChart;
    @BindView(R.id.closeChart)
    protected ImageView      ivCloseElevationChart;
    @BindView(R.id.tbMain)
    protected Toolbar        tbMain;
    @BindView(R.id.banner)
    protected InMobiBanner   banner;
    @BindView(R.id.drawer_layout)
    protected DrawerLayout   drawerLayout;
    @BindView(R.id.nvDrawer)
    protected NavigationView nvDrawer;

    @Inject
    protected DaoSession daoSession;
    @Inject
    protected Context    appContext;

    private final BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final double latitude = intent.getDoubleExtra(GeofencingService.GEOFENCE_RECEIVER_LATITUDE_KEY, 0D);
            final double longitude = intent.getDoubleExtra(GeofencingService.GEOFENCE_RECEIVER_LONGITUDE_KEY, 0D);
            final Location location = new Location("");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            onLocationChanged(location);
        }
    };

    private GoogleMap       googleMap                             = null;
    private Location        currentLocation                       = null;
    // Moves to current position if app has just started
    private boolean         appHasJustStarted                     = true;
    private String          distanceMeasuredAsText                = "";
    private MenuItem        searchMenuItem                        = null;
    // Show position if we come from other app (p.e. Whatsapp)
    private boolean         mustShowPositionWhenComingFromOutside = false;
    private LatLng          sendDestinationPosition               = null;
    private boolean         bannerShown                           = false;
    private boolean         elevationChartShown                   = false;
    @SuppressWarnings("rawtypes")
    private AsyncTask       showingElevationTask                  = null;
    private GraphView       graphView                             = null;
    private float                 DEVICE_DENSITY;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private List<LatLng>          coordinates;
    private boolean               calculatingDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DFMLogger.logMessage(TAG, "onCreate savedInstanceState=" + Utils.dumpBundleToString(savedInstanceState));

        super.onCreate(savedInstanceState);
        InMobiSdk.setLogLevel(BuildConfig.DEBUG ? InMobiSdk.LogLevel.DEBUG : InMobiSdk.LogLevel.NONE);
        InMobiSdk.init(this, getString(R.string.inmobi_api_key));
        setContentView(R.layout.activity_main);
        bind(this);

        setSupportActionBar(tbMain);

        final ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeButtonEnabled(true);
        }

        DEVICE_DENSITY = getResources().getDisplayMetrics().density;

        final SupportMapFragment supportMapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        supportMapFragment.getMapAsync(this);

        banner.setListener(new BannerAdListener() {
            @Override
            public void onAdLoadSucceeded(InMobiBanner inMobiBanner) {
                DFMLogger.logMessage(TAG, "onAdLoadSucceeded");

                bannerShown = true;
                fixMapPadding();
            }

            @Override
            public void onAdLoadFailed(InMobiBanner inMobiBanner, InMobiAdRequestStatus inMobiAdRequestStatus) {
                DFMLogger.logMessage(TAG,
                                     String.format("onAdLoadFailed %s %s",
                                                   inMobiAdRequestStatus.getStatusCode(),
                                                   inMobiAdRequestStatus.getMessage()));
            }

            @Override
            public void onAdDisplayed(InMobiBanner inMobiBanner) {
                DFMLogger.logMessage(TAG, "onAdDisplayed");
            }

            @Override
            public void onAdDismissed(InMobiBanner inMobiBanner) {
                DFMLogger.logMessage(TAG, "onAdDismissed");
            }

            @Override
            public void onAdInteraction(InMobiBanner inMobiBanner, Map<Object, Object> map) {
                DFMLogger.logMessage(TAG, String.format("onAdInteraction %s", map.toString()));

                DFMLogger.logEvent("Ad tapped");
            }

            @Override
            public void onUserLeftApplication(InMobiBanner inMobiBanner) {
                DFMLogger.logMessage(TAG, "onUserLeftApplication");
            }

            @Override
            public void onAdRewardActionCompleted(InMobiBanner inMobiBanner, Map<Object, Object> map) {
                DFMLogger.logMessage(TAG, String.format("onAdRewardActionCompleted %s", map.toString()));
            }
        });
        if (!BuildConfig.DEBUG) {
            banner.load();
        }

        if (!isOnline(appContext)) {
            showWifiAlertDialog();
        }

        // Iniciando la app
        if (currentLocation == null) {
            toastIt(getString(R.string.toast_loading_position), appContext);
        }

        handleIntents(getIntent());

        nvDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_current_position:
                        menuItem.setChecked(true);
                        drawerLayout.closeDrawers();
                        onStartingPointSelected();
                        return true;
                    case R.id.menu_any_position:
                        menuItem.setChecked(true);
                        drawerLayout.closeDrawers();
                        onStartingPointSelected();
                        return true;
                    case R.id.menu_rate_app:
                        drawerLayout.closeDrawers();
                        showRateDialog();
                        return true;
                    case R.id.menu_legal_notices:
                        drawerLayout.closeDrawers();
                        showGooglePlayServiceLicenseDialog();
                        return true;
                    case R.id.menu_settings:
                        drawerLayout.closeDrawers();
                        openSettingsActivity();
                        return true;
                }
                return false;
            }
        });

        // TODO: 23.08.15 check if this is still needed
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                                                          drawerLayout,
                                                          R.string.progressdialog_search_position_message,
                                                          R.string.progressdialog_search_position_message) {
            @Override
            public void onDrawerOpened(View drawerView) {
                DFMLogger.logMessage(TAG, "onDrawerOpened");

                super.onDrawerOpened(drawerView);
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                DFMLogger.logMessage(TAG, "onDrawerClosed");

                super.onDrawerClosed(drawerView);
                supportInvalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        googleMap = map;

        googleMap.setMyLocationEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        googleMap.setOnMapLongClickListener(this);
        googleMap.setOnMapClickListener(this);
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter(this));

        onStartingPointSelected();
    }

    @Override
    public void onMapLongClick(LatLng point) {
        DFMLogger.logMessage(TAG, "onMapLongClick");

        calculatingDistance = true;

        if (getSelectedDistanceMode() == DistanceMode.DISTANCE_FROM_ANY_POINT) {
            if (coordinates == null || coordinates.isEmpty()) {
                toastIt(getString(R.string.toast_first_point_needed), appContext);
            } else {
                coordinates.add(point);
                drawAndShowMultipleDistances(coordinates, "", false, true);
            }
        }
        // Si no hemos encontrado la posición actual, no podremos
        // calcular la distancia
        else if (currentLocation != null) {
            if ((getSelectedDistanceMode() == DistanceMode.DISTANCE_FROM_CURRENT_POINT) && (coordinates.isEmpty())) {
                coordinates.add(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
            }
            coordinates.add(point);
            drawAndShowMultipleDistances(coordinates, "", false, true);
        }

        calculatingDistance = false;
    }

    @Override
    public void onMapClick(LatLng point) {
        DFMLogger.logMessage(TAG, "onMapClick");

        if (getSelectedDistanceMode() == DistanceMode.DISTANCE_FROM_ANY_POINT) {
            if (!calculatingDistance) {
                coordinates.clear();
            }

            calculatingDistance = true;

            if (coordinates.isEmpty()) {
                googleMap.clear();
            }
            coordinates.add(point);
            googleMap.addMarker(new MarkerOptions().position(point));
        } else {
            // Si no hemos encontrado la posición actual, no podremos
            // calcular la distancia
            if (currentLocation != null) {
                if (coordinates != null) {
                    if (!calculatingDistance) {
                        coordinates.clear();
                    }
                    calculatingDistance = true;

                    if (coordinates.isEmpty()) {
                        googleMap.clear();
                        coordinates.add(new LatLng(currentLocation.getLatitude(),
                                                   currentLocation.getLongitude()));
                    }
                    coordinates.add(point);
                    googleMap.addMarker(new MarkerOptions().position(point));
                } else {
                    final IllegalStateException illegalStateException = new IllegalStateException("Empty coordinates list");
                    DFMLogger.logException(illegalStateException);

                    throw illegalStateException;
                }
            }
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        DFMLogger.logMessage(TAG, "onInfoWindowClick");

        final Intent showInfoActivityIntent = new Intent(MainActivity.this, ShowInfoActivity.class);

        showInfoActivityIntent.putExtra(ShowInfoActivity.POSITIONS_LIST_EXTRA_KEY_NAME,
                                        Lists.newArrayList(coordinates));
        showInfoActivityIntent.putExtra(ShowInfoActivity.DISTANCE_EXTRA_KEY_NAME, distanceMeasuredAsText);
        startActivity(showInfoActivityIntent);
    }

    /**
     * Swaps starting point in the main content view
     */
    private void onStartingPointSelected() {
        if (getSelectedDistanceMode() == DistanceMode.DISTANCE_FROM_CURRENT_POINT) {
            DFMLogger.logMessage(TAG, "onStartingPointSelected Distance from current point");
        } else{
            DFMLogger.logMessage(TAG, "onStartingPointSelected Distance from any point");
        }

        calculatingDistance = false;

        coordinates = Lists.newArrayList();
        googleMap.clear();
        if (showingElevationTask != null) {
            DFMLogger.logMessage(TAG, "onStartingPointSelected cancelling elevation task");
            showingElevationTask.cancel(true);
        }
        rlElevationChart.setVisibility(View.INVISIBLE);
        elevationChartShown = false;
        fixMapPadding();
    }

    private DistanceMode getSelectedDistanceMode() {
        return nvDrawer.getMenu().findItem(R.id.menu_current_position).isChecked()
               ? DistanceMode.DISTANCE_FROM_CURRENT_POINT
               : DistanceMode.DISTANCE_FROM_ANY_POINT;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        DFMLogger.logMessage(TAG, "onPostCreate savedInstanceState=" + Utils.dumpBundleToString(savedInstanceState));

        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (actionBarDrawerToggle != null) {
            actionBarDrawerToggle.syncState();
        } else {
            DFMLogger.logMessage(TAG, "onPostCreate actionBarDrawerToggle null");
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        DFMLogger.logMessage(TAG, "onConfigurationChanged");

        super.onConfigurationChanged(newConfig);
        if (actionBarDrawerToggle != null) {
            actionBarDrawerToggle.onConfigurationChanged(newConfig);
        } else {
            DFMLogger.logMessage(TAG, "onConfigurationChanged actionBarDrawerToggle null");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        DFMLogger.logMessage(TAG, "onNewIntent " + Utils.dumpIntentToString(intent));

        setIntent(intent);
        handleIntents(intent);
    }

    /**
     * Handles all Intent types.
     *
     * @param intent The input intent.
     */
    private void handleIntents(final Intent intent) {
        DFMLogger.logMessage(TAG, "handleIntents " + Utils.dumpIntentToString(intent));

        if (intent != null) {
            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                handleSearchIntent(intent);
            } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                try {
                    handleViewPositionIntent(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    DFMLogger.logException(e);
                }
            }
        }
    }

    /**
     * Handles a search intent.
     *
     * @param intent Input intent.
     */
    private void handleSearchIntent(final Intent intent) {
        DFMLogger.logMessage(TAG, "handleSearchIntent");

        // Para controlar instancias únicas, no queremos que cada vez que
        // busquemos nos inicie una nueva instancia de la aplicación
        final String query = intent.getStringExtra(SearchManager.QUERY);
        if (currentLocation != null) {
            new SearchPositionByName().execute(query);
        }
        if (searchMenuItem != null) {
            MenuItemCompat.collapseActionView(searchMenuItem);
        }
    }

    /**
     * Handles a send intent with position data.
     *
     * @param intent Input intent with position data.
     */
    private void handleViewPositionIntent(final Intent intent) throws Exception {
        DFMLogger.logMessage(TAG, "handleViewPositionIntent");
        final Uri uri = intent.getData();
        DFMLogger.logMessage(TAG, "handleViewPositionIntent uri=" + uri.toString());

        final String uriScheme = uri.getScheme();
        if (uriScheme.equals("geo")) {
            final String schemeSpecificPart = uri.getSchemeSpecificPart();
            final Matcher matcher = getMatcherForUri(schemeSpecificPart);
            if (matcher.find()) {
                if (matcher.group(1).equals("0") && matcher.group(2).equals("0")) {
                    if (matcher.find()) { // Manage geo:0,0?q=lat,lng(label)
                        setDestinationPosition(matcher);
                    } else { // Manage geo:0,0?q=my+street+address
                        String destination = Uri.decode(uri.getQuery()).replace('+', ' ');
                        destination = destination.replace("q=", "");

                        // TODO check this ugly workaround
                        new SearchPositionByName().execute(destination);
                        mustShowPositionWhenComingFromOutside = true;
                    }
                } else { // Manage geo:latitude,longitude or geo:latitude,longitude?z=zoom
                    setDestinationPosition(matcher);
                }
            } else {
                final NoSuchFieldException noSuchFieldException = new NoSuchFieldException("Error al obtener las coordenadas. Matcher = " +
                                                                                           matcher.toString());
                DFMLogger.logException(noSuchFieldException);
                throw noSuchFieldException;
            }
        } else if ((uriScheme.equals("http") || uriScheme.equals("https"))
                   && (uri.getHost().equals("maps.google.com"))) { // Manage maps.google.com?q=latitude,longitude

            final String queryParameter = uri.getQueryParameter("q");
            if (queryParameter != null) {
                final Matcher matcher = getMatcherForUri(queryParameter);
                if (matcher.find()) {
                    setDestinationPosition(matcher);
                } else {
                    final NoSuchFieldException noSuchFieldException = new NoSuchFieldException("Error al obtener las coordenadas. Matcher = " +
                                                                                               matcher.toString());
                    DFMLogger.logException(noSuchFieldException);
                    throw noSuchFieldException;
                }
            } else {
                final NoSuchFieldException noSuchFieldException = new NoSuchFieldException("Query sin parámetro q.");
                DFMLogger.logException(noSuchFieldException);
                throw noSuchFieldException;
            }
        } else {
            final Exception exception = new Exception("Imposible tratar la query " + uri.toString());
            DFMLogger.logException(exception);
            throw exception;
        }
    }

    private void setDestinationPosition(final Matcher matcher) {
        DFMLogger.logMessage(TAG, "setDestinationPosition");

        sendDestinationPosition = new LatLng(Double.valueOf(matcher.group(1)), Double.valueOf(matcher.group(2)));
        mustShowPositionWhenComingFromOutside = true;
    }

    private Matcher getMatcherForUri(final String schemeSpecificPart) {
        DFMLogger.logMessage(TAG, "getMatcherForUri scheme=" + schemeSpecificPart);

        // http://regex101.com/
        // http://www.regexplanet.com/advanced/java/index.html
        final String regex = "(\\-?\\d+\\.*\\d*),(\\-?\\d+\\.*\\d*)";
        final Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(schemeSpecificPart);
    }

    /**
     * Shows the wireless centralized settings in API<11, otherwise shows general settings
     */
    private void showWifiAlertDialog() {
        DFMLogger.logMessage(TAG, "showWifiAlertDialog");

        showAlertDialog((android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB ?
                         android.provider.Settings.ACTION_WIRELESS_SETTINGS :
                         android.provider.Settings.ACTION_SETTINGS),
                        getString(R.string.dialog_connection_problems_title),
                        getString(R.string.dialog_connection_problems_message),
                        getString(R.string.dialog_connection_problems_positive_button),
                        getString(R.string.dialog_connection_problems_negative_button),
                        MainActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        DFMLogger.logMessage(TAG, "onCreateOptionsMenu");

        // Inflate the options menu from XML
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        // Expandir el EditText de la búsqueda a lo largo del ActionBar
        searchMenuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        // Configure the search info and add any event listeners
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        // Indicamos que la activity actual sea la buscadora
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(false);
        searchView.setQueryRefinementEnabled(true);
        searchView.setIconifiedByDefault(true);

        // Muestra el item de menú de cargar si hay elementos en la BD
        final MenuItem loadItem = menu.findItem(R.id.action_load);
        // TODO hacerlo en segundo plano
        final List<Distance> allDistances = daoSession.loadAll(Distance.class);
        if (allDistances.size() == 0) {
            loadItem.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DFMLogger.logMessage(TAG, "onOptionsItemSelected item=" + item.getItemId());

        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (actionBarDrawerToggle != null && actionBarDrawerToggle.onOptionsItemSelected(item)) {
            DFMLogger.logMessage(TAG, "onOptionsItemSelected ActionBar home button click");

            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case R.id.action_load:
                loadDistancesFromDB();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        DFMLogger.logMessage(TAG, "onBackPressed");

        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Loads all entries stored in the database and show them to the user in a
     * dialog.
     */
    private void loadDistancesFromDB() {
        DFMLogger.logMessage(TAG, "loadDistancesFromDB");

        // TODO hacer esto en segundo plano
        final List<Distance> allDistances = daoSession.loadAll(Distance.class);

        if (allDistances != null && allDistances.size() > 0) {
            final DistanceSelectionDialogFragment distanceSelectionDialogFragment = new DistanceSelectionDialogFragment();
            distanceSelectionDialogFragment.setDistanceList(allDistances);
            distanceSelectionDialogFragment.setOnDialogActionListener(new DistanceSelectionDialogFragment.OnDialogActionListener() {
                @Override
                public void onItemClick(int position) {
                    final Distance distance = allDistances.get(position);
                    final List<Position> positionList = daoSession.getPositionDao()
                                                                  ._queryDistance_PositionList(distance.getId());
                    coordinates.clear();
                    coordinates.addAll(Utils.convertPositionListToLatLngList(positionList));

                    drawAndShowMultipleDistances(coordinates, distance.getName() + "\n", true, true);
                }
            });
            distanceSelectionDialogFragment.show(getSupportFragmentManager(), null);
        }
    }

    /**
     * Shows settings activity.
     */
    private void openSettingsActivity() {
        DFMLogger.logMessage(TAG, "openSettingsActivity");

        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    /**
     * Shows rate dialog.
     */
    private void showRateDialog() {
        DFMLogger.logMessage(TAG, "showRateDialog");

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_rate_app_title)
               .setMessage(R.string.dialog_rate_app_message)
               .setPositiveButton(getString(R.string.dialog_rate_app_positive_button),
                                  new DialogInterface.OnClickListener() {
                                      @Override
                                      public void onClick(DialogInterface dialog, int which) {
                                          dialog.dismiss();
                                          openPlayStoreAppPage();
                                      }
                                  })
               .setNegativeButton(getString(R.string.dialog_rate_app_negative_button),
                                  new DialogInterface.OnClickListener() {
                                      @Override
                                      public void onClick(DialogInterface dialog, int which) {
                                          dialog.dismiss();
                                          openFeedbackActivity();
                                      }
                                  }).create().show();
    }

    /**
     * Opens Google Play Store, in Distance From Me page
     */
    private void openPlayStoreAppPage() {
        DFMLogger.logMessage(TAG, "openPlayStoreAppPage");

        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=gc.david.dfm")));
    }

    /**
     * Opens the feedback activity.
     */
    private void openFeedbackActivity() {
        DFMLogger.logMessage(TAG, "openFeedbackActivity");

        final Intent openFeedbackActivityIntent = new Intent(MainActivity.this, FeedbackActivity.class);
        startActivity(openFeedbackActivityIntent);
    }

    /**
     * Shows an AlertDialog with the Google Play Services License.
     */
    private void showGooglePlayServiceLicenseDialog() {
        DFMLogger.logMessage(TAG, "showGooglePlayServiceLicenseDialog");

        final String LicenseInfo = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(appContext);
        final AlertDialog.Builder LicenseDialog = new AlertDialog.Builder(MainActivity.this);
        LicenseDialog.setTitle(R.string.menu_legal_notices_title);
        LicenseDialog.setMessage(LicenseInfo);
        LicenseDialog.show();
    }

    /**
     * Called when the Activity is no longer visible at all. Stop updates and
     * disconnect.
     */
    @Override
    public void onStop() {
        DFMLogger.logMessage(TAG, "onStop");

        super.onStop();
        unregisterReceiver(locationReceiver);
        stopService(new Intent(this, GeofencingService.class));
    }

    /**
     * Called when the Activity is restarted, even before it becomes visible.
     */
    @Override
    public void onStart() {
        DFMLogger.logMessage(TAG, "onStart");

        super.onStart();
        registerReceiver(locationReceiver, new IntentFilter(GeofencingService.GEOFENCE_RECEIVER_ACTION));
        startService(new Intent(this, GeofencingService.class));
    }

    /**
     * Called when the system detects that this Activity is now visible.
     */
    @SuppressLint("NewApi")
    @Override
    public void onResume() {
        DFMLogger.logMessage(TAG, "onResume");

        super.onResume();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            invalidateOptionsMenu();
        }
        checkPlayServices();
    }

    @Override
    public void onDestroy() {
        DFMLogger.logMessage(TAG, "onDestroy");

        if (showingElevationTask != null) {
            DFMLogger.logMessage(TAG, "onDestroy cancelling showing elevation task before destroying app");
            showingElevationTask.cancel(true);
        }
        super.onDestroy();
    }

    /**
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed()
     * in LocationUpdateRemover and LocationUpdateRequester may call
     * startResolutionForResult() to start an Activity that handles Google Play
     * services problems. The result of this call returns here, to
     * onActivityResult.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        DFMLogger.logMessage(TAG, "onActivityResult requestCode=" + requestCode + ", " +
                                  "resultCode=" + resultCode + "intent=" + Utils.dumpIntentToString(intent));

        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST:

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        // Log the result
                        // Log.d(LocationUtils.APPTAG, getString(R.string.resolved));

                        // Display the result
                        // mConnectionState.setText(R.string.connected);
                        // mConnectionStatus.setText(R.string.resolved);
                        break;

                    // If any other result was returned by Google Play services
                    default:
                        // Log the result
                        // Log.d(LocationUtils.APPTAG,
                        // getString(R.string.no_resolution));

                        // Display the result
                        // mConnectionState.setText(R.string.disconnected);
                        // mConnectionStatus.setText(R.string.no_resolution);

                        break;
                }

                // If any other request code was received
            default:
                // Report that this Activity received an unknown requestCode
                // Log.d(LocationUtils.APPTAG,
                // getString(R.string.unknown_activity_request_code, requestCode));

                break;
        }
    }

    /**
     * Checks if Google Play Services is available on the device.
     *
     * @return Returns <code>true</code> if available; <code>false</code>
     * otherwise.
     */
    private boolean checkPlayServices() {
        DFMLogger.logMessage(TAG, "checkPlayServices");

        // Comprobamos que Google Play Services está disponible en el terminal
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(appContext);

        // Si está disponible, devolvemos verdadero. Si no, mostramos un mensaje
        // de error y devolvemos falso
        if (resultCode == ConnectionResult.SUCCESS) {
            DFMLogger.logMessage(TAG, "checkPlayServices success");

            return true;
        } else {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                DFMLogger.logMessage(TAG, "checkPlayServices isUserRecoverableError");

                final int RQS_GooglePlayServices = 1;
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, RQS_GooglePlayServices).show();
            } else {
                DFMLogger.logMessage(TAG, "checkPlayServices device not supported, finishing");

                finish();
            }
            return false;
        }
    }

    /**
     * Called by Location Services if the attempt to Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        DFMLogger.logMessage(TAG, "onConnectionFailed");

        /*
         * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
        if (connectionResult.hasResolution()) {
            DFMLogger.logMessage(TAG, "onConnectionFailed connection has resolution");

            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services cancelled the original
				 * PendingIntent
				 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
                DFMLogger.logException(e);
            }
        } else {
            DFMLogger.logMessage(TAG, "onConnectionFailed connection does not have resolution");
            // If no resolution is available, display a dialog to the user with
            // the error.
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    public void onLocationChanged(final Location location) {
        DFMLogger.logMessage(TAG, "onLocationChanged");

        if (currentLocation != null) {
            currentLocation.set(location);
        } else {
            currentLocation = new Location(location);
        }

        if (appHasJustStarted) {
            DFMLogger.logMessage(TAG, "onLocationChanged appHasJustStarted");

            if (mustShowPositionWhenComingFromOutside) {
                DFMLogger.logMessage(TAG, "onLocationChanged mustShowPositionWhenComingFromOutside");

                if (currentLocation != null && sendDestinationPosition != null) {
                    new SearchPositionByCoordinates().execute(sendDestinationPosition);
                    mustShowPositionWhenComingFromOutside = false;
                }
            } else {
                DFMLogger.logMessage(TAG, "onLocationChanged NOT mustShowPositionWhenComingFromOutside");

                final LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
                // 17 is a good zoom level for this action
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 17));
            }
            appHasJustStarted = false;
        }
    }

    /**
     * Shows a dialog returned by Google Play services for the connection error
     * code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
    private void showErrorDialog(final int errorCode) {
        DFMLogger.logMessage(TAG, "showErrorDialog errorCode=" + errorCode);

        // Get the error dialog from Google Play services
        final Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
                                                                         this,
                                                                         LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {
            // Create a new DialogFragment in which to show the error dialog
            final ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(getSupportFragmentManager(), "Geofence detection");
        }
    }

    private void drawAndShowMultipleDistances(final List<LatLng> coordinates,
                                              final String message,
                                              final boolean isLoadingFromDB,
                                              final boolean mustApplyZoomIfNeeded) {
        DFMLogger.logMessage(TAG, "drawAndShowMultipleDistances");

        // Borramos los antiguos marcadores y lineas
        googleMap.clear();

        // Calculamos la distancia
        distanceMeasuredAsText = calculateDistance(coordinates);

        // Pintar todos menos el primero si es desde la posición actual
        addMarkers(coordinates, distanceMeasuredAsText, message, isLoadingFromDB);

        // Añadimos las líneas
        addLines(coordinates, isLoadingFromDB);

        // Aquí hacer la animación de la cámara
        moveCameraZoom(coordinates.get(0), coordinates.get(coordinates.size() - 1), mustApplyZoomIfNeeded);

        // Muestra el perfil de elevación si está en las preferencias
        // y si está conectado a internet
        if (DFMPreferences.shouldShowElevationChart(appContext) && isOnline(appContext)) {
            getElevation(coordinates);
        }
    }

    /**
     * Adds a marker to the map in a specified position and shows its info
     * window.
     *
     * @param coordinates     Positions list.
     * @param distance        Distance to destination.
     * @param message         Destination address (if needed).
     * @param isLoadingFromDB Indicates whether we are loading data from database.
     */
    private void addMarkers(final List<LatLng> coordinates,
                            final String distance,
                            final String message,
                            final boolean isLoadingFromDB) {
        DFMLogger.logMessage(TAG, "addMarkers");

        for (int i = 0; i < coordinates.size(); i++) {
            if ((i == 0 && (isLoadingFromDB || getSelectedDistanceMode() == DistanceMode.DISTANCE_FROM_ANY_POINT)) ||
                (i == coordinates.size() - 1)) {
                final LatLng coordinate = coordinates.get(i);
                final Marker marker = addMarker(coordinate);

                if (i == coordinates.size() - 1) {
                    marker.setTitle(message + distance);
                    marker.showInfoWindow();
                }
            }
        }
    }

    private Marker addMarker(final LatLng coordinate) {
        DFMLogger.logMessage(TAG, "addMarker");

        return googleMap.addMarker(new MarkerOptions().position(coordinate));
    }

    private void addLines(final List<LatLng> coordinates, final boolean isLoadingFromDB) {
        DFMLogger.logMessage(TAG, "addLines");

        for (int i = 0; i < coordinates.size() - 1; i++) {
            addLine(coordinates.get(i), coordinates.get(i + 1), isLoadingFromDB);
        }
    }

    /**
     * Adds a line between start and end positions.
     *
     * @param start Start position.
     * @param end   Destination position.
     */
    private void addLine(final LatLng start, final LatLng end, final boolean isLoadingFromDB) {
        DFMLogger.logMessage(TAG, "addLine");

        final PolylineOptions lineOptions = new PolylineOptions().add(start).add(end);
        lineOptions.width(3 * getResources().getDisplayMetrics().density);
        lineOptions.color(isLoadingFromDB ? Color.YELLOW : Color.GREEN);
        googleMap.addPolyline(lineOptions);
    }

    /**
     * Returns the distance between start and end positions normalized by device
     * locale.
     *
     * @param coordinates position list.
     * @return The normalized distance.
     */
    private String calculateDistance(final List<LatLng> coordinates) {
        DFMLogger.logMessage(TAG, "calculateDistance");

        double distanceInMetres = Utils.calculateDistanceInMetres(coordinates);

        return Haversine.normalizeDistance(distanceInMetres, getAmericanOrEuropeanLocale());
    }

    /**
     * Moves camera position and applies zoom if needed.
     *
     * @param p1 Start position.
     * @param p2 Destination position.
     */
    private void moveCameraZoom(final LatLng p1, final LatLng p2, final boolean mustApplyZoomIfNeeded) {
        DFMLogger.logMessage(TAG, "moveCameraZoom");

        double centerLat = 0.0;
        double centerLon = 0.0;

        // Diferenciamos según preferencias
        final String centre = DFMPreferences.getAnimationPreference(getBaseContext());
        if (DFMPreferences.ANIMATION_CENTRE_VALUE.equals(centre)) {
            centerLat = (p1.latitude + p2.latitude) / 2;
            centerLon = (p1.longitude + p2.longitude) / 2;
        } else if (DFMPreferences.ANIMATION_DESTINATION_VALUE.equals(centre)) {
            centerLat = p2.latitude;
            centerLon = p2.longitude;
        } else if (centre.equals(DFMPreferences.NO_ANIMATION_DESTINATION_VALUE)) {
            return;
        }

        if (mustApplyZoomIfNeeded) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(centerLat, centerLon),
                                                                      Utils.calculateZoom(p1, p2)));
        } else {
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(p2.latitude, p2.longitude)));
        }
    }

    /**
     * Calculates elevation points in background and shows elevation chart.
     *
     * @param coordinates Positions list.
     */
    private void getElevation(final List<LatLng> coordinates) {
        DFMLogger.logMessage(TAG, "getElevation");

        String positionListUrlParameter = "";
        for (int i = 0; i < coordinates.size(); i++) {
            final LatLng coordinate = coordinates.get(i);
            positionListUrlParameter += String.valueOf(coordinate.latitude) +
                                        "," +
                                        String.valueOf(coordinate.longitude);
            if (i != coordinates.size() - 1) {
                positionListUrlParameter += "|";
            }
        }
        if (positionListUrlParameter.isEmpty()) {
            final IllegalStateException illegalStateException = new IllegalStateException("Coordinates list empty");
            DFMLogger.logException(illegalStateException);
            throw illegalStateException;
        }

        if (showingElevationTask != null) {
            DFMLogger.logMessage(TAG, "getElevation cancelling previous elevation asynctask");
            showingElevationTask.cancel(true);
        }
        showingElevationTask = new GetAltitude().execute(positionListUrlParameter);
    }

    /**
     * Sets map attending to the action which is performed.
     */
    private void fixMapPadding() {
        DFMLogger.logMessage(TAG, "fixMapPadding");

        if (bannerShown) {
            DFMLogger.logMessage(TAG, "fixMapPadding bannerShown");

            if (elevationChartShown) {
                DFMLogger.logMessage(TAG, "fixMapPadding elevationChartShown");

                googleMap.setPadding(0, rlElevationChart.getHeight(), 0, banner.getLayoutParams().height);
            } else {
                DFMLogger.logMessage(TAG, "fixMapPadding NOT elevationChartShown");

                googleMap.setPadding(0, 0, 0, banner.getLayoutParams().height);
            }
        } else {
            DFMLogger.logMessage(TAG, "fixMapPadding NOT bannerShown");

            if (elevationChartShown) {
                DFMLogger.logMessage(TAG, "fixMapPadding elevationChartShown");

                googleMap.setPadding(0, rlElevationChart.getHeight(), 0, 0);
            } else {
                DFMLogger.logMessage(TAG, "fixMapPadding NOT elevationChartShown");

                googleMap.setPadding(0, 0, 0, 0);
            }
        }
    }

    private enum DistanceMode {
        DISTANCE_FROM_CURRENT_POINT,
        DISTANCE_FROM_ANY_POINT
    }

    /**
     * A subclass of AsyncTask that calls getFromLocationName() in the background.
     */
    private class SearchPositionByName extends AsyncTask<Object, Void, Integer> {

        private final String TAG = SearchPositionByName.class.getSimpleName();

        protected List<Address>  addressList;
        protected StringBuilder  fullAddress;
        protected LatLng         selectedPosition;
        protected ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            DFMLogger.logMessage(TAG, "onPreExecute");

            addressList = null;
            fullAddress = new StringBuilder();
            selectedPosition = null;

            // Comprobamos que haya conexión con internet (WiFi o Datos)
            if (!isOnline(appContext)) {
                showWifiAlertDialog();

                // Restauramos el menú y que vuelva a empezar de nuevo
                MenuItemCompat.collapseActionView(searchMenuItem);
                cancel(false);
            } else {
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setTitle(R.string.progressdialog_search_position_title);
                progressDialog.setMessage(getString(R.string.progressdialog_search_position_message));
                progressDialog.setCancelable(false);
                progressDialog.setIndeterminate(true);
                progressDialog.show();
            }
        }

        @Override
        protected Integer doInBackground(Object... params) {
            DFMLogger.logMessage(TAG, "doInBackground");

            /* get latitude and longitude from the addressList */
            final Geocoder geoCoder = new Geocoder(appContext, Locale.getDefault());
            try {
                addressList = geoCoder.getFromLocationName((String) params[0], 5);
            } catch (IOException e) {
                e.printStackTrace();
                DFMLogger.logException(e);
                return -1; // Network is unavailable or any other I/O problem occurs
            }
            if (addressList == null) {
                return -3; // No backend service available
            } else if (addressList.isEmpty()) {
                return -2; // No matches were found
            } else {
                return 0;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            DFMLogger.logMessage(TAG, "onPostExecute result=" + result);

            switch (result) {
                case 0:
                    if (addressList != null && addressList.size() > 0) {
                        // Si hay varios, elegimos uno. Si solo hay uno, mostramos ese
                        if (addressList.size() == 1) {
                            processSelectedAddress(0);
                            handleSelectedAddress();
                        } else {
                            final AddressSugestionsDialogFragment addressSugestionsDialogFragment = new AddressSugestionsDialogFragment();
                            addressSugestionsDialogFragment.setAddressList(addressList);
                            addressSugestionsDialogFragment.setOnDialogActionListener(new AddressSugestionsDialogFragment.OnDialogActionListener() {
                                @Override
                                public void onItemClick(int position) {
                                    processSelectedAddress(position);
                                    handleSelectedAddress();
                                }
                            });
                            addressSugestionsDialogFragment.show(getSupportFragmentManager(), null);
                        }
                    }
                    break;
                case -1:
                    toastIt(getString(R.string.toast_no_find_address), appContext);
                    break;
                case -2:
                    toastIt(getString(R.string.toast_no_results), appContext);
                    break;
                case -3:
                    toastIt(getString(R.string.toast_no_find_address), appContext);
                    break;
            }
            progressDialog.dismiss();
            if (searchMenuItem != null) {
                MenuItemCompat.collapseActionView(searchMenuItem);
            }
        }

        private void handleSelectedAddress() {
            DFMLogger.logMessage(TAG, "handleSelectedAddress" + getSelectedDistanceMode());

            if (getSelectedDistanceMode() == DistanceMode.DISTANCE_FROM_ANY_POINT) {
                coordinates.add(selectedPosition);
                if (coordinates.isEmpty()) {
                    DFMLogger.logMessage(TAG, "handleSelectedAddress empty coordinates list");

                    // add marker
                    final Marker marker = addMarker(selectedPosition);
                    marker.setTitle(fullAddress.toString());
                    marker.showInfoWindow();
                    // moveCamera
                    moveCameraZoom(selectedPosition, selectedPosition, false);
                    distanceMeasuredAsText = calculateDistance(Lists.newArrayList(selectedPosition, selectedPosition));
                    // That means we are looking for a first position, so we want to calculate a distance starting
                    // from here
                    calculatingDistance = true;
                } else {
                    drawAndShowMultipleDistances(coordinates, fullAddress.toString(), false, true);
                }
            } else {
                if (!appHasJustStarted) {
                    DFMLogger.logMessage(TAG, "handleSelectedAddress appHasJustStarted");

                    if (coordinates == null || coordinates.isEmpty()) {
                        DFMLogger.logMessage(TAG, "handleSelectedAddress empty coordinates list");

                        coordinates = Lists.newArrayList();
                        coordinates.add(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                    }
                    coordinates.add(selectedPosition);
                    drawAndShowMultipleDistances(coordinates, fullAddress.toString(), false, true);
                } else {
                    DFMLogger.logMessage(TAG, "handleSelectedAddress NOT appHasJustStarted");

                    // Coming from View Action Intent
                    sendDestinationPosition = selectedPosition;
                }
            }
        }

        /**
         * Processes the address selected by the user and sets the new destination
         * position.
         *
         * @param item The item index in the AlertDialog.
         */
        protected void processSelectedAddress(final int item) {
            DFMLogger.logMessage(TAG, "processSelectedAddress item=" + item);

            // Fill address info to show in the marker info window
            final Address address = addressList.get(item);
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                fullAddress.append(address.getAddressLine(i)).append("\n");
            }
            selectedPosition = new LatLng(address.getLatitude(), address.getLongitude());
        }
    }

    /**
     * A subclass of SearchPositionByName to get position by coordinates.
     */
    private class SearchPositionByCoordinates extends SearchPositionByName {

        private final String TAG = SearchPositionByCoordinates.class.getSimpleName();

        @Override
        protected Integer doInBackground(Object... params) {
            DFMLogger.logMessage(TAG, "doInBackground");

            /* get latitude and longitude from the addressList */
            final Geocoder geoCoder = new Geocoder(appContext, Locale.getDefault());
            final LatLng latLng = (LatLng) params[0];
            try {
                addressList = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            } catch (final IOException e) {
                e.printStackTrace();
                DFMLogger.logException(e);
                return -1; // No encuentra una dirección, no puede conectar con el servidor
            } catch (final IllegalArgumentException e) {
                final IllegalArgumentException illegalArgumentException = new IllegalArgumentException(String.format("Error en latitud=%f o longitud=%f.\n%s",
                                                                                                                     latLng.latitude,
                                                                                                                     latLng.longitude,
                                                                                                                     e.toString()));
                DFMLogger.logException(illegalArgumentException);
                throw illegalArgumentException;
            }
            if (addressList == null) {
                return -3; // empty list if there is no backend service available
            } else if (addressList.size() > 0) {
                return 0;
            } else {
                return -2; // null if no matches were found // Cuando no hay conexión que sirva
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            DFMLogger.logMessage(TAG, "onPostExecute result=" + result);

            switch (result) {
                case 0:
                    processSelectedAddress(0);
                    drawAndShowMultipleDistances(Lists.newArrayList(new LatLng(currentLocation.getLatitude(),
                                                                               currentLocation.getLongitude()),
                                                                    selectedPosition),
                                                 fullAddress.toString(),
                                                 false,
                                                 true);
                    break;
                case -1:
                    toastIt(getString(R.string.toast_no_find_address), appContext);
                    break;
                case -2:
                    toastIt(getString(R.string.toast_no_results), appContext);
                    break;
                case -3:
                    toastIt(getString(R.string.toast_no_find_address), appContext);
                    break;
            }
            progressDialog.dismiss();
        }

    }

    /**
     * A subclass of AsyncTask that gets elevation points from coordinates in
     * background and shows an elevation chart.
     *
     * @author David
     */
    private class GetAltitude extends AsyncTask<String, Void, Double> {

        private final String TAG = GetAltitude.class.getSimpleName();

        private HttpClient httpClient = null;
        private HttpGet    httpGet    = null;
        private HttpResponse httpResponse;
        private String       responseAsString;
        private InputStream inputStream = null;
        private JSONObject responseJSON;

        @Override
        protected void onPreExecute() {
            DFMLogger.logMessage(TAG, "onPreExecute");

            httpClient = new DefaultHttpClient();
            responseAsString = null;

            // Delete elevation chart if exists
            if (graphView != null) {
                rlElevationChart.removeView(graphView);
            }
            rlElevationChart.setVisibility(View.INVISIBLE);
            graphView = null;
            elevationChartShown = false;
            fixMapPadding();
        }

        @Override
        protected Double doInBackground(String... params) {
            DFMLogger.logMessage(TAG, "doInBackground");

            httpGet = new HttpGet("http://maps.googleapis.com/maps/api/elevation/json?sensor=true"
                                  + "&path=" + Uri.encode(params[0])
                                  + "&samples=" + ELEVATION_SAMPLES);
            httpGet.setHeader("content-type", "application/json");
            try {
                httpResponse = httpClient.execute(httpGet);
                inputStream = httpResponse.getEntity().getContent();
                if (inputStream != null) {
                    responseAsString = Utils.convertInputStreamToString(inputStream);
                    responseJSON = new JSONObject(responseAsString);
                    if (responseJSON.get("status").equals("OK")) {
                        buildElevationChart(responseJSON.getJSONArray("results"));
                    }
                }
                // TODO merge this catches!
            } catch (IllegalStateException | IOException | JSONException e) {
                e.printStackTrace();
                DFMLogger.logException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Double result) {
            DFMLogger.logMessage(TAG, "onPostExecute result=" + result);

            showElevationProfileChart();
            // When HttpClient instance is no longer needed
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpClient.getConnectionManager().shutdown();
        }

        /**
         * Builds the information about the elevation profile chart. Use this in
         * a background task.
         *
         * @param array JSON array with the response data.
         * @throws JSONException
         */
        private void buildElevationChart(final JSONArray array) throws JSONException {
            DFMLogger.logMessage(TAG, "buildElevationChart");

            // Creates the serie and adds data to it
            final GraphViewSeries series =
                    new GraphViewSeries(null,
                                        new GraphViewSeriesStyle(getResources().getColor(R.color.elevation_chart_line),
                                                                 (int) (3 * DEVICE_DENSITY)),
                                        new GraphView.GraphViewData[]{});

            final Locale locale = getAmericanOrEuropeanLocale();

            for (int w = 0; w < array.length(); w++) {
                series.appendData(new GraphView.GraphViewData(w,
                                                              Haversine.normalizeAltitudeByLocale(Double.valueOf(array.getJSONObject(w)
                                                                                                                      .get("elevation")
                                                                                                                      .toString()),
                                                                                                  locale)),
                                  false,
                                  array.length());
            }

            // Creates the line and add it to the chart
            graphView = new LineGraphView(appContext, getString(R.string.elevation_chart_title,
                                                                Haversine.getAltitudeUnitByLocale(locale)));
            graphView.addSeries(series);
            graphView.getGraphViewStyle().setGridColor(Color.TRANSPARENT);
            graphView.getGraphViewStyle().setNumHorizontalLabels(1); // Con cero no va
            graphView.getGraphViewStyle().setTextSize(15 * DEVICE_DENSITY);
            graphView.getGraphViewStyle().setVerticalLabelsWidth((int) (50 * DEVICE_DENSITY));
        }

        /**
         * Shows the elevation profile chart.
         */
        private void showElevationProfileChart() {
            DFMLogger.logMessage(TAG, "showElevationProfileChart");

            if (graphView != null) {
                rlElevationChart.setVisibility(LinearLayout.VISIBLE);
                rlElevationChart.setBackgroundColor(getResources().getColor(R.color.elevation_chart_background));
                rlElevationChart.addView(graphView);
                elevationChartShown = true;
                fixMapPadding();

                ivCloseElevationChart.setVisibility(View.VISIBLE);
                ivCloseElevationChart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rlElevationChart.removeView(graphView);
                        rlElevationChart.setVisibility(View.INVISIBLE);
                        elevationChartShown = false;
                        fixMapPadding();
                    }
                });
            }
        }
    }

    private Locale getAmericanOrEuropeanLocale() {
        final String defaultUnit = DFMPreferences.getMeasureUnitPreference(getBaseContext());
        return DFMPreferences.MEASURE_AMERICAN_UNIT_VALUE.equals(defaultUnit) ? Locale.US : Locale.FRANCE;
    }
}
