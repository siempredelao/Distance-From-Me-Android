package gc.david.dfm;

import gc.david.dfm.db.Distance;
import gc.david.dfm.db.DistancesDataSource;
import gc.david.dfm.map.Haversine;
import gc.david.dfm.map.LocationUtils;
import gc.david.dfm.map.MyInfoWindowAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.flurry.android.FlurryAdListener;
import com.flurry.android.FlurryAdSize;
import com.flurry.android.FlurryAdType;
import com.flurry.android.FlurryAds;
import com.flurry.android.FlurryAgent;
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

/**
 * Implements the app main Activity.
 * 
 * @author David
 * 
 */
public class MainActivity extends ActionBarActivity implements
		LocationListener, GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, FlurryAdListener {
	
	private GoogleMap mapa						= null;

	// A request to connect to Location Services
	private LocationRequest mLocationRequest	= null;

	// Stores the current instantiation of the location client in this object
	private LocationClient mLocationClient		= null;

	// La posición actual
	private Location current					= null;
	private Polyline linea						= null;
	private final int RQS_GooglePlayServices	= 1;
	// Al iniciar la app, movemos la cámara hacia la posición actual
	private boolean inicioApp					= true;
	// Para discernir si estamos refinando la posición o si estamos buscando
	// otra
	private boolean aplicarZoom					= true;
	private String distance						= "";
	private LatLng posicionElegida				= null;
	// Dirección que devuelve el buscar por una string
	private String direccionBP					= "";
	private MenuItem searchItem					= null;
	// Si venimos por ejemplo de whatsapp para ver una dirección
	private boolean verPosicion					= false;
	private String posicionDestinoEnvio			= "";
	// Cuando obtenemos una distancia de la base de datos
	private boolean cargandoDistancia			= false;
	
	private FrameLayout mBanner = null;
	private String adName = "Prueba";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mapa = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		
		if (mapa != null){
			mapa.setMyLocationEnabled(true);
			mapa.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			
			mBanner = (FrameLayout) findViewById(R.id.banner);
			
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
			if (! isConnected)
				alertDialogShow(
						android.provider.Settings.ACTION_WIRELESS_SETTINGS,
						getText(R.string.wireless_off).toString(),
						getText(R.string.wireless_enable).toString(),
						getText(R.string.do_nothing).toString());
			
			mapa.setOnMapLongClickListener(new OnMapLongClickListener() {
				@Override
				public void onMapLongClick(LatLng point) {
					// Si no hemos encontrado la posición actual, no podremos
					// calcular la distancia
					if (current != null)
						tareasDistancia(
								new LatLng(current.getLatitude(), current
										.getLongitude()), point, "");
				}
			});
	
			mapa.setOnMarkerDragListener(new OnMarkerDragListener() {
				@Override
				public void onMarkerDragStart(Marker marker) {
				}
	
				@Override
				public void onMarkerDragEnd(Marker marker) {
					// NO movemos el zoom porque estamos simplemente afinando la
					// posición
					aplicarZoom = false;
					tareasDistancia(
							new LatLng(current.getLatitude(), current
									.getLongitude()),
							new LatLng(marker.getPosition().latitude, marker
									.getPosition().longitude), "");
					aplicarZoom = true;
				}
	
				@Override
				public void onMarkerDrag(Marker marker) {
					// No funciona el hacerlo sobre la marcha...
				}
			});
	
			mapa.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
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
	
			mapa.setInfoWindowAdapter(new MyInfoWindowAdapter(this));

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
	 * Makes easy to toast!
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
			new BuscaPosicion().execute(query);
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
		String queryParameter = u.getQueryParameter("q"); // loc:latitud,longitud (You)
		if (queryParameter != null){
			// Esta string será la que mandemos a BuscaPosicion
			posicionDestinoEnvio = queryParameter.replace("loc:", "").replaceAll(" (\\D*)", ""); // latitud,longitud
			
			verPosicion = true;
		}
	}

	/**
	 * A subclass of AsyncTask that calls getFromLocationName() in the
	 * background.
	 */
	private class BuscaPosicion extends AsyncTask<String, Void, Integer> {

		private ProgressDialog pd;
		private List<Address> addresses;

		@Override
		protected void onPreExecute() {
			addresses = null;
			direccionBP = "";
			posicionElegida = null;
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
						android.provider.Settings.ACTION_WIRELESS_SETTINGS,
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
								(String[]) agrupaDirecciones(addresses)
										.toArray(new String[addresses.size()]),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int item) {
										procesaElegido(item);

										if (posicionElegida != null)
											tareasDistancia(
													new LatLng(
															current.getLatitude(),
															current.getLongitude()),
													posicionElegida,
													direccionBP);
									}
								});
						builder.create();
						builder.show();
					} else {
						procesaElegido(0);
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

			if (posicionElegida != null)
				tareasDistancia(
						new LatLng(current.getLatitude(),
								current.getLongitude()), posicionElegida,
						direccionBP);

			MenuItemCompat.collapseActionView(searchItem);

			super.onPostExecute(result);
		}

		/**
		 * Gives the current network status.
		 * 
		 * @return Returns <code>true</code> if the device is connected to a network;
		 *         otherwise, returns <code>false</code>.
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
		private void procesaElegido(int item) {
			// esto para el marcador!
			// +1 porque si buscamos por país nos devuelve 0 y ni
			// entra
			// en el bucle
			for (int i = 0; i < addresses.get(item).getMaxAddressLineIndex() + 1; i++)
				direccionBP += addresses.get(item).getAddressLine(i) + "\n";

			posicionElegida = new LatLng(addresses.get(item).getLatitude(),
					addresses.get(item).getLongitude());
		}

		/**
		 * Extract a list of address from a list of Address objects.
		 * 
		 * @param lista
		 *            An Address's list.
		 * @return A string list with only addresses in text.
		 */
		private List<String> agrupaDirecciones(List<Address> lista) {
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
		DistancesDataSource dDS = new DistancesDataSource(getApplicationContext());
		dDS.open();
		if (dDS != null){
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
			cargarDistanciasBD();
			return true;
		case R.id.menu_legalnotices:
			showGooglePlayServiceLicense();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Load all entries stored in the database and show them to the user in a
	 * dialog.
	 */
	private void cargarDistanciasBD() {
		DistancesDataSource dds = new DistancesDataSource(getApplicationContext());
		dds.open();
		ArrayList<Distance> distancias = dds.getAllDistances();
		dds.close();

		final AdaptadorDistancias adaptador = new AdaptadorDistancias(this, distancias);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getText(R.string.list_dialog_title).toString())
				.setAdapter(adaptador, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						cargandoDistancia = true;
						Distance distancia = adaptador.getDatos().get(which);
						LatLng inicio = new LatLng(distancia.getLat_a(), distancia.getLon_a());
						LatLng fin = new LatLng(distancia.getLat_b(), distancia.getLon_b());
						
						tareasDistancia(inicio, fin, distancia.getNombre() + "\n");
					}
				}).create().show();
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
        FlurryAds.removeAd(this, adName, mBanner);
        FlurryAds.setAdListener(null);
		FlurryAgent.onEndSession(this);
	}

	/*
	 * Called when the Activity is restarted, even before it becomes visible.
	 */
	@Override
	public void onStart() {
		super.onStart();

		FlurryAgent.onStartSession(this, "FHH59NSTJNYMMNRNW7KN");
        // get callbacks for ad events
        FlurryAds.setAdListener(this);
		// fetch and prepare ad for this ad space. won’t render one yet
		FlurryAds.fetchAd(this, adName, mBanner, FlurryAdSize.BANNER_BOTTOM);
		
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
	@Override
	public void onResume() {
		super.onResume();

		checkPlayServices();
	}
	
	@Override
	public void onDestroy() {
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
	 * @return Returns <code>true</code> if available; <code>false</code> otherwise.
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

		if (inicioApp) {
			if (verPosicion){
				if (current != null){
					new BuscaPosicion().execute(posicionDestinoEnvio);
				}
				verPosicion = false;
			} else {
				LatLng latlng = new LatLng(location.getLatitude(),
						location.getLongitude());
				// 17 es un buen nivel de zoom para esta acción
				mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 17));
			}
			inicioApp = false;
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
	 * Show a dialog returned by Google Play services for the connection error
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
	 * Define a DialogFragment to display the error dialog generated in
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
	private void tareasDistancia(LatLng start, LatLng end, String mensaje) {
		// Borramos los antiguos marcadores y líneas
		mapa.clear();

		// Calculamos la distancia
		distance = calculaDistancia(start, end);

		// Añadimos el nuevo marcador
		anadeMarcador(end, distance, mensaje);

		// Añadimos la línea
		anadeLinea(start, end);

		// Aquí hacer la animación de la cámara
		mueveCamaraZoom(start, end);
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
	private void anadeMarcador(LatLng point, String distancia, String mensaje) {
		Marker marcador = mapa.addMarker(new MarkerOptions().position(point)
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
	private void anadeLinea(LatLng start, LatLng end) {
		if (linea != null) {
			linea.remove();
			linea = null;
		}
		PolylineOptions lineOptions = new PolylineOptions().add(start).add(
				end);
		lineOptions.width(6);
		if (cargandoDistancia){
			cargandoDistancia = false;
			lineOptions.color(Color.YELLOW);
		} else
			lineOptions.color(Color.GREEN);
		linea = mapa.addPolyline(lineOptions);
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
	private String calculaDistancia(LatLng start, LatLng end) {
		double metros = Haversine.getDistanceJNI(start.latitude,
				start.longitude, end.latitude, end.longitude);

		return Haversine.normalize(metros,
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
	private void mueveCamaraZoom(LatLng p1, LatLng p2) {
		double centroLat = (p1.latitude + p2.latitude) / 2;
		double centroLon = (p1.longitude + p2.longitude) / 2;

		if (aplicarZoom)
			mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					centroLat, centroLon), calculaZoom(p1, p2)));
		else
			mapa.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(
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
	private float calculaZoom(LatLng p1, LatLng p2) {
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

	@Override
	public void onAdClicked(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAdClosed(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAdOpened(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onApplicationExit(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRenderFailed(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRendered(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onVideoCompleted(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean shouldDisplayAd(String arg0, FlurryAdType arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void spaceDidFailToReceiveAd(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void spaceDidReceiveAd(String arg0) {
		// called when the ad has been prepared, ad can be displayed:
		FlurryAds.displayAd(this, adName, mBanner);
	}
}
