package gc.david.dfm;

import gc.david.dfm.db.Distance;
import gc.david.dfm.db.DistancesDataSource;
import gc.david.dfm.map.Haversine;
import gc.david.dfm.map.LocationUtils;
import gc.david.dfm.map.MyInfoWindowAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

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

/**
 * Implements the app main Activity.
 * 
 * @author David
 * 
 */
public class MainActivity extends ActionBarActivity implements
		LocationListener, GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private GoogleMap googleMap					= null;
	// A request to connect to Location Services
	private LocationRequest mLocationRequest	= null;
	// Stores the current instantiation of the location client in this object
	private LocationClient mLocationClient		= null;
	// Current position
	private Location current					= null;
	private Polyline line						= null;
	private final int RQS_GooglePlayServices	= 1;
	// Moves to current position if app has just started
	private boolean appHasJustStarted			= true;
	// Distinguish between position polishing and other position searching
	private boolean applyZoom					= true;
	private String distance						= "";
	private LatLng selectedPosition				= null;
	// Address returned at string searching
	private String bpAddress					= "";
	private MenuItem searchItem					= null;
	// Show position if we come from other app (p.e. Whatsapp)
	private boolean seePosition					= false;
	private String sendDestinationPosition		= "";
	// To change line color when we choose a distance from database
	private boolean loadingDistance				= false;
	private IMBanner banner						= null;
	// Google Map items padding
	private boolean bannerPadding				= false;
	private boolean elevationPadding			= false;
	private static int ELEVATION_SAMPLES		= 100;
	private AsyncTask showingElevation			= null;
	private GraphView graphView					= null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SupportMapFragment fragment = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map));
		googleMap = fragment.getMap();

		if (googleMap != null) {
			googleMap.setMyLocationEnabled(true);
			googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			
			// InMobi Ads
			InMobi.initialize(this, "9b61f509a1454023b5295d8aea4482c2");
			banner = (IMBanner) findViewById(R.id.banner);
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
					// To make Google workers happy ¬¬
					bannerPadding = true;
					mapPadding();
				}
				@Override
				public void onBannerRequestFailed(IMBanner arg0, IMErrorCode arg1) {
				}
				@Override
				public void onBannerInteraction(IMBanner arg0, Map<String, String> arg1) {
				}
			});
			banner.loadBanner();
			
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			boolean isConnected = activeNetwork != null
					&& activeNetwork.isConnectedOrConnecting();
			if (!isConnected)
				// Show the wireless centralized settings in API<11
				// or shows general settings in API >=11
				alertDialogShow(
						(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB ?
								android.provider.Settings.ACTION_WIRELESS_SETTINGS :
									android.provider.Settings.ACTION_SETTINGS),
						getText(R.string.wireless_off).toString(),
						getText(R.string.wireless_enable).toString(),
						getText(R.string.do_nothing).toString());
			
			googleMap.setOnMapLongClickListener(new OnMapLongClickListener() {
				@Override
				public void onMapLongClick(LatLng point) {
					// Si no hemos encontrado la posición actual, no podremos
					// calcular la distancia
					if (current != null)
						distanceTasks(new LatLng(current.getLatitude(),
								current.getLongitude()), point, "");
				}
			});

			googleMap.setOnMarkerDragListener(new OnMarkerDragListener() {
				@Override
				public void onMarkerDragStart(Marker marker) {
				}

				@Override
				public void onMarkerDragEnd(Marker marker) {
					// NO movemos el zoom porque estamos simplemente afinando la
					// posición
					applyZoom = false;
					distanceTasks(
							new LatLng(current.getLatitude(), current
									.getLongitude()),
							new LatLng(marker.getPosition().latitude, marker
									.getPosition().longitude), "");
					applyZoom = true;
				}

				@Override
				public void onMarkerDrag(Marker marker) {
				}
			});

			googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
				@Override
				public void onInfoWindowClick(Marker marker) {
					// Abrir una activity
					Intent intent = new Intent(MainActivity.this,
							ShowInfoActivity.class);
					intent.putExtra("origen", new LatLng(current.getLatitude(),
							current.getLongitude()));
					intent.putExtra("destino", new LatLng(
							marker.getPosition().latitude,
							marker.getPosition().longitude));
					intent.putExtra("distancia", distance);
					startActivity(intent);
				}
			});

			googleMap.setInfoWindowAdapter(new MyInfoWindowAdapter(this));

			// Iniciando la app
			if (current == null)
				toastIt(getText(R.string.loading).toString());

			handleIntents(getIntent());
		}

		// Create a new global location parameters object
		mLocationRequest = LocationRequest.create();
		// Set the update interval
		mLocationRequest
				.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the interval ceiling to one minute
		mLocationRequest
				.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

		// Create a new location client, using the enclosing class to handle
		// callbacks
		mLocationClient = new LocationClient(this, this, this);
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
	private void handleIntents(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			handleSearchIntent(intent);
		} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			handleSendPositionIntent(intent);
		}
	}

	/**
	 * Handles a search intent.
	 * 
	 * @param intent
	 *            Input intent.
	 */
	private void handleSearchIntent(Intent intent) {
		// Para controlar instancias únicas, no queremos que cada vez que
		// busquemos nos inicie una nueva instancia de la aplicación
		String query = intent.getStringExtra(SearchManager.QUERY);
		if (current != null)
			new SearchPosition().execute(query);
	}

	/**
	 * Handles a send intent with position data.
	 * 
	 * @param intent
	 *            Input intent with position data.
	 * 
	 */
	private void handleSendPositionIntent(Intent intent) {
		Uri u = intent.getData();

		// Buscamos el envío por Whatsapp
		String queryParameter = u.getQueryParameter("q"); // loc:latitud,longitud
															// (You)
		if (queryParameter != null) {
			// Esta string será la que mandemos a BuscaPosicion
			sendDestinationPosition = queryParameter.replace("loc:", "")
					.replaceAll(" (\\D*)", ""); // latitud,longitud

			seePosition = true;
		}
	}

	/**
	 * A subclass of AsyncTask that calls getFromLocationName() in the
	 * background.
	 */
	private class SearchPosition extends AsyncTask<String, Void, Integer> {

		private ProgressDialog pd;
		private List<Address> addresses;

		@Override
		protected void onPreExecute() {
			addresses = null;
			bpAddress = "";
			selectedPosition = null;
			pd = new ProgressDialog(MainActivity.this);
			pd.setTitle(R.string.searching);
			pd.setMessage(getText(R.string.wait) + "...");
			pd.setCancelable(false);
			pd.setIndeterminate(true);
			pd.show();

			// Comprobamos que haya conexión con internet (WiFi o Datos)
			if (!isOnline()) {
				if (pd != null)
					pd.dismiss();
				
					alertDialogShow(
							(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB ?
									android.provider.Settings.ACTION_WIRELESS_SETTINGS :
										android.provider.Settings.ACTION_SETTINGS),
							getText(R.string.wireless_off).toString(),
							getText(R.string.wireless_enable).toString(),
							getText(R.string.do_nothing).toString());
					
				// Restauramos el menú y que vuelva a empezar de nuevo
				MenuItemCompat.collapseActionView(searchItem);
				cancel(false);
			}

			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(String... params) {
			/* get latitude and longitude from the addresses */
			Geocoder geoCoder = new Geocoder(getApplicationContext(),
					Locale.getDefault());
			try {
				addresses = geoCoder.getFromLocationName(params[0], 5);
			} catch (IOException e) {
				e.printStackTrace();
				return -1; // No encuentra una direccion, no puede conectar con
							// el servidor
			}
			if (addresses == null)
				return -3; // empty list if there is no backend service
							// available
			else if (addresses.size() > 0)
				return 0;
			else
				return -2; // null if no matches were found // Cuando no hay
							// conexión que sirva
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (result == 0) {
				if (addresses != null && addresses.size() > 0) {
					// Si hay varios, elegimos uno. Si solo hay uno, mostramos
					// ese
					if (addresses.size() > 1) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								MainActivity.this);

						builder.setTitle(getText(R.string.select_address));
						builder.setItems(
								(String[]) groupAdresses(addresses)
										.toArray(new String[addresses.size()]),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int item) {
										processSelected(item);

										if (selectedPosition != null)
											distanceTasks(
													new LatLng(
															current.getLatitude(),
															current.getLongitude()),
													selectedPosition,
													bpAddress);
									}
								});
						builder.create();
						builder.show();
					} else {
						processSelected(0);
					}
				}
			} else if (result == -1) {
				toastIt(getText(R.string.nofindaddress).toString());
			} else if (result == -2) {
				toastIt(getText(R.string.noresults).toString());
			} else if (result == -3) {
				toastIt(getText(R.string.nofindaddress).toString());
			}

			if (pd != null)
				pd.dismiss();

			if (selectedPosition != null)
				distanceTasks(
						new LatLng(current.getLatitude(),
								current.getLongitude()), selectedPosition,
						bpAddress);

			MenuItemCompat.collapseActionView(searchItem);

			super.onPostExecute(result);
		}

		/**
		 * Gives the current network status.
		 * 
		 * @return Returns <code>true</code> if the device is connected to a
		 *         network; otherwise, returns <code>false</code>.
		 */
		private boolean isOnline() {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if (netInfo != null && netInfo.isConnected())
				return true;
			return false;
		}

		/**
		 * Process the address selected by the user and set the new destination
		 * position.
		 * 
		 * @param item
		 *            The index item in the AlertDialog.
		 */
		private void processSelected(int item) {
			// esto para el marcador!
			// +1 porque si buscamos por país nos devuelve 0 y ni
			// entra
			// en el bucle
			for (int i = 0; i < addresses.get(item).getMaxAddressLineIndex() + 1; i++)
				bpAddress += addresses.get(item).getAddressLine(i) + "\n";

			selectedPosition = new LatLng(addresses.get(item).getLatitude(),
					addresses.get(item).getLongitude());
		}

		/**
		 * Extract a list of address from a list of Address objects.
		 * 
		 * @param lista
		 *            An Address's list.
		 * @return A string list with only addresses in text.
		 */
		private List<String> groupAdresses(List<Address> lista) {
			List<String> nueva = new ArrayList<String>();
			String aux;
			for (Address l : lista) {
				aux = "";
				for (int j = 0; j < l.getMaxAddressLineIndex() + 1; j++)
					aux += l.getAddressLine(j) + "\n";
				nueva.add(aux);
			}
			return nueva;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the options menu from XML
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		// Expandir el EditText de la búsqueda a lo largo del ActionBar
		searchItem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) MenuItemCompat
				.getActionView(searchItem);
		// Configure the search info and add any event listeners
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		// Indicamos que la activity actual sea la buscadora
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		searchView.setSubmitButtonEnabled(false);
		searchView.setQueryRefinementEnabled(true);
		searchView.setIconifiedByDefault(true);

		// Muestra el icono de cargar si procede
		MenuItem loadItem = menu.findItem(R.id.action_load);
		DistancesDataSource dDS = new DistancesDataSource(
				getApplicationContext());
		dDS.open();
		if (dDS != null) {
			if (dDS.getAllDistances() == null)
				loadItem.setVisible(false);
			dDS.close();
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
			rateApp();
			return true;
		case R.id.menu_legalnotices:
			showGooglePlayServiceLicense();
			return true;
		case R.id.menu_settings:
			showSettings();
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
		DistancesDataSource dds = new DistancesDataSource(
				getApplicationContext());
		dds.open();
		ArrayList<Distance> distancias = dds.getAllDistances();
		dds.close();

		if (distancias != null){
			final DistanceAdapter adaptador = new DistanceAdapter(this,
					distancias);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getText(R.string.list_dialog_title).toString())
					.setAdapter(adaptador, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							loadingDistance = true;
							Distance distancia = adaptador.getData().get(which);
							LatLng inicio = new LatLng(distancia.getLat_a(),
									distancia.getLon_a());
							LatLng fin = new LatLng(distancia.getLat_b(), distancia
									.getLon_b());
	
							distanceTasks(inicio, fin, distancia.getName()
									+ "\n");
						}
					}).create().show();
		} else
			toastIt(getText(R.string.no_distances_registered).toString());
	}

	/**
	 * Shows settings activity.
	 */
	private void showSettings(){
		startActivity(new Intent(this, SettingsActivity.class));
	}
	
	/**
	 * Shows rate dialog.
	 */
	private void rateApp(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.rate_title)
		.setMessage(R.string.rate_message)
		.setPositiveButton(getText(R.string.rate_positive_button), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				openPlayStore();
			}
		})
		.setNegativeButton(getText(R.string.rate_negative_button), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				goComplain();
			}
		})
		.create()
		.show();
	}
	
	/**
	 * Opens Google Play Store, in Distance From Me page
	 */
	private void openPlayStore(){
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=gc.david.dfm")));
	}
	
	/**
	 * Drives the user to a email client to make a complain/suggestion.
	 */
	private void goComplain(){
		Intent sendTo = new Intent(Intent.ACTION_SENDTO);
	    String uriText = "mailto:" + Uri.encode("davidaguiargonzalez@gmail.com") +
	            "?subject=" + Uri.encode(getText(R.string.complain_message).toString()) +
	            "&body=" + Uri.encode(getText(R.string.complain_hint).toString());
	    Uri uri = Uri.parse(uriText);
	    sendTo.setData(uri);

	    List<ResolveInfo> resolveInfos = 
	            getPackageManager().queryIntentActivities(sendTo, 0);

        // Emulators may not like this check...
        if (!resolveInfos.isEmpty()){
        	startActivity(sendTo);
        } else {
		    // Nothing resolves send to, so fallback to send...
			Intent intent = new Intent(Intent.ACTION_SENDTO);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"davidaguiargonzalez@gmail.com"});
			intent.putExtra(Intent.EXTRA_SUBJECT, getText(R.string.complain_message).toString());
			intent.putExtra(Intent.EXTRA_TEXT, getText(R.string.complain_hint).toString());
			try {
				startActivity(intent);
			} catch (ActivityNotFoundException e){
				toastIt(getText(R.string.complain_problem).toString());
			}
        }
	}
		
	/**
	 * Shows an AlertDialog with the Google Play Services License.
	 */
	private void showGooglePlayServiceLicense() {
		String LicenseInfo = GooglePlayServicesUtil
				.getOpenSourceSoftwareLicenseInfo(getApplicationContext());
		AlertDialog.Builder LicenseDialog = new AlertDialog.Builder(
				MainActivity.this);
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
		if (mLocationClient.isConnected())
			stopPeriodicUpdates();

		// After disconnect() is called, the client is considered "dead".
		mLocationClient.disconnect();

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
		mLocationClient.connect();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	/*
	 * Called when the system detects that this Activity is now visible.
	 */
	@SuppressLint("NewApi")
	@Override
	public void onResume() {
		super.onResume();
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
			invalidateOptionsMenu();
		checkPlayServices();
	}

	@Override
	public void onDestroy() {
		if (showingElevation != null)
			showingElevation.cancel(true);
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
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {

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
		// Comprobamos que Google Play Services esté disponible en el terminal
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getApplicationContext());

		// Si está disponible, devolvemos verdadero. Si no, mostramos un mensaje
		// de error y devolvemos falso
		if (resultCode == ConnectionResult.SUCCESS)
			return true;
		else {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						RQS_GooglePlayServices).show();
			else
				// Log.i("checkPlayServices", "Dispositivo no soportado");
				finish();
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
		if (current != null)
			current.set(location);
		else
			current = new Location(location);

		if (appHasJustStarted) {
			if (seePosition) {
				if (current != null) {
					new SearchPosition().execute(sendDestinationPosition);
				}
				seePosition = false;
			} else {
				LatLng latlng = new LatLng(location.getLatitude(),
						location.getLongitude());
				// 17 is a good zoom level for this action
				googleMap.animateCamera(CameraUpdateFactory
						.newLatLngZoom(latlng, 17));
			}
			appHasJustStarted = false;
		}
	}

	/**
	 * In response to a request to start updates, send a request to Location
	 * Services.
	 */
	private void startPeriodicUpdates() {
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	/**
	 * In response to a request to stop updates, send a request to Location
	 * Services.
	 */
	private void stopPeriodicUpdates() {
		mLocationClient.removeLocationUpdates(this);
	}

	/**
	 * Shows a dialog returned by Google Play services for the connection error
	 * code
	 * 
	 * @param errorCode
	 *            An error code returned from onConnectionFailed
	 */
	private void showErrorDialog(int errorCode) {

		// Get the error dialog from Google Play services
		Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
				this, LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

		// If Google Play services can provide an error dialog
		if (errorDialog != null) {

			// Create a new DialogFragment in which to show the error dialog
			ErrorDialogFragment errorFragment = new ErrorDialogFragment();

			// Set the dialog in the DialogFragment
			errorFragment.setDialog(errorDialog);

			// Show the error dialog in the DialogFragment
			errorFragment.show(getSupportFragmentManager(),
					LocationUtils.APPTAG);
		}
	}

	/**
	 * Defines a DialogFragment to display the error dialog generated in
	 * showErrorDialog.
	 */
	public static class ErrorDialogFragment extends DialogFragment {

		// Global field to contain the error dialog
		private Dialog mDialog;

		/**
		 * Default constructor. Sets the dialog field to null
		 */
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		/**
		 * Set the dialog to display
		 * 
		 * @param dialog
		 *            An error dialog
		 */
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		/**
		 * This method must return a Dialog to the DialogFragment.
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}

	/**
	 * Calculates the distance to a specified position, adds the marker and the
	 * line.
	 * 
	 * @param start
	 *            Start position.
	 * @param end
	 *            Destination position.
	 * @param mensaje
	 *            Address to show in the info window (if needed).
	 */
	private void distanceTasks(LatLng start, LatLng end, String mensaje) {
		// Borramos los antiguos marcadores y líneas
		googleMap.clear();

		// Calculamos la distancia
		distance = calculateDistance(start, end);

		// Añadimos el nuevo marcador
		addMarker(end, distance, mensaje);

		// Añadimos la línea
		addLine(start, end);

		// Aquí hacer la animación de la cámara
		moveCameraZoom(start, end);
		
		// Muestra el perfil de elevación si está en las preferencias
		// y si está conectado a internet
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		if (sharedPreferences.getBoolean("elevation_chart", false)){
			// Está conectado a internet?
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if (netInfo != null && netInfo.isConnected())
					getElevation(start, end);
		}
	}
	
	/**
	 * Adds a marker to the map in a specified position and shows its info
	 * window.
	 * 
	 * @param point
	 *            Destination position.
	 * @param distancia
	 *            Distance to destination.
	 * @param mensaje
	 *            Destination address (if needed).
	 */
	private void addMarker(LatLng point, String distancia, String mensaje) {
		Marker marcador = googleMap.addMarker(new MarkerOptions().position(point)
				.title(mensaje + distancia));
		marcador.setDraggable(true);
		marcador.showInfoWindow();
	}

	/**
	 * Adds a line between start and end positions.
	 * 
	 * @param start
	 *            Start position.
	 * @param end
	 *            Destination position.
	 */
	private void addLine(LatLng start, LatLng end) {
		if (line != null) {
			line.remove();
			line = null;
		}
		PolylineOptions lineOptions = new PolylineOptions().add(start).add(end);
		lineOptions.width(3*getResources().getDisplayMetrics().density);
		if (loadingDistance) {
			loadingDistance = false;
			lineOptions.color(Color.YELLOW);
		} else
			lineOptions.color(Color.GREEN);
		line = googleMap.addPolyline(lineOptions);
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
	private String calculateDistance(LatLng start, LatLng end) {
		double metros = Haversine.getDistanceJNI(start.latitude,
				start.longitude, end.latitude, end.longitude);

		return Haversine.normalizeDistance(metros,
				getResources().getConfiguration().locale);
	}

	/**
	 * Moves camera position and applies zoom if needed.
	 * 
	 * @param p1
	 *            Start position.
	 * @param p2
	 *            Destination position.
	 */
	private void moveCameraZoom(LatLng p1, LatLng p2) {
		double centroLat = 0.0, centroLon = 0.0;
		// Diferenciamos según preferencias
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String centre = sharedPreferences.getString("centre", "CEN");
		if (centre.equals("CEN")){
			centroLat = (p1.latitude + p2.latitude) / 2;
			centroLon = (p1.longitude + p2.longitude) / 2;	
		} else if (centre.equals("DES")){
			centroLat = p2.latitude;
			centroLon = p2.longitude;
		}
		
		if (applyZoom)
			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					centroLat, centroLon), calculateZoom(p1, p2)));
		else
			googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(
					p2.latitude, p2.longitude)));
	}

	/**
	 * Calculates zoom level to make possible current and destination positions
	 * appear in the device.
	 * 
	 * @param p1
	 *            Current position.
	 * @param p2
	 *            Destination position.
	 * @return Zoom level.
	 */
	private float calculateZoom(LatLng p1, LatLng p2) {
		double metros = Haversine.getDistance(p1.latitude, p1.longitude,
				p2.latitude, p2.longitude);
		double kms = metros / 1000;

		if (kms > 2700)
			return 3;
		else if (kms > 1300)
			return 4;
		else if (kms > 650)
			return 5;
		else if (kms > 325)
			return 6;
		else if (kms > 160)
			return 7;
		else if (kms > 80)
			return 8;
		else if (kms > 40)
			return 9;
		else if (kms > 20)
			return 10;
		else if (kms > 10)
			return 11;
		else if (kms > 5)
			return 12;
		else if (kms > 2.5)
			return 13;
		else if (kms > 1.25)
			return 14;
		else if (kms > 0.6)
			return 15;
		else if (kms > 0.3)
			return 16;
		else if (kms > 0.15)
			return 17;

		return 18;
	}

	private void getElevation(LatLng start, LatLng end) {
		String startPos = 	String.valueOf(start.latitude) +
							"," +
							String.valueOf(start.longitude);
		String endPos = String.valueOf(end.latitude) +
						"," +
						String.valueOf(end.longitude);

		if (showingElevation != null)
			showingElevation.cancel(true);
		showingElevation = new GetAltitude().execute(startPos, endPos);
	}

	private class GetAltitude extends AsyncTask<String, Void, Double>{

		private HttpClient httpClient					= null;
		private HttpGet httpGet							= null;
		private HttpResponse response;
		private String respStr, sensor, url;
		private InputStream inputStream					= null;
		private JSONObject respJSON;
		private RelativeLayout layout;
		
		@Override
		protected void onPreExecute() {
			httpClient = new DefaultHttpClient();
			url = "http://maps.googleapis.com/maps/api/elevation/json";
			sensor = "sensor=true";
			respStr = null;
			layout = (RelativeLayout) findViewById(R.id.elevationchart);
			if (graphView != null)
				layout.removeView(graphView);
			layout.setVisibility(LinearLayout.INVISIBLE);
			graphView = null;
			elevationPadding = false;
			mapPadding();
		}

		@SuppressWarnings("deprecation")
		@Override
		protected Double doInBackground(String... params) {
			httpGet = new HttpGet(url + "?" + sensor
									+ "&path=" + params[0]
									+ URLEncoder.encode("|") + params[1]
									+ "&samples=" + ELEVATION_SAMPLES);
			httpGet.setHeader("content-type", "application/json");
			try {
				response = httpClient.execute(httpGet);
				inputStream = response.getEntity().getContent();
				if (inputStream != null){
					respStr = convertInputStreamToString(inputStream);
					respJSON = new JSONObject(respStr);
					if (respJSON != null){
						if (respJSON.get("status").equals("OK")){
							buildElevationChart(respJSON.getJSONArray("results"));
						} else
							Log.d("doInBackground", "Error en el JSON! " + respJSON.getString("status"));
					}
				} else
					Log.d("doInBackground", "InputStream null!");
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
			showElevationProfile();
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
		private String convertInputStreamToString(InputStream inputStream) throws IOException{
	        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
	        String line = "", result = "";
	        while ((line = bufferedReader.readLine()) != null)
	            result += line;
	 
	        inputStream.close();
	        return result;
	    }
		
		/**
		 * Shows the elevation profile chart.
		 */
		private void showElevationProfile(){
			if (graphView != null){
				layout.setVisibility(LinearLayout.VISIBLE);
				layout.setBackgroundColor(Color.parseColor("#66191919"));
				layout.addView(graphView);
				elevationPadding = true;
				mapPadding();
				
				ImageView closeChart = (ImageView) findViewById(R.id.closeChart);
				if (closeChart != null){
					closeChart.setVisibility(LinearLayout.VISIBLE);
					closeChart.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							layout.removeView(graphView);
							layout.setVisibility(LinearLayout.INVISIBLE);
							elevationPadding = false;
							mapPadding();
						}
					});
				}
			}
		}

		/**
		 * Builds the information about the elevation profile chart. Use this in
		 * a background task.
		 * 
		 * @param array
		 *            JSON array with the response data.
		 * @throws JSONException
		 */
		private void buildElevationChart(JSONArray array) throws JSONException {
			float density = getResources().getDisplayMetrics().density;
			GraphViewSeries series = new GraphViewSeries(null,
					new GraphViewSeriesStyle(Color.parseColor("#00FA9A"), (int) (3*density)),
					new GraphView.GraphViewData[] {});

			for (int w = 0; w < array.length(); w++)
				series.appendData(
						new GraphView.GraphViewData(w, Double.valueOf(array
								.getJSONObject(w).get("elevation").toString())),
						false, array.length());

			graphView = new LineGraphView(getApplicationContext(), getText(
					R.string.elevation_profile).toString());
			graphView.addSeries(series);
			graphView.getGraphViewStyle().setGridColor(Color.TRANSPARENT);
			graphView.getGraphViewStyle().setNumHorizontalLabels(1); // Con cero no va
			graphView.getGraphViewStyle().setTextSize(15*density);
			graphView.getGraphViewStyle().setVerticalLabelsWidth((int) (50*density));
		}
	}
	
	/**
	 * Shows an AlertDialog with a message, positive and negative button, and
	 * executes an action if needed.
	 * 
	 * @param action
	 *            Action to execute.
	 * @param message
	 *            Message to show to the user.
	 * @param positiveButton
	 *            Positive button text.
	 * @param negativeButton
	 *            Negative button text.
	 */
	private void alertDialogShow(final String action, String message,
			String positiveButton, String negativeButton) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(positiveButton,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Intent optionsIntent = new Intent(action);
								startActivity(optionsIntent);
							}
						});
		builder.setNegativeButton(negativeButton,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	/**
	 * Sets map attending to the action which is performed.
	 */
	private void mapPadding() {
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.elevationchart);
		if (bannerPadding)
			if (elevationPadding)
				googleMap.setPadding(0, layout.getHeight(), 0, banner.getLayoutParams().height);
			else
				googleMap.setPadding(0, 0, 0, banner.getLayoutParams().height);
		else
			if (elevationPadding)
				googleMap.setPadding(0, layout.getHeight(), 0, 0);
			else
				googleMap.setPadding(0, 0, 0, 0);
	}
}
