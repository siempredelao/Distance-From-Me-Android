/*
 * Copyright (c) 2018 David Aguiar Gonzalez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gc.david.dfm.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import dagger.Lazy;
import gc.david.dfm.ConnectionManager;
import gc.david.dfm.DFMApplication;
import gc.david.dfm.DFMPreferences;
import gc.david.dfm.PreferencesProvider;
import gc.david.dfm.R;
import gc.david.dfm.Utils;
import gc.david.dfm.adapter.MarkerInfoWindowAdapter;
import gc.david.dfm.address.domain.GetAddressCoordinatesByNameUseCase;
import gc.david.dfm.address.domain.GetAddressNameByCoordinatesUseCase;
import gc.david.dfm.address.presentation.Address;
import gc.david.dfm.address.presentation.AddressPresenter;
import gc.david.dfm.dagger.DaggerMainComponent;
import gc.david.dfm.dagger.MainModule;
import gc.david.dfm.dagger.RootModule;
import gc.david.dfm.dagger.StorageModule;
import gc.david.dfm.deviceinfo.DeviceInfo;
import gc.david.dfm.deviceinfo.PackageManager;
import gc.david.dfm.distance.domain.GetPositionListUseCase;
import gc.david.dfm.distance.domain.LoadDistancesUseCase;
import gc.david.dfm.elevation.domain.ElevationUseCase;
import gc.david.dfm.elevation.presentation.Elevation;
import gc.david.dfm.elevation.presentation.ElevationPresenter;
import gc.david.dfm.feedback.Feedback;
import gc.david.dfm.feedback.FeedbackPresenter;
import gc.david.dfm.logger.DFMLogger;
import gc.david.dfm.map.Haversine;
import gc.david.dfm.map.LocationUtils;
import gc.david.dfm.model.Distance;
import gc.david.dfm.model.Position;
import gc.david.dfm.service.GeofencingService;
import gc.david.dfm.ui.animation.AnimatorUtil;
import gc.david.dfm.ui.dialog.AddressSuggestionsDialogFragment;
import gc.david.dfm.ui.dialog.DistanceSelectionDialogFragment;
import gc.david.dfm.ui.dialog.ErrorDialogFragment;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;
import static butterknife.ButterKnife.bind;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
                                                               OnMapReadyCallback,
                                                               OnMapLongClickListener,
                                                               OnMapClickListener,
                                                               OnInfoWindowClickListener,
                                                               Elevation.View,
                                                               Address.View {

    private static final String   TAG                      = MainActivity.class.getSimpleName();
    private static final String[] PERMISSIONS              = {ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION};
    private static final int      PERMISSIONS_REQUEST_CODE = 2;

    @BindView(R.id.elevationchart)
    protected RelativeLayout       rlElevationChart;
    @BindView(R.id.closeChart)
    protected ImageView            ivCloseElevationChart;
    @BindView(R.id.tbMain)
    protected Toolbar              tbMain;
    @BindView(R.id.drawer_layout)
    protected DrawerLayout         drawerLayout;
    @BindView(R.id.nvDrawer)
    protected NavigationView       nvDrawer;
    @BindView(R.id.main_activity_showchart_floatingactionbutton)
    protected FloatingActionButton fabShowChart;
    @BindView(R.id.main_activity_mylocation_floatingactionbutton)
    protected FloatingActionButton fabMyLocation;

    @Inject
    protected Context                appContext;
    @Inject
    protected Lazy<PackageManager>   packageManager;
    @Inject
    protected Lazy<DeviceInfo>       deviceInfo;
    @Inject
    protected ElevationUseCase       elevationUseCase;
    @Inject
    protected ConnectionManager      connectionManager;
    @Inject
    protected PreferencesProvider    preferencesProvider;
    @Inject
    protected GetAddressCoordinatesByNameUseCase getAddressCoordinatesByNameUseCase;
    @Inject
    protected GetAddressNameByCoordinatesUseCase getAddressNameByCoordinatesUseCase;
    @Inject
    protected LoadDistancesUseCase   loadDistancesUseCase;
    @Inject
    protected GetPositionListUseCase getPositionListUseCase;

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

    private GoogleMap    googleMap                             = null;
    private Location     currentLocation                       = null;
    // Moves to current position if app has just started
    private boolean      appHasJustStarted                     = true;
    private String       distanceMeasuredAsText                = "";
    private MenuItem     searchMenuItem                        = null;
    // Show position if we come from other app (p.e. Whatsapp)
    private boolean      mustShowPositionWhenComingFromOutside = false;
    private LatLng       sendDestinationPosition               = null;
    private GraphView    graphView                             = null;
    private final List<LatLng> coordinates                     = new ArrayList<>();
    private boolean        calculatingDistance;
    private ProgressDialog progressDialog;

    private Elevation.Presenter elevationPresenter;
    private Address.Presenter   addressPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DFMLogger.INSTANCE.logMessage(TAG, "onCreate savedInstanceState=" + Utils.INSTANCE.dumpBundleToString(savedInstanceState));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DaggerMainComponent.builder()
                           .rootModule(new RootModule((DFMApplication) getApplication()))
                           .storageModule(new StorageModule())
                           .mainModule(new MainModule())
                           .build()
                           .inject(this);
        bind(this);

        setSupportActionBar(tbMain);

        final ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeButtonEnabled(true);
            final Drawable upArrow = appContext.getDrawable(R.drawable.ic_menu_white_24dp);
            supportActionBar.setHomeAsUpIndicator(upArrow);
        }

        elevationPresenter = new ElevationPresenter(this, elevationUseCase, connectionManager, preferencesProvider);
        addressPresenter = new AddressPresenter(this,
                                                getAddressCoordinatesByNameUseCase,
                                                getAddressNameByCoordinatesUseCase,
                                                connectionManager);

        final SupportMapFragment supportMapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        supportMapFragment.getMapAsync(this);

        if (!connectionManager.isOnline()) {
            showConnectionProblemsDialog();
        }

        handleIntents(getIntent());

        nvDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {
                    case R.id.menu_current_position:
                        menuItem.setChecked(true);
                        onStartingPointSelected();
                        if (SDK_INT >= M && !isLocationPermissionGranted()) {
                            Snackbar.make(drawerLayout,
                                          "This feature needs location permissions.",
                                          Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Settings", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    startActivity(intent);
                                }
                                    })
                                    .show();
                        }
                        return true;
                    case R.id.menu_any_position:
                        menuItem.setChecked(true);
                        onStartingPointSelected();
                        return true;
                    case R.id.menu_rate_app:
                        showRateDialog();
                        return true;
                    case R.id.menu_settings:
                        SettingsActivity.open(MainActivity.this);
                        return true;
                    case R.id.menu_help_feedback:
                        HelpAndFeedbackActivity.open(MainActivity.this);
                        return true;
                    case R.id.menu_about:
                        AboutActivity.open(MainActivity.this);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        googleMap = map;

        googleMap.getUiSettings().setMyLocationButtonEnabled(false);

        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        googleMap.setOnMapLongClickListener(this);
        googleMap.setOnMapClickListener(this);
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter(this));

        onStartingPointSelected();

        if (SDK_INT >= M && !isLocationPermissionGranted()) {
            requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
        } else {
            Utils.INSTANCE.toastIt(R.string.toast_loading_position, appContext);
            googleMap.setMyLocationEnabled(true);
            fabMyLocation.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            // not necessary to check both permissions, they fall under location group
            if (grantResults[0] == PERMISSION_GRANTED) {
                DFMLogger.INSTANCE.logMessage(TAG, "onRequestPermissionsResult GRANTED");

                Utils.INSTANCE.toastIt(R.string.toast_loading_position, appContext);
                googleMap.setMyLocationEnabled(true);
                fabMyLocation.setVisibility(View.VISIBLE);

                registerReceiver(locationReceiver, new IntentFilter(GeofencingService.GEOFENCE_RECEIVER_ACTION));
                startService(new Intent(this, GeofencingService.class));
            } else {
                DFMLogger.INSTANCE.logMessage(TAG, "onRequestPermissionsResult DENIED");
                fabMyLocation.setVisibility(View.GONE);
                nvDrawer.getMenu().findItem(R.id.menu_any_position).setChecked(true);
                onStartingPointSelected();
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng point) {
        DFMLogger.INSTANCE.logMessage(TAG, "onMapLongClick");

        calculatingDistance = true;

        if (getSelectedDistanceMode() == DistanceMode.DISTANCE_FROM_ANY_POINT) {
            if (coordinates.isEmpty()) {
                Utils.INSTANCE.toastIt(R.string.toast_first_point_needed, appContext);
            } else {
                coordinates.add(point);
                drawAndShowMultipleDistances(coordinates, "", false);
            }
        } else if (currentLocation != null) { // Without current location, we cannot calculate any distance
            if (getSelectedDistanceMode() == DistanceMode.DISTANCE_FROM_CURRENT_POINT && coordinates.isEmpty()) {
                coordinates.add(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
            }
            coordinates.add(point);
            drawAndShowMultipleDistances(coordinates, "", false);
        }

        calculatingDistance = false;
    }

    @Override
    public void onMapClick(LatLng point) {
        DFMLogger.INSTANCE.logMessage(TAG, "onMapClick");

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
            // Without current location, we cannot calculate any distance
            if (currentLocation != null) {
                if (!calculatingDistance) {
                    coordinates.clear();
                }
                calculatingDistance = true;

                if (coordinates.isEmpty()) {
                    googleMap.clear();
                    coordinates.add(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                }
                coordinates.add(point);
                googleMap.addMarker(new MarkerOptions().position(point));
            }
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        DFMLogger.INSTANCE.logMessage(TAG, "onInfoWindowClick");

        ShowInfoActivity.open(this, coordinates, distanceMeasuredAsText);
    }

    private void onStartingPointSelected() {
        if (getSelectedDistanceMode() == DistanceMode.DISTANCE_FROM_CURRENT_POINT) {
            DFMLogger.INSTANCE.logMessage(TAG, "onStartingPointSelected Distance from current point");
        } else {
            DFMLogger.INSTANCE.logMessage(TAG, "onStartingPointSelected Distance from any point");
        }

        calculatingDistance = false;

        coordinates.clear();

        if (googleMap != null) {
            googleMap.clear();
        }

        elevationPresenter.onReset();
    }

    private DistanceMode getSelectedDistanceMode() {
        return nvDrawer.getMenu().findItem(R.id.menu_current_position).isChecked()
               ? DistanceMode.DISTANCE_FROM_CURRENT_POINT
               : DistanceMode.DISTANCE_FROM_ANY_POINT;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        DFMLogger.INSTANCE.logMessage(TAG, "onNewIntent " + Utils.INSTANCE.dumpIntentToString(intent));

        setIntent(intent);
        handleIntents(intent);
    }

    private void handleIntents(final Intent intent) {
        if (intent != null) {
            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                handleSearchIntent(intent);
            } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                handleViewPositionIntent(intent);
            }
        }
    }

    private void handleSearchIntent(final Intent intent) {
        DFMLogger.INSTANCE.logMessage(TAG, "handleSearchIntent");

        // Para controlar instancias únicas, no queremos que cada vez que
        // busquemos nos inicie una nueva instancia de la aplicación
        final String query = intent.getStringExtra(SearchManager.QUERY);
        if (currentLocation != null) {
            addressPresenter.searchPositionByName(query);
            searchMenuItem.collapseActionView();
        }
        if (searchMenuItem != null) {
            searchMenuItem.collapseActionView();
        }
    }

    private void handleViewPositionIntent(final Intent intent) {
        final Uri uri = intent.getData();
        DFMLogger.INSTANCE.logMessage(TAG, "handleViewPositionIntent uri=" + uri.toString());

        final String uriScheme = uri.getScheme();
        if (uriScheme.equals("geo")) {
            handleGeoSchemeIntent(uri);
        } else if ((uriScheme.equals("http") || uriScheme.equals("https"))
                   && (uri.getHost().equals("maps.google.com"))) { // Manage maps.google.com?q=latitude,longitude
            handleMapsHostIntent(uri);
        } else {
            final Exception exception = new Exception("Imposible tratar la query " + uri.toString());
            DFMLogger.INSTANCE.logException(exception);
            Utils.INSTANCE.toastIt("Unable to parse address", this);
        }
    }

    private void handleGeoSchemeIntent(final Uri uri) {
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
                    addressPresenter.searchPositionByName(destination);
                    searchMenuItem.collapseActionView();
                    mustShowPositionWhenComingFromOutside = true;
                }
            } else { // Manage geo:latitude,longitude or geo:latitude,longitude?z=zoom
                setDestinationPosition(matcher);
            }
        } else {
            final NoSuchFieldException noSuchFieldException = new NoSuchFieldException("Error al obtener las coordenadas. Matcher = " +
                                                                                       matcher.toString());
            DFMLogger.INSTANCE.logException(noSuchFieldException);
            Utils.INSTANCE.toastIt("Unable to parse address", this);
        }
    }

    private void handleMapsHostIntent(final Uri uri) {
        final String queryParameter = uri.getQueryParameter("q");
        if (queryParameter != null) {
            final Matcher matcher = getMatcherForUri(queryParameter);
            if (matcher.find()) {
                setDestinationPosition(matcher);
            } else {
                final NoSuchFieldException noSuchFieldException = new NoSuchFieldException("Error al obtener las coordenadas. Matcher = " +
                                                                                           matcher.toString());
                DFMLogger.INSTANCE.logException(noSuchFieldException);
                Utils.INSTANCE.toastIt("Unable to parse address", this);
            }
        } else {
            final NoSuchFieldException noSuchFieldException = new NoSuchFieldException("Query sin parámetro q.");
            DFMLogger.INSTANCE.logException(noSuchFieldException);
            Utils.INSTANCE.toastIt("Unable to parse address", this);
        }
    }

    private void setDestinationPosition(final Matcher matcher) {
        DFMLogger.INSTANCE.logMessage(TAG, "setDestinationPosition");

        sendDestinationPosition = new LatLng(Double.valueOf(matcher.group(1)), Double.valueOf(matcher.group(2)));
        mustShowPositionWhenComingFromOutside = true;
    }

    private Matcher getMatcherForUri(final String schemeSpecificPart) {
        DFMLogger.INSTANCE.logMessage(TAG, "getMatcherForUri scheme=" + schemeSpecificPart);

        final String regex = "(\\-?\\d+\\.*\\d*),(\\-?\\d+\\.*\\d*)";
        final Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(schemeSpecificPart);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        searchMenuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchMenuItem.getActionView();
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        // Indicamos que la activity actual sea la buscadora
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(false);
        searchView.setQueryRefinementEnabled(true);
        searchView.setIconifiedByDefault(true);

        // TODO: 16.01.17 move this to presenter
        final MenuItem loadItem = menu.findItem(R.id.action_load);
        loadDistancesUseCase.execute(new LoadDistancesUseCase.Callback() {
            @Override
            public void onDistanceListLoaded(final List<Distance> distanceList) {
                if (distanceList.isEmpty()) {
                    loadItem.setVisible(false);
                }
            }

            @Override
            public void onError() {
                loadItem.setVisible(false);
            }
        });

        menu.findItem(R.id.action_crash).setVisible(!Utils.INSTANCE.isReleaseBuild());

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                DFMLogger.INSTANCE.logMessage(TAG, "onOptionsItemSelected home");
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_search:
                DFMLogger.INSTANCE.logMessage(TAG, "onOptionsItemSelected search");
                return true;
            case R.id.action_load:
                DFMLogger.INSTANCE.logMessage(TAG, "onOptionsItemSelected load distances from ddbb");
                loadDistancesFromDB();
                return true;
            case R.id.action_crash:
                DFMLogger.INSTANCE.logMessage(TAG, "onOptionsItemSelected crash");
                throw new RuntimeException("User forced crash");
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void loadDistancesFromDB() {
        // TODO: 16.01.17 move this to presenter
        loadDistancesUseCase.execute(new LoadDistancesUseCase.Callback() {
            @Override
            public void onDistanceListLoaded(final List<Distance> distanceList) {
                if (!distanceList.isEmpty()) {
                    final DistanceSelectionDialogFragment distanceSelectionDialogFragment = new DistanceSelectionDialogFragment();
                    distanceSelectionDialogFragment.setDistanceList(distanceList);
                    distanceSelectionDialogFragment.setOnDialogActionListener(new DistanceSelectionDialogFragment.OnDialogActionListener() {
                        @Override
                        public void onItemClick(int position) {
                            final Distance distance = distanceList.get(position);
                            getPositionListUseCase.execute(distance.getId(), new GetPositionListUseCase.Callback() {
                                @Override
                                public void onPositionListLoaded(final List<Position> positionList) {
                                    coordinates.clear();
                                    coordinates.addAll(Utils.INSTANCE.convertPositionListToLatLngList(positionList));

                                    drawAndShowMultipleDistances(coordinates, distance.getName() + "\n", true);
                                }

                                @Override
                                public void onError() {
                                    DFMLogger.INSTANCE.logException(new Exception("Unable to get position by id."));
                                }
                            });
                        }
                    });
                    distanceSelectionDialogFragment.show(getSupportFragmentManager(), null);
                }
            }

            @Override
            public void onError() {
                DFMLogger.INSTANCE.logException(new Exception("Unable to load distances."));
            }
        });
    }

    private void showRateDialog() {
        DFMLogger.INSTANCE.logMessage(TAG, "showRateDialog");

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

    private void openPlayStoreAppPage() {
        DFMLogger.INSTANCE.logMessage(TAG, "openPlayStoreAppPage");

        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=gc.david.dfm")));
        } catch (ActivityNotFoundException e) {
            DFMLogger.INSTANCE.logException(new Exception("Unable to open Play Store, rooted device?"));
        }
    }

    private void openFeedbackActivity() {
        DFMLogger.INSTANCE.logMessage(TAG, "openFeedbackActivity");

        new FeedbackPresenter(new Feedback.View() {
            @Override
            public void showError() {
                Utils.INSTANCE.toastIt(R.string.toast_send_feedback_error, appContext);
            }

            @Override
            public void showEmailClient(final Intent intent) {
                startActivity(intent);
            }

            @Override
            public Context context() {
                return appContext;
            }
        }, packageManager.get(), deviceInfo.get()).start();
    }

    /**
     * Called when the Activity is no longer visible at all. Stop updates and
     * disconnect.
     */
    @Override
    public void onStop() {
        DFMLogger.INSTANCE.logMessage(TAG, "onStop");

        super.onStop();
        try {
            unregisterReceiver(locationReceiver);
        } catch (IllegalArgumentException exception) {
            DFMLogger.INSTANCE.logMessage(TAG, "onStop receiver not registered, do nothing");
        }
        stopService(new Intent(this, GeofencingService.class));
    }

    /**
     * Called when the Activity is restarted, even before it becomes visible.
     */
    @Override
    public void onStart() {
        DFMLogger.INSTANCE.logMessage(TAG, "onStart");

        super.onStart();
        if (isLocationPermissionGranted()) {
            registerReceiver(locationReceiver, new IntentFilter(GeofencingService.GEOFENCE_RECEIVER_ACTION));
            startService(new Intent(this, GeofencingService.class));
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true);
            }
            fabMyLocation.setVisibility(View.VISIBLE);
        } else {
            fabMyLocation.setVisibility(View.GONE);
        }
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED;
    }

    /**
     * Called when the system detects that this Activity is now visible.
     */
    @Override
    public void onResume() {
        DFMLogger.INSTANCE.logMessage(TAG, "onResume");

        super.onResume();
        invalidateOptionsMenu();
        checkPlayServices();
    }

    @Override
    public void onDestroy() {
        DFMLogger.INSTANCE.logMessage(TAG, "onDestroy");

        elevationPresenter.onReset();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        DFMLogger.INSTANCE.logMessage(TAG,
                             String.format(Locale.getDefault(),
                                           "onActivityResult requestCode=%d, resultCode=%d, intent=%s",
                                           requestCode,
                                           resultCode,
                                           Utils.INSTANCE.dumpIntentToString(intent)));

        if (requestCode == LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                // Log the result
                // Log.d(LocationUtils.APPTAG, getString(R.string.resolved));

                // Display the result
                // mConnectionState.setText(R.string.connected);
                // mConnectionStatus.setText(R.string.resolved);
            } else { // If any other result was returned by Google Play services
                // Log the result
                // Log.d(LocationUtils.APPTAG,
                // getString(R.string.no_resolution));

                // Display the result
                // mConnectionState.setText(R.string.disconnected);
                // mConnectionStatus.setText(R.string.no_resolution);
            }
        }
    }

    private boolean checkPlayServices() {
        final GoogleApiAvailability googleApiAvailabilityInstance = GoogleApiAvailability.getInstance();
        final int resultCode = googleApiAvailabilityInstance.isGooglePlayServicesAvailable(appContext);

        if (resultCode == ConnectionResult.SUCCESS) {
            DFMLogger.INSTANCE.logMessage(TAG, "checkPlayServices success");

            return true;
        } else {
            if (googleApiAvailabilityInstance.isUserResolvableError(resultCode)) {
                DFMLogger.INSTANCE.logMessage(TAG, "checkPlayServices isUserRecoverableError");

                final int RQS_GooglePlayServices = 1;
                googleApiAvailabilityInstance.getErrorDialog(this, resultCode, RQS_GooglePlayServices).show();
            } else {
                DFMLogger.INSTANCE.logMessage(TAG, "checkPlayServices device not supported, finishing");

                finish();
            }
            return false;
        }
    }

    /**
     * Called by Location Services if the attempt to Location Services fails.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        DFMLogger.INSTANCE.logMessage(TAG, "onConnectionFailed");

        /*
         * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
        if (connectionResult.hasResolution()) {
            DFMLogger.INSTANCE.logMessage(TAG, "onConnectionFailed connection has resolution");

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
                DFMLogger.INSTANCE.logException(e);
            }
        } else {
            DFMLogger.INSTANCE.logMessage(TAG, "onConnectionFailed connection does not have resolution");
            // If no resolution is available, display a dialog to the user with
            // the error.
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    public void onLocationChanged(final Location location) {
        DFMLogger.INSTANCE.logMessage(TAG, "onLocationChanged");

        if (currentLocation != null) {
            currentLocation.set(location);
        } else {
            currentLocation = new Location(location);
        }

        if (appHasJustStarted) {
            DFMLogger.INSTANCE.logMessage(TAG, "onLocationChanged appHasJustStarted");

            if (mustShowPositionWhenComingFromOutside) {
                DFMLogger.INSTANCE.logMessage(TAG, "onLocationChanged mustShowPositionWhenComingFromOutside");

                if (currentLocation != null && sendDestinationPosition != null) {
                    addressPresenter.searchPositionByCoordinates(sendDestinationPosition);

                    mustShowPositionWhenComingFromOutside = false;
                }
            } else {
                DFMLogger.INSTANCE.logMessage(TAG, "onLocationChanged NOT mustShowPositionWhenComingFromOutside");

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
        DFMLogger.INSTANCE.logMessage(TAG, "showErrorDialog errorCode=" + errorCode);

        final Dialog errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(this,
                                                                                      errorCode,
                LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {
            final ErrorDialogFragment errorFragment = new ErrorDialogFragment();
            errorFragment.setDialog(errorDialog);
            errorFragment.show(getSupportFragmentManager(), "Geofence detection");
        }
    }

    private void drawAndShowMultipleDistances(final List<LatLng> coordinates,
                                              final String message,
                                              final boolean isLoadingFromDB) {
        DFMLogger.INSTANCE.logMessage(TAG, "drawAndShowMultipleDistances");

        googleMap.clear();

        distanceMeasuredAsText = calculateDistance(coordinates);

        addMarkers(coordinates, distanceMeasuredAsText, message, isLoadingFromDB);

        addLines(coordinates, isLoadingFromDB);

        moveCameraZoom(coordinates);

        elevationPresenter.buildChart(coordinates);
    }

    private void addMarkers(final List<LatLng> coordinates,
                            final String distance,
                            final String message,
                            final boolean isLoadingFromDB) {
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
        return googleMap.addMarker(new MarkerOptions().position(coordinate));
    }

    private void addLines(final List<LatLng> coordinates, final boolean isLoadingFromDB) {
        for (int i = 0; i < coordinates.size() - 1; i++) {
            addLine(coordinates.get(i), coordinates.get(i + 1), isLoadingFromDB);
        }
    }

    private void addLine(final LatLng start, final LatLng end, final boolean isLoadingFromDB) {
        final PolylineOptions lineOptions = new PolylineOptions().add(start).add(end);
        lineOptions.width(getResources().getDimension(R.dimen.map_line_width));
        lineOptions.color(isLoadingFromDB ? Color.YELLOW : Color.GREEN);
        googleMap.addPolyline(lineOptions);
    }

    private String calculateDistance(final List<LatLng> coordinates) {
        double distanceInMetres = Utils.INSTANCE.calculateDistanceInMetres(coordinates);

        return Haversine.INSTANCE.normalizeDistance(distanceInMetres, getAmericanOrEuropeanLocale());
    }

    private void moveCameraZoom(final List<LatLng> coordinatesList) {
        final String centre = DFMPreferences.INSTANCE.getAnimationPreference(getBaseContext());
        switch (centre) {
            case DFMPreferences.ANIMATION_CENTRE_VALUE:
                final LatLngBounds.Builder latLngBoundsBuilder = new LatLngBounds.Builder();
                for (LatLng latLng : coordinatesList) {
                    latLngBoundsBuilder.include(latLng);
                }
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBoundsBuilder.build(), 100));
                break;
            case DFMPreferences.ANIMATION_DESTINATION_VALUE:
                final LatLng lastCoordinates = coordinatesList.get(coordinatesList.size() - 1);
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lastCoordinates.latitude,
                        lastCoordinates.longitude)));
                break;
            case DFMPreferences.NO_ANIMATION_DESTINATION_VALUE:
                // nothing
                break;
        }
    }

    private void fixMapPadding() {
        DFMLogger.INSTANCE.logMessage(TAG,
                             String.format("fixMapPadding elevationChartShown %s",
                                           rlElevationChart.isShown()));
        googleMap.setPadding(0,
                             rlElevationChart.isShown() ? rlElevationChart.getHeight() : 0,
                             0,
                             0);
    }

    @Override
    public void setPresenter(final Elevation.Presenter presenter) {
        this.elevationPresenter = presenter;
    }

    @Override
    public void hideChart() {
        rlElevationChart.setVisibility(View.INVISIBLE);
        fabShowChart.setVisibility(View.INVISIBLE);
        fixMapPadding();
    }

    @Override
    public void showChart() {
        rlElevationChart.setVisibility(View.VISIBLE);
        fixMapPadding();
    }

    @Override
    public void buildChart(final List<Double> elevationList) {
        final Locale locale = getAmericanOrEuropeanLocale();

        // Creates the series and adds data to it
        final GraphViewSeries series = buildGraphViewSeries(elevationList, locale);

        if (graphView == null) {
            graphView = new LineGraphView(appContext,
                                          getString(R.string.elevation_chart_title,
                                                    Haversine.INSTANCE.getAltitudeUnitByLocale(locale)));
            final GraphViewStyle graphViewStyle = graphView.getGraphViewStyle();
            graphViewStyle.setGridColor(Color.TRANSPARENT);
            graphViewStyle.setNumHorizontalLabels(1); // Not working with zero?
            graphViewStyle.setTextSize(getResources().getDimension(R.dimen.elevation_chart_text_size));
            graphViewStyle.setVerticalLabelsWidth(getResources().getDimensionPixelSize(R.dimen.elevation_chart_vertical_label_width));
            rlElevationChart.addView(graphView);

            ivCloseElevationChart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    elevationPresenter.onCloseChart();
                }
            });
        }
        graphView.removeAllSeries();
        graphView.addSeries(series);

        elevationPresenter.onChartBuilt();
    }

    @NonNull
    private GraphViewSeries buildGraphViewSeries(final List<Double> elevationList, final Locale locale) {
        final GraphViewSeriesStyle style = new GraphViewSeriesStyle(ContextCompat.getColor(getApplicationContext(),
                                                                                           R.color.elevation_chart_line),
                                                                    getResources().getDimensionPixelSize(R.dimen.elevation_chart_line_size));
        final GraphViewSeries series = new GraphViewSeries(null, style, new GraphView.GraphViewData[]{});

        for (int w = 0; w < elevationList.size(); w++) {
            series.appendData(new GraphView.GraphViewData(w,
                                                          Haversine.INSTANCE.normalizeAltitudeByLocale(elevationList.get(w),
                                                                                              locale)),
                              false,
                              elevationList.size());
        }
        return series;
    }

    @Override
    public void animateHideChart() {
        AnimatorUtil.replaceViews(rlElevationChart, fabShowChart);
    }

    @Override
    public void animateShowChart() {
        AnimatorUtil.replaceViews(fabShowChart, rlElevationChart);
    }

    @Override
    public boolean isMinimiseButtonShown() {
        return fabShowChart.isShown();
    }

    @Override
    public void logError(final String errorMessage) {
        DFMLogger.INSTANCE.logException(new Exception(errorMessage));
    }

    @OnClick(R.id.main_activity_showchart_floatingactionbutton)
    void onShowChartClick() {
        elevationPresenter.onOpenChart();
    }

    @OnClick(R.id.main_activity_mylocation_floatingactionbutton)
    void onMyLocationClick() {
        if (currentLocation != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(),
                                                                             currentLocation.getLongitude())));
        }
    }

    @Override
    public void setPresenter(final Address.Presenter presenter) {
        this.addressPresenter = presenter;
    }

    @Override
    public void showConnectionProblemsDialog() {
        DFMLogger.INSTANCE.logMessage(TAG, "showConnectionProblemsDialog");

        Utils.INSTANCE.showAlertDialog(android.provider.Settings.ACTION_SETTINGS,
                        R.string.dialog_connection_problems_title,
                        R.string.dialog_connection_problems_message,
                        R.string.dialog_connection_problems_positive_button,
                        R.string.dialog_connection_problems_negative_button,
                        this);
    }

    @Override
    public void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.progressdialog_search_position_title);
        progressDialog.setMessage(getString(R.string.progressdialog_search_position_message));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    @Override
    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void showCallError(final String errorMessage) {
        logError(errorMessage);
        Utils.INSTANCE.toastIt(R.string.toast_no_find_address, appContext);
    }

    @Override
    public void showNoMatchesMessage() {
        Utils.INSTANCE.toastIt(R.string.toast_no_results, appContext);
    }

    @Override
    public void showAddressSelectionDialog(final List<gc.david.dfm.address.domain.model.Address> addressList) {
        final AddressSuggestionsDialogFragment addressSuggestionsDialogFragment = new AddressSuggestionsDialogFragment();
        addressSuggestionsDialogFragment.setAddressList(addressList);
        addressSuggestionsDialogFragment.setOnDialogActionListener(new AddressSuggestionsDialogFragment.OnDialogActionListener() {
            @Override
            public void onItemClick(final int position) {
                addressPresenter.selectAddressInDialog(addressList.get(position));
            }
        });
        addressSuggestionsDialogFragment.show(getSupportFragmentManager(), null);
    }

    @Override
    public void showPositionByName(final gc.david.dfm.address.domain.model.Address address) {
        DFMLogger.INSTANCE.logMessage(TAG, "showPositionByName " + getSelectedDistanceMode());

        final LatLng addressCoordinates = address.getCoordinates();
        if (getSelectedDistanceMode() == DistanceMode.DISTANCE_FROM_ANY_POINT) {
            coordinates.add(addressCoordinates);
            if (coordinates.isEmpty()) {
                DFMLogger.INSTANCE.logMessage(TAG, "showPositionByName empty coordinates list");

                // add marker
                final Marker marker = addMarker(addressCoordinates);
                marker.setTitle(address.getFormattedAddress());
                marker.showInfoWindow();
                // moveCamera
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(addressCoordinates.latitude,
                                                                                 addressCoordinates.longitude)));
                distanceMeasuredAsText = "";
                // That means we are looking for a first position, so we want to calculate a distance starting
                // from here
                calculatingDistance = true;
            } else {
                drawAndShowMultipleDistances(coordinates, address.getFormattedAddress() + "\n", false);
            }
        } else {
            if (!appHasJustStarted) {
                DFMLogger.INSTANCE.logMessage(TAG, "showPositionByName appHasJustStarted");

                if (coordinates.isEmpty()) {
                    DFMLogger.INSTANCE.logMessage(TAG, "showPositionByName empty coordinates list");

                    coordinates.add(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                }
                coordinates.add(addressCoordinates);
                drawAndShowMultipleDistances(this.coordinates, address.getFormattedAddress() + "\n", false);
            } else {
                DFMLogger.INSTANCE.logMessage(TAG, "showPositionByName NOT appHasJustStarted");

                // Coming from View Action Intent
                sendDestinationPosition = addressCoordinates;
            }
        }
    }

    @Override
    public void showPositionByCoordinates(final gc.david.dfm.address.domain.model.Address address) {
        drawAndShowMultipleDistances(Arrays.asList(new LatLng(currentLocation.getLatitude(),
                                                              currentLocation.getLongitude()),
                                                   address.getCoordinates()),
                                     address.getFormattedAddress() + "\n",
                                     false);
    }

    private enum DistanceMode {
        DISTANCE_FROM_CURRENT_POINT,
        DISTANCE_FROM_ANY_POINT
    }

    private Locale getAmericanOrEuropeanLocale() {
        final String defaultUnit = DFMPreferences.INSTANCE.getMeasureUnitPreference(getBaseContext());
        return DFMPreferences.MEASURE_AMERICAN_UNIT_VALUE.equals(defaultUnit) ? Locale.US : Locale.FRANCE;
    }
}
