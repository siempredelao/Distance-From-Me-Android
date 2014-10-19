package gc.david.dfm;

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
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.inmobi.commons.InMobi;
import com.inmobi.monetization.IMBanner;
import com.inmobi.monetization.IMBannerListener;
import com.inmobi.monetization.IMErrorCode;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import gc.david.dfm.db.Distance;
import gc.david.dfm.db.DistancesDataSource;
import gc.david.dfm.map.Haversine;
import gc.david.dfm.map.LocationUtils;
import gc.david.dfm.map.MarkerInfoWindowAdapter;

import static gc.david.dfm.Utils.isOnline;
import static gc.david.dfm.Utils.showAlertDialog;
import static gc.david.dfm.Utils.toastIt;

/**
 * Implements the app main Activity.
 *
 * @author David
 *
 */
public class MainActivity extends ActionBarActivity implements
		LocationListener, GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private GoogleMap googleMap								= null;
	// A request to connect to Location Services
	private LocationRequest locationRequest 				= null;
	// Stores the current instantiation of the location client in this object
	private LocationClient locationClient					= null;
	// Current position
	private Location currentLocation 						= null;
	private Polyline polyline								= null;
	private final int RQS_GooglePlayServices				= 1;
	// Moves to current position if app has just started
	private boolean appHasJustStarted						= true;
	// Distinguish between position polishing and other position searching
	private boolean mustApplyZoom							= true;
	private String distanceMeasuredAsText					= "";
	private LatLng selectedPosition							= null;
	// Address returned at string searching
	private StringBuilder fullAddress = null;
	private MenuItem searchMenuItem							= null;
	// Show position if we come from other app (p.e. Whatsapp)
	private boolean mustShowPositionWhenComingFromOutside 	= false;
	private String sendDestinationPosition					= "";
	// To change line color when we choose a distanceMeasuredAsText from database
	private boolean loadingDistance							= false;
	private IMBanner banner									= null;
	// Google Map items padding
	private boolean bannerShown								= false;
	private boolean elevationChartShown						= false;
	private static final int ELEVATION_SAMPLES				= 100;
	@SuppressWarnings("rawtypes")
	private AsyncTask showingElevationTask					= null;
	private GraphView graphView								= null;
	private float DEVICE_DENSITY;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		DEVICE_DENSITY = getResources().getDisplayMetrics().density;

		final SupportMapFragment fragment = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map));
		googleMap = fragment.getMap();

		if (googleMap != null) {
			googleMap.setMyLocationEnabled(true);
			googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

			// InMobi Ads
			InMobi.initialize(this, "9b61f509a1454023b5295d8aea4482c2");
			banner = (IMBanner) findViewById(R.id.banner);
			if (banner != null){
				// Si no hay red el banner no carga ni aunque esté vacío
				banner.setRefreshInterval(30);
				banner.setIMBannerListener(new IMBannerListener() {
					@Override
					public void onShowBannerScreen(IMBanner arg0) {
					}
					@Override
					public void onLeaveApplication(IMBanner arg0) {
					}
					@Override
					public void onDismissBannerScreen(IMBanner arg0) {
					}
					@Override
					public void onBannerRequestSucceeded(IMBanner arg0) {
						bannerShown = true;
						fixMapPadding();
					}
					@Override
					public void onBannerRequestFailed(IMBanner arg0, IMErrorCode arg1) {
					}
					@Override
					public void onBannerInteraction(IMBanner arg0, Map<String, String> arg1) {
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
					// Si no hemos encontrado la posición actual, no podremos
					// calcular la distancia
					if (currentLocation != null) {
						showDistanceOnMap(new LatLng(currentLocation.getLatitude(),
								currentLocation.getLongitude()), point, "");
					}
				}
			});

			googleMap.setOnMarkerDragListener(new OnMarkerDragListener() {
				@Override
				public void onMarkerDragStart(Marker marker) {
					// nothing
				}

				@Override
				public void onMarkerDragEnd(Marker marker) {
					// NO movemos el zoom porque estamos simplemente afinando la
					// posición
					mustApplyZoom = false;
					showDistanceOnMap(
							new LatLng(currentLocation.getLatitude(), currentLocation
									.getLongitude()),
							new LatLng(marker.getPosition().latitude, marker
									.getPosition().longitude), "");
					mustApplyZoom = true;
				}

				@Override
				public void onMarkerDrag(Marker marker) {
					// nothing
				}
			});

			googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
				@Override
				public void onInfoWindowClick(Marker marker) {
					if (currentLocation != null) {
						final Intent showInfoActivityIntent = new Intent(MainActivity.this, ShowInfoActivity.class);
						showInfoActivityIntent.putExtra(ShowInfoActivity.originExtraKeyName, new LatLng(currentLocation.getLatitude(),
								currentLocation.getLongitude()));
						showInfoActivityIntent.putExtra(ShowInfoActivity.destinationExtraKeyName, new LatLng(
								marker.getPosition().latitude,
								marker.getPosition().longitude));
						showInfoActivityIntent.putExtra(ShowInfoActivity.distanceExtraKeyName, distanceMeasuredAsText);
						startActivity(showInfoActivityIntent);
					} else {
						toastIt(getText(R.string.loading), getApplicationContext());
					}
				}
			});

			googleMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter(this));

			// Iniciando la app
			if (currentLocation == null) {
				toastIt(getText(R.string.loading), getApplicationContext());
			}

			handleIntents(getIntent());
		}

		// Create a new global location parameters object
		locationRequest = LocationRequest.create();
		// Set the update interval
		locationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
		// Use high accuracy
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the interval ceiling to one minute
		locationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

		// Create a new location client, using the enclosing class to handle
		// callbacks
		locationClient = new LocationClient(this, this, this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntents(intent);
	}

	/**
	 * Handles all Intent types.
	 *
	 * @param intent
	 *            The input intent.
	 */
	private void handleIntents(final Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
			handleSearchIntent(intent);
		} else if (intent.getAction().equals(Intent.ACTION_VIEW)) {
			handleSendPositionIntent(intent);
		}
	}

	/**
	 * Handles a search intent.
	 *
	 * @param intent
	 *            Input intent.
	 */
	private void handleSearchIntent(final Intent intent) {
		// Para controlar instancias únicas, no queremos que cada vez que
		// busquemos nos inicie una nueva instancia de la aplicación
		final String query = intent.getStringExtra(SearchManager.QUERY);
		if (currentLocation != null) {
			new SearchPosition().execute(query);
		}
		MenuItemCompat.collapseActionView(searchMenuItem);
	}

	/**
	 * Handles a send intent with position data.
	 *
	 * @param intent
	 *            Input intent with position data.
	 *
	 */
	private void handleSendPositionIntent(final Intent intent) {
		final Uri uri = intent.getData();

		// Buscamos el envío por Whatsapp
		final String queryParameter = uri.getQueryParameter("q"); // loc:latitud,longitud (You)
		if (queryParameter != null) {
			// http://www.regexplanet.com/advanced/java/index.html
//			final String regex = "(\\-?\\d+\\.\\d+),(\\-?\\d+\\.\\d+)";
//			final Pattern pattern = Pattern.compile(regex);
//			final Matcher matcher = pattern.matcher(queryParameter);
//			if (matcher.find()) {
//				try {
//					sendDestinationPosition = new LatLng(Double.valueOf(matcher.group(1)), Double.valueOf(matcher.group(2)));
//				} catch (Exception e) {
//					System.out.println("Error al obtener las coordenadas. Matcher = " + matcher.toString());
//					e.printStackTrace();
//				}
//			}

			// Esta string será la que mandemos a BuscaPosicion
			sendDestinationPosition = queryParameter.replace("loc:", "")
					.replaceAll(" (\\D*)", ""); // latitud,longitud

			mustShowPositionWhenComingFromOutside = true;
		}
	}

	/**
	 * A subclass of AsyncTask that calls getFromLocationName() in the
	 * background.
	 */
	private class SearchPosition extends AsyncTask<String, Void, Integer> {

		private ProgressDialog progressDialog;
		private List<Address> addressList;

		@Override
		protected void onPreExecute() {
			addressList = null;
			fullAddress = new StringBuilder();
			selectedPosition = null;
			progressDialog = new ProgressDialog(MainActivity.this);
			progressDialog.setTitle(R.string.searching);
			progressDialog.setMessage(getText(R.string.wait));
			progressDialog.setCancelable(false);
			progressDialog.setIndeterminate(true);
			progressDialog.show();

			// Comprobamos que haya conexión con internet (WiFi o Datos)
			if (!isOnline(getApplicationContext())) {
				if (progressDialog != null) {
					progressDialog.dismiss();
				}

				showWifiAlertDialog();

				// Restauramos el menú y que vuelva a empezar de nuevo
				MenuItemCompat.collapseActionView(searchMenuItem);
				cancel(false);
			}

			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(String... params) {
			/* get latitude and longitude from the addressList */
			final Geocoder geoCoder = new Geocoder(getApplicationContext(), Locale.getDefault());
			try {
				addressList = geoCoder.getFromLocationName(params[0], 5);
			} catch (IOException e) {
				e.printStackTrace();
				return -1; // No encuentra una dirección, no puede conectar con el servidor
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
			if (result == 0) {
				if (addressList != null && addressList.size() > 0) {
					// Si hay varios, elegimos uno. Si solo hay uno, mostramos ese
					if (addressList.size() > 1) {
						final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
						builder.setTitle(getText(R.string.select_address));
						builder.setItems(
								groupAdresses(addressList)
										.toArray(new String[addressList.size()]),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int item) {
										processSelectedAddress(item);

										if (selectedPosition != null) {
											showDistanceOnMap(
													new LatLng(
															currentLocation.getLatitude(),
															currentLocation.getLongitude()),
													selectedPosition,
													fullAddress.toString());
										}
									}
								});
						builder.create().show();
					} else {
						processSelectedAddress(0);
					}
				}
			} else if (result == -1) {
				toastIt(getText(R.string.nofindaddress), getApplicationContext());
			} else if (result == -2) {
				toastIt(getText(R.string.noresults), getApplicationContext());
			} else if (result == -3) {
				toastIt(getText(R.string.nofindaddress), getApplicationContext());
			}

			if (progressDialog != null) {
				progressDialog.dismiss();
			}

			if (selectedPosition != null) {
				showDistanceOnMap(new LatLng(currentLocation.getLatitude(),
								currentLocation.getLongitude()),
						selectedPosition,
						fullAddress.toString());
			}

			MenuItemCompat.collapseActionView(searchMenuItem);
			super.onPostExecute(result);
		}

		/**
		 * Processes the address selected by the user and sets the new destination
		 * position.
		 *
		 * @param item
		 *            The item index in the AlertDialog.
		 */
		private void processSelectedAddress(final int item) {
			// esto para el marcador!
			for (int i = 0; i <= addressList.get(item).getMaxAddressLineIndex(); i++) {
				fullAddress.append(addressList.get(item).getAddressLine(i)).append("\n");
			}

			selectedPosition = new LatLng(addressList.get(item).getLatitude(),
					addressList.get(item).getLongitude());
		}

		/**
		 * Extract a list of address from a list of Address objects.
		 *
		 * @param addressList
		 *            An Address's list.
		 * @return A string list with only addresses in text.
		 */
		private List<String> groupAdresses(final List<Address> addressList) {
			final List<String> result = new ArrayList<String>();
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
	 * Shows the wireless centralized settings in API<11, otherwise shows general settings
	 */
	private void showWifiAlertDialog() {
		showAlertDialog(
				(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB ?
						android.provider.Settings.ACTION_WIRELESS_SETTINGS :
						android.provider.Settings.ACTION_SETTINGS),
				getText(R.string.no_connection),
				getText(R.string.wireless_off),
				getText(R.string.wireless_enable),
				getText(R.string.do_nothing),
				MainActivity.this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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
		final DistancesDataSource distancesDataSource = new DistancesDataSource(getApplicationContext());
		distancesDataSource.open();
		if (distancesDataSource != null) {
			if (distancesDataSource.getAllDistances() == null) {
				loadItem.setVisible(false);
			}
			distancesDataSource.close();
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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

	/**
	 * Loads all entries stored in the database and show them to the user in a
	 * dialog.
	 */
	private void loadDistancesFromDB() {
		// TODO hacer esto en segundo plano
		final DistancesDataSource dds = new DistancesDataSource(getApplicationContext());
		dds.open();
		final ArrayList<Distance> allDistances = dds.getAllDistances();
		dds.close();

		if (allDistances != null && allDistances.size() > 0) {
			final DistanceAdapter distanceAdapter = new DistanceAdapter(this, allDistances);
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getText(R.string.list_dialog_title).toString())
					.setAdapter(distanceAdapter, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							loadingDistance = true;
							final Distance distance = distanceAdapter.getDistanceList().get(which);
							final LatLng originLatLng = new LatLng(distance.getLat_a(),
									distance.getLon_a());
							final LatLng destinationLatLng = new LatLng(distance.getLat_b(),
									distance.getLon_b());

							showDistanceOnMap(originLatLng, destinationLatLng, distance.getName()
									+ "\n");
						}
					}).create().show();
		} else {
			toastIt(getText(R.string.no_distances_registered), getApplicationContext());
		}
	}

	/**
	 * Shows settings activity.
	 */
	private void openSettingsActivity(){
		startActivity(new Intent(this, SettingsActivity.class));
	}

	/**
	 * Shows rate dialog.
	 */
	private void showRateDialog(){
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.rate_title)
		.setMessage(R.string.rate_message)
		.setPositiveButton(getText(R.string.rate_positive_button), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				openPlayStoreAppPage();
			}
		})
		.setNegativeButton(getText(R.string.rate_negative_button), new DialogInterface.OnClickListener() {
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
	private void openPlayStoreAppPage(){
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=gc.david.dfm")));
	}

	/**
	 * Opens the feedback activity.
	 */
	private void openFeedbackActivity(){
		final Intent openFeedbackActivityIntent = new Intent(MainActivity.this, FeedbackActivity.class);
		startActivity(openFeedbackActivityIntent);
	}

	/**
	 * Shows an AlertDialog with the Google Play Services License.
	 */
	private void showGooglePlayServiceLicenseDialog() {
		final String LicenseInfo = GooglePlayServicesUtil
				.getOpenSourceSoftwareLicenseInfo(getApplicationContext());
		final AlertDialog.Builder LicenseDialog = new AlertDialog.Builder(MainActivity.this);
		LicenseDialog.setTitle(R.string.menu_legalnotices);
		LicenseDialog.setMessage(LicenseInfo);
		LicenseDialog.show();
	}

	/*
	 * Called when the Activity is no longer visible at all. Stop updates and
	 * disconnect.
	 */
	@Override
	public void onStop() {
		// If the client is connected
		if (locationClient.isConnected()) {
			stopPeriodicUpdates();
		}

		// After disconnect() is called, the client is considered "dead".
		locationClient.disconnect();

		super.onStop();
	}

	/*
	 * Called when the Activity is restarted, even before it becomes visible.
	 */
	@Override
	public void onStart() {
		super.onStart();
		/*
		 * Connect the client. Don't re-start any requests here; instead, wait
		 * for onResume()
		 */
		locationClient.connect();
	}

	/*
	 * Called when the system detects that this Activity is now visible.
	 */
	@SuppressLint("NewApi")
	@Override
	public void onResume() {
		super.onResume();
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			invalidateOptionsMenu();
		}
		checkPlayServices();
	}

	@Override
	public void onDestroy() {
		if (showingElevationTask != null) {
			showingElevationTask.cancel(true);
		}
		super.onDestroy();
	}

	/*
	 * Handle results returned to this Activity by other Activities started with
	 * startActivityForResult(). In particular, the method onConnectionFailed()
	 * in LocationUpdateRemover and LocationUpdateRequester may call
	 * startResolutionForResult() to start an Activity that handles Google Play
	 * services problems. The result of this call returns here, to
	 * onActivityResult.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

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
	 *         otherwise.
	 */
	private boolean checkPlayServices() {
		// Comprobamos que Google Play Services está disponible en el terminal
		final int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getApplicationContext());

		// Si está disponible, devolvemos verdadero. Si no, mostramos un mensaje
		// de error y devolvemos falso
		if (resultCode == ConnectionResult.SUCCESS) {
			return true;
		} else {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this, RQS_GooglePlayServices)
						.show();
			} else {
				// Log.i("checkPlayServices", "Dispositivo no soportado");
				finish();
			}
			return false;
		}
	}

	/*
	 * Called by Location Services when the request to connect the client
	 * finishes successfully. At this point, you can request the current
	 * location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle bundle) {
		startPeriodicUpdates();
	}

	/*
	 * Called by Location Services if the connection to the location client
	 * drops because of an error.
	 */
	@Override
	public void onDisconnected() {
	}

	/*
	 * Called by Location Services if the attempt to Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
						LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services cancelled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
			// If no resolution is available, display a dialog to the user with
			// the error.
			showErrorDialog(connectionResult.getErrorCode());
		}
	}

	/*
	 * Report location updates to the UI.
	 */
	@Override
	public void onLocationChanged(Location location) {
		if (currentLocation != null) {
			currentLocation.set(location);
		} else {
			currentLocation = new Location(location);
		}

		if (appHasJustStarted) {
			if (mustShowPositionWhenComingFromOutside) {
				if (currentLocation != null) {
					new SearchPosition().execute(sendDestinationPosition);
				}
				mustShowPositionWhenComingFromOutside = false;
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
		locationClient.requestLocationUpdates(locationRequest, this);
	}

	/**
	 * In response to a request to stop updates, send a request to Location
	 * Services.
	 */
	private void stopPeriodicUpdates() {
		locationClient.removeLocationUpdates(this);
	}

	/**
	 * Shows a dialog returned by Google Play services for the connection error
	 * code
	 *
	 * @param errorCode
	 *            An error code returned from onConnectionFailed
	 */
	private void showErrorDialog(final int errorCode) {

		// Get the error dialog from Google Play services
		final Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
				this, LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

		// If Google Play services can provide an error dialog
		if (errorDialog != null) {
			// Create a new DialogFragment in which to show the error dialog
			final ErrorDialogFragment errorFragment = new ErrorDialogFragment();

			// Set the dialog in the DialogFragment
			errorFragment.setDialog(errorDialog);

			// Show the error dialog in the DialogFragment
			errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
		}
	}

	/**
	 * Calculates the distanceMeasuredAsText to a specified position, adds the marker and the
	 * line.
	 *
	 * @param start
	 *            Start position.
	 * @param end
	 *            Destination position.
	 * @param message
	 *            Address to show in the info window (if needed).
	 */
	private void showDistanceOnMap(final LatLng start, final LatLng end, final String message) {
		// Borramos los antiguos marcadores y lineas
		googleMap.clear();

		// Calculamos la distancia
		distanceMeasuredAsText = calculateDistance(start, end);

		// Añadimos el nuevo marcador
		addMarker(end, distanceMeasuredAsText, message);

		// Añadimos la línea
		addLine(start, end);

		// Aquí hacer la animación de la cámara
		moveCameraZoom(start, end);

		// Muestra el perfil de elevación si está en las preferencias
		// y si está conectado a internet
		if (getSharedPreferences(getBaseContext()).getBoolean("elevation_chart", false)
				&& isOnline(getApplicationContext())) {
			getElevation(start, end);
		}
	}

	/**
	 * Adds a marker to the map in a specified position and shows its info
	 * window.
	 *
	 * @param point
	 *            Destination position.
	 * @param distance
	 *            Distance to destination.
	 * @param message
	 *            Destination address (if needed).
	 */
	private void addMarker(final LatLng point, final String distance, final String message) {
		final Marker marker = googleMap.addMarker(new MarkerOptions().position(point)
				.title(message + distance));
		marker.setDraggable(true);
		marker.showInfoWindow();
	}

	/**
	 * Adds a line between start and end positions.
	 *
	 * @param start
	 *            Start position.
	 * @param end
	 *            Destination position.
	 */
	private void addLine(final LatLng start, final LatLng end) {
		if (polyline != null) {
			polyline.remove();
			polyline = null;
		}
		final PolylineOptions lineOptions = new PolylineOptions().add(start).add(end);
		lineOptions.width(3*getResources().getDisplayMetrics().density);
		if (loadingDistance) {
			loadingDistance = false;
			lineOptions.color(Color.YELLOW);
		} else
			lineOptions.color(Color.GREEN);
		polyline = googleMap.addPolyline(lineOptions);
	}

	/**
	 * Returns the distance between start and end positions normalized by device
	 * locale.
	 *
	 * @param start
	 *            Start position.
	 * @param end
	 *            Destination position.
	 * @return The normalized distance.
	 */
	private String calculateDistance(final LatLng start, final LatLng end) {
		double distanceInMetres = Haversine.getDistance(start.latitude,
				start.longitude,
				end.latitude,
				end.longitude);

		return Haversine.normalizeDistance(distanceInMetres, getResources().getConfiguration().locale);
	}

	/**
	 * Moves camera position and applies zoom if needed.
	 *
	 * @param p1
	 *            Start position.
	 * @param p2
	 *            Destination position.
	 */
	private void moveCameraZoom(final LatLng p1, final LatLng p2) {
		double centerLat = 0.0;
		double centerLon = 0.0;

		// Diferenciamos según preferencias
		final String centre = getSharedPreferences(getBaseContext()).getString("centre", "CEN");
		if (centre.equals("CEN")) {
			centerLat = (p1.latitude + p2.latitude) / 2;
			centerLon = (p1.longitude + p2.longitude) / 2;
		} else if (centre.equals("DES")) {
			centerLat = p2.latitude;
			centerLon = p2.longitude;
		}

		if (mustApplyZoom) {
			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					centerLat, centerLon), calculateZoom(p1, p2)));
		} else {
			googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(
					p2.latitude, p2.longitude)));
		}
	}

	private SharedPreferences getSharedPreferences(final Context context){
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	/**
	 * Calculates zoom level to make possible current and destination positions
	 * appear in the device.
	 *
	 * @param origin
	 *            Current position.
	 * @param destination
	 *            Destination position.
	 * @return Zoom level.
	 */
	private float calculateZoom(final LatLng origin, final LatLng destination) {
		double distanceInMetres = Haversine.getDistance(origin.latitude, origin.longitude,
				destination.latitude, destination.longitude);
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
	 * @param start
	 *            Start position.
	 * @param end
	 *            Destination position.
	 */
	private void getElevation(final LatLng start, final LatLng end) {
		final String startPos = 	String.valueOf(start.latitude) +
							"," +
							String.valueOf(start.longitude);
		final String endPos = String.valueOf(end.latitude) +
						"," +
						String.valueOf(end.longitude);

		if (showingElevationTask != null) {
			showingElevationTask.cancel(true);
		}
		showingElevationTask = new GetAltitude().execute(startPos, endPos);
	}

	/**
	 * A subclass of AsyncTask that gets elevation points from coordinates in
	 * background and shows an elevation chart.
	 *
	 * @author David
	 *
	 */
	private class GetAltitude extends AsyncTask<String, Void, Double>{

		private HttpClient httpClient		= null;
		private HttpGet httpGet				= null;
		private HttpResponse httpResponse;
		private String responseAsString;
		private InputStream inputStream		= null;
		private JSONObject responseJSON;
		private RelativeLayout layout;

		@Override
		protected void onPreExecute() {
			httpClient = new DefaultHttpClient();
			responseAsString = null;
			// Delete elevation chart if exists
			layout = (RelativeLayout) findViewById(R.id.elevationchart);
			if (graphView != null) {
				layout.removeView(graphView);
			}
			layout.setVisibility(LinearLayout.INVISIBLE);
			graphView = null;
			elevationChartShown = false;
			fixMapPadding();
		}

		@SuppressWarnings("deprecation")
		@Override
		protected Double doInBackground(String... params) {
			httpGet = new HttpGet("http://maps.googleapis.com/maps/api/elevation/json?sensor=true"
									+ "&path=" + params[0]
									+ URLEncoder.encode("|") + params[1]
									+ "&samples=" + ELEVATION_SAMPLES);
			httpGet.setHeader("content-type", "application/json");
			try {
				httpResponse = httpClient.execute(httpGet);
				inputStream = httpResponse.getEntity().getContent();
				if (inputStream != null) {
					responseAsString = convertInputStreamToString(inputStream);
					responseJSON = new JSONObject(responseAsString);
					if (responseJSON != null) {
						if (responseJSON.get("status").equals("OK")) {
							buildElevationChart(responseJSON.getJSONArray("results"));
						}
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Double result) {
			showElevationProfileChart();
			// When HttpClient instance is no longer needed
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpClient.getConnectionManager().shutdown();
		}

		/**
		 * Converts the InputStream with the retrieved data to String.
		 * @param inputStream The input stream.
		 * @return The InputStream converted to String.
		 * @throws IOException
		 */
		private String convertInputStreamToString(final InputStream inputStream) throws IOException {
	        final BufferedReader bufferedReader =
	        		new BufferedReader(new InputStreamReader(inputStream));
	        String line = "";
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
		 * @param array
		 *            JSON array with the response data.
		 * @throws JSONException
		 */
		private void buildElevationChart(final JSONArray array) throws JSONException {
			// Creates the serie and adds data to it
			final GraphViewSeries series =
					new GraphViewSeries(
							null,
							new GraphViewSeriesStyle(getResources().getColor(R.color.elevation_chart_line),
									(int) (3 * DEVICE_DENSITY)),
							new GraphView.GraphViewData[] {});

			for (int w = 0; w < array.length(); w++)
				series.appendData(
						new GraphView.GraphViewData(
								w,
								Haversine.normalizeAltitudeByLocale(
										Double.valueOf(array.getJSONObject(w)
												.get("elevation").toString()),
										Locale.getDefault())),
						false,
						array.length());

			// Creates the line and add it to the chart
			graphView = new LineGraphView(
					getApplicationContext(),
					getText(R.string.elevation_profile).toString() + " ("
							+ Haversine.getAltitudeUnitByLocale(Locale.getDefault())
							+ ")");
			graphView.addSeries(series);
			graphView.getGraphViewStyle().setGridColor(Color.TRANSPARENT);
			graphView.getGraphViewStyle().setNumHorizontalLabels(1); // Con cero no va
			graphView.getGraphViewStyle().setTextSize(15 * DEVICE_DENSITY);
			graphView.getGraphViewStyle().setVerticalLabelsWidth((int) (50 * DEVICE_DENSITY));
		}

		/**
		 * Shows the elevation profile chart.
		 */
		private void showElevationProfileChart(){
			if (graphView != null){
				layout.setVisibility(LinearLayout.VISIBLE);
				layout.setBackgroundColor(getResources().getColor(R.color.elevation_chart_background));
				layout.addView(graphView);
				elevationChartShown = true;
				fixMapPadding();

				final ImageView closeChart = (ImageView) findViewById(R.id.closeChart);
				if (closeChart != null){
					closeChart.setVisibility(LinearLayout.VISIBLE);
					closeChart.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							layout.removeView(graphView);
							layout.setVisibility(LinearLayout.INVISIBLE);
							elevationChartShown = false;
							fixMapPadding();
						}
					});
				}
			}
		}
	}

	/**
	 * Sets map attending to the action which is performed.
	 */
	private void fixMapPadding() {
		final RelativeLayout rlElevationChart = (RelativeLayout) findViewById(R.id.elevationchart);
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
}
