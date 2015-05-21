package gc.david.dfm.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.collect.Lists;
import com.inmobi.commons.InMobi;
import com.inmobi.monetization.IMBanner;
import com.inmobi.monetization.IMBannerListener;
import com.inmobi.monetization.IMErrorCode;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;
import com.splunk.mint.Mint;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.InjectView;
import gc.david.dfm.DFMApplication;
import gc.david.dfm.R;
import gc.david.dfm.adapter.DistanceAdapter;
import gc.david.dfm.adapter.MarkerInfoWindowAdapter;
import gc.david.dfm.adapter.NavigationDrawerItemAdapter;
import gc.david.dfm.map.Haversine;
import gc.david.dfm.map.LocationUtils;
import gc.david.dfm.model.DaoSession;
import gc.david.dfm.model.Distance;
import gc.david.dfm.model.Position;

import static butterknife.ButterKnife.inject;
import static gc.david.dfm.Utils.isOnline;
import static gc.david.dfm.Utils.showAlertDialog;
import static gc.david.dfm.Utils.toastIt;

/**
 * Implements the app main Activity.
 *
 * @author David
 */
public class MainActivity extends ActionBarActivity implements LocationListener,
                                                               GoogleApiClient.ConnectionCallbacks,
                                                               GoogleApiClient.OnConnectionFailedListener {

    private static final int ELEVATION_SAMPLES       = 100;
    private final        int FIRST_DRAWER_ITEM_INDEX = 1;

    @InjectView(R.id.elevationchart)
    protected RelativeLayout rlElevationChart;
    @InjectView(R.id.closeChart)
    protected ImageView      ivCloseElevationChart;

    private GoogleMap       googleMap                             = null;
    // A request to connect to Location Services
    private LocationRequest locationRequest                       = null;
    // Stores the current instantiation of the location client in this object
    private GoogleApiClient googleApiClient                       = null;
    private Location        currentLocation                       = null;
    // Moves to current position if app has just started
    private boolean         appHasJustStarted                     = true;
    private String          distanceMeasuredAsText                = "";
    private MenuItem        searchMenuItem                        = null;
    // Show position if we come from other app (p.e. Whatsapp)
    private boolean         mustShowPositionWhenComingFromOutside = false;
    private LatLng          sendDestinationPosition               = null;
    private IMBanner        banner                                = null;
    private boolean         bannerShown                           = false;
    private boolean         elevationChartShown                   = false;
    @SuppressWarnings("rawtypes")
    private AsyncTask       showingElevationTask                  = null;
    private GraphView       graphView                             = null;
    private float                 DEVICE_DENSITY;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout          drawerLayout;
    private ListView              drawerList;
    private DistanceMode          distanceMode;
    private List<LatLng>          coordinates;
    private boolean               calculatingDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mint.initAndStartSession(MainActivity.this, "6f2149e6");
        // Enable logging
        Mint.enableLogging(true);
        Mint.leaveBreadcrumb("MainActivity::onCreate");

        setContentView(R.layout.activity_main);
        inject(this);

        DEVICE_DENSITY = getResources().getDisplayMetrics().density;

        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                                                           .addConnectionCallbacks(this)
                                                           .addOnConnectionFailedListener(this)
                                                           .build();

        final SupportMapFragment fragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        googleMap = fragment.getMap();

        if (googleMap != null) {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

            // InMobi Ads
            InMobi.initialize(this, "9b61f509a1454023b5295d8aea4482c2");
            banner = (IMBanner) findViewById(R.id.banner);
            if (banner != null) {
                // Si no hay red el banner no carga ni aunque esté vacío
                banner.setRefreshInterval(30);
                banner.setIMBannerListener(new IMBannerListener() {
                    @Override
                    public void onShowBannerScreen(IMBanner arg0) {
                        Mint.leaveBreadcrumb("MainActivity::banner onShowBannerScreen");
                    }

                    @Override
                    public void onLeaveApplication(IMBanner arg0) {
                        Mint.leaveBreadcrumb("MainActivity::banner onLeaveApplication");
                    }

                    @Override
                    public void onDismissBannerScreen(IMBanner arg0) {
                        Mint.leaveBreadcrumb("MainActivity::banner onDismissBannerScreen");
                    }

                    @Override
                    public void onBannerRequestSucceeded(IMBanner arg0) {
                        Mint.leaveBreadcrumb("MainActivity::banner onBannerRequestSucceeded");
                        bannerShown = true;
                        fixMapPadding();
                    }

                    @Override
                    public void onBannerRequestFailed(IMBanner arg0, IMErrorCode arg1) {
                        Mint.leaveBreadcrumb("MainActivity::banner onBannerRequestFailed");
                    }

                    @Override
                    public void onBannerInteraction(IMBanner arg0, Map<String, String> arg1) {
                        Mint.leaveBreadcrumb("MainActivity::banner onBannerInteraction");
                        Mint.logEvent("Ad tapped");
                    }
                });
                banner.loadBanner();
            }

            if (!isOnline(getApplicationContext())) {
                showWifiAlertDialog();
            }

            googleMap.setOnMapLongClickListener(new OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng point) {
                    Mint.leaveBreadcrumb("MainActivity::googleMap onMapLongClick");
                    calculatingDistance = true;

                    if (distanceMode == DistanceMode.DISTANCE_FROM_ANY_POINT) {
                        if (coordinates == null || coordinates.isEmpty()) {
                            toastIt(getString(R.string.toast_first_point_needed), getApplicationContext());
                        } else {
                            coordinates.add(point);
                            drawAndShowMultipleDistances(coordinates, "", false, true);
                        }
                    }
                    // Si no hemos encontrado la posición actual, no podremos
                    // calcular la distancia
                    else if (currentLocation != null) {
                        if ((distanceMode == DistanceMode.DISTANCE_FROM_CURRENT_POINT) && (coordinates.isEmpty())) {
                            coordinates.add(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                        }
                        coordinates.add(point);
                        drawAndShowMultipleDistances(coordinates, "", false, true);
                    }

                    calculatingDistance = false;
                }
            });

            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng point) {
                    Mint.leaveBreadcrumb("MainActivity::googleMap onMapClick");
                    if (distanceMode == DistanceMode.DISTANCE_FROM_ANY_POINT) {
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
                                Mint.logException(illegalStateException);
                                throw illegalStateException;
                            }
                        }
                    }
                }
            });

            // TODO Future release
            // Cambiar esto: debería modificar solamentela posición que estemos tuneando y recalcular
//			googleMap.setOnMarkerDragListener(new OnMarkerDragListener() {
////				private String selectedMarkerId;
//
//				@Override
//				public void onMarkerDragStart(Marker marker) {
////					selectedMarkerId = null;
////					final String markerId = marker.getId();
////					if (coordinates.contains(markerId)) {
////						for (int i = 0; i < coordinates.size(); i++) {
////							final LatLng position = coordinates.get(i);
////							if (markerId.latitude == position.latitude &&
////									markerId.longitude == position.longitude) {
////								selectedMarkerId = i;
////								break;
////							}
////						}
////					}
//				}
//
//				@Override
//				public void onMarkerDragEnd(Marker marker) {
////					if (selectedMarkerId != -1) {
////						coordinates.set(selectedMarkerId, marker.getPosition());
////					}
////					// NO movemos el zoom porque estamos simplemente afinando la
////					// posición
////					drawAndShowMultipleDistances(coordinates, "", false, false);
//				}
//
//				@Override
//				public void onMarkerDrag(Marker marker) {
//					// nothing
//				}
//			});

            googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    Mint.leaveBreadcrumb("MainActivity::googleMap onInfoWindowClick");
                    final Intent showInfoActivityIntent = new Intent(MainActivity.this, ShowInfoActivity.class);

                    showInfoActivityIntent.putExtra(ShowInfoActivity.POSITIONS_LIST_EXTRA_KEY_NAME,
                                                    Lists.newArrayList(coordinates));
                    showInfoActivityIntent.putExtra(ShowInfoActivity.DISTANCE_EXTRA_KEY_NAME, distanceMeasuredAsText);
                    startActivity(showInfoActivityIntent);
                }
            });

            googleMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter(this));

            // Iniciando la app
            if (currentLocation == null) {
                toastIt(getString(R.string.toast_loading_position), getApplicationContext());
            }

            handleIntents(getIntent());

            final List<String> distanceModes = Lists.newArrayList(getString(R.string.navigation_drawer_starting_point_current_position_item),
                                                                  getString(R.string.navigation_drawer_starting_point_any_position_item));
            final List<Integer> distanceIcons = Lists.newArrayList(R.drawable.ic_action_device_gps_fixed,
                                                                   R.drawable.ic_action_communication_location_on);
            drawerList = (ListView) findViewById(R.id.left_drawer);

            // TODO cambiar esto por un header como dios manda
            final LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View convertView = inflater.inflate(R.layout.simple_textview_list_item, drawerList, false);
            final TextView tvListElement = (TextView) convertView.findViewById(R.id.simple_textview);
            tvListElement.setText(getString(R.string.navigation_drawer_starting_point_header));
            tvListElement.setClickable(false);
            tvListElement.setTextColor(getResources().getColor(R.color.white));
            drawerList.addHeaderView(convertView);
            drawerList.setAdapter(new NavigationDrawerItemAdapter(this, distanceModes, distanceIcons));

            drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectItem(position);
                }
            });

            drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                                                              drawerLayout,
                                                              R.string.progressdialog_search_position_message,
                                                              R.string.progressdialog_search_position_message) {
                @Override
                public void onDrawerOpened(View drawerView) {
                    Mint.leaveBreadcrumb("MainActivity::actionBarDrawerToggle onDrawerOpened");
                    super.onDrawerOpened(drawerView);
                    supportInvalidateOptionsMenu();
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    Mint.leaveBreadcrumb("MainActivity::actionBarDrawerToggle onDrawerClosed");
                    super.onDrawerClosed(drawerView);
                    supportInvalidateOptionsMenu();
                }
            };

            drawerLayout.setDrawerListener(actionBarDrawerToggle);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

            if (savedInstanceState == null) {
                Mint.leaveBreadcrumb("MainActivity savedInstanceState == null");
                // TODO change this because the header!!!!
                selectItem(FIRST_DRAWER_ITEM_INDEX);
            }
        }
    }

    private DaoSession getApplicationDaoSession() {
        Mint.leaveBreadcrumb("MainActivity::getApplicationDaoSession");
        return ((DFMApplication) getApplicationContext()).getDaoSession();
    }

    /**
     * Swaps starting point in the main content view
     */
    private void selectItem(int position) {
        Mint.leaveBreadcrumb("MainActivity::selectItem " + position);
        if (position != 0) {
            if (position == 1) {
                Mint.logEvent("Distance from current point");
            } else if (position == 2) {
                Mint.logEvent("Distance from any point");
            }
            distanceMode = (position == FIRST_DRAWER_ITEM_INDEX) ? // TODO change this because the header!!!!
                           DistanceMode.DISTANCE_FROM_CURRENT_POINT :
                           DistanceMode.DISTANCE_FROM_ANY_POINT;

            Mint.addExtraData("distanceMode", String.valueOf(distanceMode));

            // Highlight the selected item and close the drawer
            drawerList.setItemChecked(position, true);
            drawerLayout.closeDrawer(drawerList);

            calculatingDistance = false;

            coordinates = Lists.newArrayList();
            googleMap.clear();
            if (showingElevationTask != null) {
                showingElevationTask.cancel(true);
            }
            rlElevationChart.setVisibility(View.INVISIBLE);
            elevationChartShown = false;
            fixMapPadding();
        } else {
            Mint.leaveBreadcrumb("MainActivity::selectItem 0 not valid!");
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        Mint.leaveBreadcrumb("MainActivity::onPostCreate");
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (actionBarDrawerToggle != null) {
            actionBarDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Mint.leaveBreadcrumb("MainActivity::onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        if (actionBarDrawerToggle != null) {
            actionBarDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Mint.leaveBreadcrumb("MainActivity::onNewIntent");
        setIntent(intent);
        handleIntents(intent);
    }

    /**
     * Handles all Intent types.
     *
     * @param intent The input intent.
     */
    private void handleIntents(final Intent intent) {
        Mint.leaveBreadcrumb("MainActivity::handleIntents");
        if (intent != null) {
            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                handleSearchIntent(intent);
            } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                try {
                    handleViewPositionIntent(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Mint.logException(e);
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
        Mint.leaveBreadcrumb("MainActivity::handleSearchIntent");
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
        Mint.leaveBreadcrumb("MainActivity::handleViewPositionIntent");
        final Uri uri = intent.getData();
        Mint.addExtraData("queryParameter", uri.toString());

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
                Mint.logException(noSuchFieldException);
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
                    Mint.logException(noSuchFieldException);
                    throw noSuchFieldException;
                }
            } else {
                final NoSuchFieldException noSuchFieldException = new NoSuchFieldException("Query sin parámetro q.");
                Mint.logException(noSuchFieldException);
                throw noSuchFieldException;
            }
        } else {
            final Exception exception = new Exception("Imposible tratar la query " + uri.toString());
            Mint.logException(exception);
            throw exception;
        }
    }

    private void setDestinationPosition(final Matcher matcher) {
        Mint.leaveBreadcrumb("MainActivity::setDestinationPosition");
        sendDestinationPosition = new LatLng(Double.valueOf(matcher.group(1)), Double.valueOf(matcher.group(2)));
        mustShowPositionWhenComingFromOutside = true;
    }

    private Matcher getMatcherForUri(final String schemeSpecificPart) {
        Mint.leaveBreadcrumb("MainActivity::getMatcherForUri " + schemeSpecificPart);
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
        Mint.leaveBreadcrumb("MainActivity::showWifiAlertDialog");
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
        Mint.leaveBreadcrumb("MainActivity::onCreateOptionsMenu");
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
        final List<Distance> allDistances = getApplicationDaoSession().loadAll(Distance.class);
        if (allDistances.size() == 0) {
            loadItem.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Mint.leaveBreadcrumb("MainActivity::onOptionsItemSelected");
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (actionBarDrawerToggle != null && actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case R.id.action_load:
                loadDistancesFromDB();
                return true;
            case R.id.menu_rateapp:
                showRateDialog();
                return true;
            case R.id.menu_legalnotices:
                showGooglePlayServiceLicenseDialog();
                return true;
            case R.id.menu_settings:
                openSettingsActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Mint.leaveBreadcrumb("MainActivity::onBackPressed");
        if (drawerLayout != null && drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Loads all entries stored in the database and show them to the user in a
     * dialog.
     */
    private void loadDistancesFromDB() {
        Mint.leaveBreadcrumb("MainActivity::loadDistancesFromDB");
        // TODO hacer esto en segundo plano
        final List<Distance> allDistances = getApplicationDaoSession().loadAll(Distance.class);

        if (allDistances != null && allDistances.size() > 0) {
            final DistanceAdapter distanceAdapter = new DistanceAdapter(this, allDistances);
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.dialog_load_distances_title))
                   .setAdapter(distanceAdapter, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           final Distance distance = distanceAdapter.getDistanceList().get(which);
                           final List<Position> positionList = getApplicationDaoSession().getPositionDao()
                                                                                         ._queryDistance_PositionList(distance.getId());
                           coordinates.clear();
                           coordinates.addAll(convertPositionListToLatLngList(positionList));

                           drawAndShowMultipleDistances(coordinates, distance.getName() + "\n", true, true);
                       }
                   }).create().show();
        }
    }

    private List<LatLng> convertPositionListToLatLngList(final List<Position> positionList) {
        Mint.leaveBreadcrumb("MainActivity::convertPositionListToLatLngList");
        final List<LatLng> result = Lists.newArrayList();
        for (final Position position : positionList) {
            result.add(new LatLng(position.getLatitude(), position.getLongitude()));
        }
        return result;
    }

    /**
     * Shows settings activity.
     */
    private void openSettingsActivity() {
        Mint.leaveBreadcrumb("MainActivity::openSettingsActivity");
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    /**
     * Shows rate dialog.
     */
    private void showRateDialog() {
        Mint.leaveBreadcrumb("MainActivity::showRateDialog");
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
        Mint.leaveBreadcrumb("MainActivity::openPlayStoreAppPage");
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=gc.david.dfm")));
    }

    /**
     * Opens the feedback activity.
     */
    private void openFeedbackActivity() {
        Mint.leaveBreadcrumb("MainActivity::openFeedbackActivity");
        final Intent openFeedbackActivityIntent = new Intent(MainActivity.this, FeedbackActivity.class);
        startActivity(openFeedbackActivityIntent);
    }

    /**
     * Shows an AlertDialog with the Google Play Services License.
     */
    private void showGooglePlayServiceLicenseDialog() {
        Mint.leaveBreadcrumb("MainActivity::showGooglePlayServiceLicenseDialog");
        final String LicenseInfo = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(getApplicationContext());
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
        Mint.leaveBreadcrumb("MainActivity::onStop");
        if (googleApiClient.isConnected()) {
            stopPeriodicUpdates();
        }
        super.onStop();
    }

    /**
     * Called when the Activity is restarted, even before it becomes visible.
     */
    @Override
    public void onStart() {
        Mint.leaveBreadcrumb("MainActivity::onStart");
        super.onStart();
        /*
         * Connect the client. Don't re-start any requests here; instead, wait
		 * for onResume()
		 */
        googleApiClient.connect();
    }

    /**
     * Called when the system detects that this Activity is now visible.
     */
    @SuppressLint("NewApi")
    @Override
    public void onResume() {
        Mint.leaveBreadcrumb("MainActivity::onResume");
        super.onResume();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            invalidateOptionsMenu();
        }
        checkPlayServices();
    }

    @Override
    public void onDestroy() {
        Mint.leaveBreadcrumb("MainActivity::onDestroy");
        if (showingElevationTask != null) {
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
        Mint.leaveBreadcrumb("MainActivity::onActivityResult");

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
        Mint.leaveBreadcrumb("MainActivity::checkPlayServices");
        // Comprobamos que Google Play Services está disponible en el terminal
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        // Si está disponible, devolvemos verdadero. Si no, mostramos un mensaje
        // de error y devolvemos falso
        if (resultCode == ConnectionResult.SUCCESS) {
            return true;
        } else {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                final int RQS_GooglePlayServices = 1;
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, RQS_GooglePlayServices).show();
            } else {
                // Log.i("checkPlayServices", "Dispositivo no soportado");
                finish();
            }
            return false;
        }
    }

    /**
     * Called by Location Services when the request to connect the client
     * finishes successfully. At this point, you can request the current
     * location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
        Mint.leaveBreadcrumb("MainActivity::onConnected");
        startPeriodicUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Mint.leaveBreadcrumb("MainActivity::onConnectionSuspended");
        Log.i("onConnectionSuspended", "GoogleApiClient connection has been suspended");
    }

    /**
     * Called by Location Services if the attempt to Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Mint.leaveBreadcrumb("MainActivity::onConnectionFailed");
        /*
         * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
        if (connectionResult.hasResolution()) {
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
                Mint.logException(e);
            }
        } else {
            // If no resolution is available, display a dialog to the user with
            // the error.
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    /**
     * Report location updates to the UI.
     */
    @Override
    public void onLocationChanged(Location location) {
        Mint.leaveBreadcrumb("MainActivity::onLocationChanged");
        if (currentLocation != null) {
            currentLocation.set(location);
        } else {
            currentLocation = new Location(location);
        }

        if (appHasJustStarted) {
            if (mustShowPositionWhenComingFromOutside) {
                if (currentLocation != null && sendDestinationPosition != null) {
                    new SearchPositionByCoordinates().execute(sendDestinationPosition);
                    mustShowPositionWhenComingFromOutside = false;
                }
            } else {
                final LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
                // 17 is a good zoom level for this action
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 17));
            }
            appHasJustStarted = false;
        }
    }

    /**
     * In response to a request to start updates, send a request to Location
     * Services.
     */
    private void startPeriodicUpdates() {
        Mint.leaveBreadcrumb("MainActivity::startPeriodicUpdates");
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
        // Set the interval ceiling to one minute
        locationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    /**
     * In response to a request to stop updates, send a request to Location
     * Services.
     */
    private void stopPeriodicUpdates() {
        Mint.leaveBreadcrumb("MainActivity::stopPeriodicUpdates");
        // After disconnect() is called, the client is considered "dead".
        googleApiClient.disconnect();
    }

    /**
     * Shows a dialog returned by Google Play services for the connection error
     * code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
    private void showErrorDialog(final int errorCode) {
        Mint.leaveBreadcrumb("MainActivity::showErrorDialog");

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
        Mint.leaveBreadcrumb("MainActivity::drawAndShowMultipleDistances");
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
        if (getSharedPreferences(getBaseContext()).getBoolean("elevation_chart", false) &&
            isOnline(getApplicationContext())) {
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
        Mint.leaveBreadcrumb("MainActivity::addMarkers");
        for (int i = 0; i < coordinates.size(); i++) {
            if ((i == 0 && (isLoadingFromDB || distanceMode == DistanceMode.DISTANCE_FROM_ANY_POINT)) ||
                (i == coordinates.size() - 1)) {
                final LatLng coordinate = coordinates.get(i);
                final Marker marker = addMarker(coordinate);
                // TODO Release 1.5
//			    marker.setDraggable(true);
                if (i == coordinates.size() - 1) {
                    marker.setTitle(message + distance);
                    marker.showInfoWindow();
                }
            }
        }
    }

    private Marker addMarker(final LatLng coordinate) {
        Mint.leaveBreadcrumb("MainActivity::addMarker");
        return googleMap.addMarker(new MarkerOptions().position(coordinate));
    }

    private void addLines(final List<LatLng> coordinates, final boolean isLoadingFromDB) {
        Mint.leaveBreadcrumb("MainActivity::addLines");
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
        Mint.leaveBreadcrumb("MainActivity::addLine");
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
        Mint.leaveBreadcrumb("MainActivity::calculateDistance");
        double distanceInMetres = 0.0;
        for (int i = 0; i < coordinates.size() - 1; i++) {
            distanceInMetres += Haversine.getDistance(coordinates.get(i).latitude,
                                                      coordinates.get(i).longitude,
                                                      coordinates.get(i + 1).latitude,
                                                      coordinates.get(i + 1).longitude);
        }

        Locale defaultLocale = getResources().getConfiguration().locale;

        final SharedPreferences preferences = getSharedPreferences("gc.david.dfm_preferences", Context.MODE_PRIVATE);
        final String defaultUnit = preferences.getString("unit", null);
        if (defaultUnit != null) {
            if (getString(R.string.preference_unit_entry_value_US).equals(defaultUnit)) {
                defaultLocale = Locale.US;
            } else { // EU
                defaultLocale = Locale.FRANCE;
            }
        }
        return Haversine.normalizeDistance(distanceInMetres, defaultLocale);
    }

    /**
     * Moves camera position and applies zoom if needed.
     *
     * @param p1 Start position.
     * @param p2 Destination position.
     */
    private void moveCameraZoom(final LatLng p1, final LatLng p2, final boolean mustApplyZoomIfNeeded) {
        Mint.leaveBreadcrumb("MainActivity::moveCameraZoom");
        double centerLat = 0.0;
        double centerLon = 0.0;

        // Diferenciamos según preferencias
        final String centre = getSharedPreferences(getBaseContext()).getString("animation", "CEN");
        if (centre.equals("CEN")) {
            centerLat = (p1.latitude + p2.latitude) / 2;
            centerLon = (p1.longitude + p2.longitude) / 2;
        } else if (centre.equals("DES")) {
            centerLat = p2.latitude;
            centerLon = p2.longitude;
        } else if (centre.equals("NO")) {
            return;
        }

        if (mustApplyZoomIfNeeded) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(centerLat, centerLon),
                                                                      calculateZoom(p1, p2)));
        } else {
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(p2.latitude, p2.longitude)));
        }
    }

    private SharedPreferences getSharedPreferences(final Context context) {
        Mint.leaveBreadcrumb("MainActivity::getSharedPreferences");
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Calculates zoom level to make possible current and destination positions
     * appear in the device.
     *
     * @param origin      Current position.
     * @param destination Destination position.
     * @return Zoom level.
     */
    private float calculateZoom(final LatLng origin, final LatLng destination) {
        Mint.leaveBreadcrumb("MainActivity::calculateZoom");
        double distanceInMetres = Haversine.getDistance(origin.latitude,
                                                        origin.longitude,
                                                        destination.latitude,
                                                        destination.longitude);
        double kms = distanceInMetres / 1000;

        if (kms > 2700) {
            return 3;
        } else if (kms > 1300) {
            return 4;
        } else if (kms > 650) {
            return 5;
        } else if (kms > 325) {
            return 6;
        } else if (kms > 160) {
            return 7;
        } else if (kms > 80) {
            return 8;
        } else if (kms > 40) {
            return 9;
        } else if (kms > 20) {
            return 10;
        } else if (kms > 10) {
            return 11;
        } else if (kms > 5) {
            return 12;
        } else if (kms > 2.5) {
            return 13;
        } else if (kms > 1.25) {
            return 14;
        } else if (kms > 0.6) {
            return 15;
        } else if (kms > 0.3) {
            return 16;
        } else if (kms > 0.15) {
            return 17;
        }
        return 18;
    }

    /**
     * Calculates elevation points in background and shows elevation chart.
     *
     * @param coordinates Positions list.
     */
    private void getElevation(final List<LatLng> coordinates) {
        Mint.leaveBreadcrumb("MainActivity::getElevation");
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
            Mint.logException(illegalStateException);
            throw illegalStateException;
        }

        if (showingElevationTask != null) {
            showingElevationTask.cancel(true);
        }
        showingElevationTask = new GetAltitude().execute(positionListUrlParameter);
    }

    /**
     * Sets map attending to the action which is performed.
     */
    private void fixMapPadding() {
        Mint.leaveBreadcrumb("MainActivity::fixMapPadding");
        if (bannerShown) {
            if (elevationChartShown) {
                googleMap.setPadding(0, rlElevationChart.getHeight(), 0, banner.getLayoutParams().height);
            } else {
                googleMap.setPadding(0, 0, 0, banner.getLayoutParams().height);
            }
        } else {
            if (elevationChartShown) {
                googleMap.setPadding(0, rlElevationChart.getHeight(), 0, 0);
            } else {
                googleMap.setPadding(0, 0, 0, 0);
            }
        }
    }

    private static enum DistanceMode {
        DISTANCE_FROM_CURRENT_POINT,
        DISTANCE_FROM_ANY_POINT
    }

    /**
     * A subclass of AsyncTask that calls getFromLocationName() in the background.
     */
    private class SearchPositionByName extends AsyncTask<Object, Void, Integer> {

        protected List<Address>  addressList;
        protected StringBuilder  fullAddress;
        protected LatLng         selectedPosition;
        protected ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            Mint.leaveBreadcrumb("SearchPositionByName::onPreExecute");
            addressList = null;
            fullAddress = new StringBuilder();
            selectedPosition = null;

            // Comprobamos que haya conexión con internet (WiFi o Datos)
            if (!isOnline(getApplicationContext())) {
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
            Mint.leaveBreadcrumb("SearchPositionByName::doInBackground");
            /* get latitude and longitude from the addressList */
            final Geocoder geoCoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                addressList = geoCoder.getFromLocationName((String) params[0], 5);
            } catch (IOException e) {
                e.printStackTrace();
                Mint.logException(e);
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
            Mint.leaveBreadcrumb("SearchPositionByName::onPostExecute");
            switch (result) {
                case 0:
                    if (addressList != null && addressList.size() > 0) {
                        // Si hay varios, elegimos uno. Si solo hay uno, mostramos ese
                        if (addressList.size() == 1) {
                            processSelectedAddress(0);
                            handleSelectedAddress();
                        } else {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle(getString(R.string.dialog_select_address_title));
                            builder.setItems(groupAddresses(addressList).toArray(new String[addressList.size()]),
                                             new DialogInterface.OnClickListener() {
                                                 public void onClick(DialogInterface dialog, int item) {
                                                     processSelectedAddress(item);
                                                     handleSelectedAddress();
                                                 }
                                             });
                            builder.create().show();
                        }
                    }
                    break;
                case -1:
                    toastIt(getString(R.string.toast_no_find_address), getApplicationContext());
                    break;
                case -2:
                    toastIt(getString(R.string.toast_no_results), getApplicationContext());
                    break;
                case -3:
                    toastIt(getString(R.string.toast_no_find_address), getApplicationContext());
                    break;
            }
            progressDialog.dismiss();
            if (searchMenuItem != null) {
                MenuItemCompat.collapseActionView(searchMenuItem);
            }
        }

        private void handleSelectedAddress() {
            Mint.leaveBreadcrumb("SearchPositionByName::handleSelectedAddress");
            if (distanceMode == DistanceMode.DISTANCE_FROM_ANY_POINT) {
                coordinates.add(selectedPosition);
                if (coordinates.isEmpty()) {
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
                    if (coordinates == null || coordinates.isEmpty()) {
                        coordinates = Lists.newArrayList();
                        coordinates.add(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                    }
                    coordinates.add(selectedPosition);
                    drawAndShowMultipleDistances(coordinates, fullAddress.toString(), false, true);
                } else {
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
            Mint.leaveBreadcrumb("SearchPositionByName::processSelectedAddress");
            // Fill address info to show in the marker info window
            final Address address = addressList.get(item);
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                fullAddress.append(address.getAddressLine(i)).append("\n");
            }
            selectedPosition = new LatLng(address.getLatitude(), address.getLongitude());
        }

        /**
         * Extract a list of address from a list of Address objects.
         *
         * @param addressList An Address's list.
         * @return A string list with only addresses in text.
         */
        protected List<String> groupAddresses(final List<Address> addressList) {
            Mint.leaveBreadcrumb("SearchPositionByName::groupAddresses");
            final List<String> result = Lists.newArrayList();
            StringBuilder stringBuilder;
            for (final Address l : addressList) {
                stringBuilder = new StringBuilder();
                for (int j = 0; j < l.getMaxAddressLineIndex() + 1; j++) {
                    stringBuilder.append(l.getAddressLine(j)).append("\n");
                }
                result.add(stringBuilder.toString());
            }
            return result;
        }
    }

    /**
     * A subclass of SearchPositionByName to get position by coordinates.
     */
    private class SearchPositionByCoordinates extends SearchPositionByName {
        @Override
        protected Integer doInBackground(Object... params) {
            Mint.leaveBreadcrumb("SearchPositionByCoordinates::doInBackground");
            /* get latitude and longitude from the addressList */
            final Geocoder geoCoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            final LatLng latLng = (LatLng) params[0];
            try {
                addressList = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            } catch (final IOException e) {
                e.printStackTrace();
                Mint.logException(e);
                return -1; // No encuentra una dirección, no puede conectar con el servidor
            } catch (final IllegalArgumentException e) {
                final IllegalArgumentException illegalArgumentException = new IllegalArgumentException(String.format("Error en latitud=%f o longitud=%f.\n%s",
                                                                                                                     latLng.latitude,
                                                                                                                     latLng.longitude,
                                                                                                                     e.toString()));
                Mint.logException(illegalArgumentException);
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
            Mint.leaveBreadcrumb("SearchPositionByCoordinates::onPostExecute");
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
                    toastIt(getString(R.string.toast_no_find_address), getApplicationContext());
                    break;
                case -2:
                    toastIt(getString(R.string.toast_no_results), getApplicationContext());
                    break;
                case -3:
                    toastIt(getString(R.string.toast_no_find_address), getApplicationContext());
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

        private HttpClient httpClient = null;
        private HttpGet    httpGet    = null;
        private HttpResponse httpResponse;
        private String       responseAsString;
        private InputStream inputStream = null;
        private JSONObject responseJSON;

        @Override
        protected void onPreExecute() {
            Mint.leaveBreadcrumb("GetAltitude::onPreExecute");
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
            Mint.leaveBreadcrumb("GetAltitude::doInBackground");
            httpGet = new HttpGet("http://maps.googleapis.com/maps/api/elevation/json?sensor=true"
                                  + "&path=" + Uri.encode(params[0])
                                  + "&samples=" + ELEVATION_SAMPLES);
            httpGet.setHeader("content-type", "application/json");
            try {
                httpResponse = httpClient.execute(httpGet);
                inputStream = httpResponse.getEntity().getContent();
                if (inputStream != null) {
                    responseAsString = convertInputStreamToString(inputStream);
                    responseJSON = new JSONObject(responseAsString);
                    if (responseJSON.get("status").equals("OK")) {
                        buildElevationChart(responseJSON.getJSONArray("results"));
                    }
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                Mint.logException(e);
            } catch (IllegalStateException e) {
                e.printStackTrace();
                Mint.logException(e);
            } catch (IOException e) {
                e.printStackTrace();
                Mint.logException(e);
            } catch (JSONException e) {
                e.printStackTrace();
                Mint.logException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Double result) {
            Mint.leaveBreadcrumb("GetAltitude::onPostExecute");
            showElevationProfileChart();
            // When HttpClient instance is no longer needed
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpClient.getConnectionManager().shutdown();
        }

        /**
         * Converts the InputStream with the retrieved data to String.
         *
         * @param inputStream The input stream.
         * @return The InputStream converted to String.
         * @throws IOException
         */
        private String convertInputStreamToString(final InputStream inputStream) throws IOException {
            Mint.leaveBreadcrumb("GetAltitude::convertInputStreamToString");
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }

            inputStream.close();
            bufferedReader.close();
            return result.toString();
        }

        /**
         * Builds the information about the elevation profile chart. Use this in
         * a background task.
         *
         * @param array JSON array with the response data.
         * @throws JSONException
         */
        private void buildElevationChart(final JSONArray array) throws JSONException {
            Mint.leaveBreadcrumb("GetAltitude::buildElevationChart");
            // Creates the serie and adds data to it
            final GraphViewSeries series =
                    new GraphViewSeries(null,
                                        new GraphViewSeriesStyle(getResources().getColor(R.color.elevation_chart_line),
                                                                 (int) (3 * DEVICE_DENSITY)),
                                        new GraphView.GraphViewData[]{});

            Locale defaultLocale = Locale.getDefault();

            final SharedPreferences preferences = getSharedPreferences("gc.david.dfm_preferences", Context.MODE_PRIVATE);
            final String defaultUnit = preferences.getString("unit", null);
            if (defaultUnit != null) {
                if (getString(R.string.preference_unit_entry_value_US).equals(defaultUnit)) {
                    defaultLocale = Locale.US;
                } else { // EU
                    defaultLocale = Locale.FRANCE;
                }
            }

            for (int w = 0; w < array.length(); w++) {
                series.appendData(new GraphView.GraphViewData(w,
                                                              Haversine.normalizeAltitudeByLocale(Double.valueOf(array.getJSONObject(w)
                                                                                                                      .get("elevation")
                                                                                                                      .toString()),
                                                                                                  defaultLocale)),
                                  false,
                                  array.length());
            }

            // Creates the line and add it to the chart
            graphView = new LineGraphView(getApplicationContext(),
                                          getString(R.string.elevation_chart_title,
                                                    Haversine.getAltitudeUnitByLocale(defaultLocale)));
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
            Mint.leaveBreadcrumb("GetAltitude::showElevationProfileChart");
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
}
