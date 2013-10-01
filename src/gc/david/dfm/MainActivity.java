package gc.david.dfm;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends ActionBarActivity implements
		LocationListener, GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

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
	private String direccionBP					= "";
	private MenuItem searchItem					= null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mapa = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		mapa.setMyLocationEnabled(true);
		mapa.setMapType(GoogleMap.MAP_TYPE_HYBRID);

		// Create a new global location parameters object
		mLocationRequest = LocationRequest.create();
		//Set the update interval
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

		if (current == null)
			Toast.makeText(getApplicationContext(), getText(R.string.loading),
					Toast.LENGTH_LONG).show();

		mapa.setOnMapLongClickListener(new OnMapLongClickListener() {
			@Override
			public void onMapLongClick(LatLng point) {
				// Si no hemos encontrado la posición actual, no podremos
				// calcular la distancia
				if (current != null)
					tareasDistancia(point, "");
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
				tareasDistancia(new LatLng(marker.getPosition().latitude,
						marker.getPosition().longitude), "");
				aplicarZoom = true;
			}

			@Override
			public void onMarkerDrag(Marker marker) {
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

		mapa.setInfoWindowAdapter(new MyInfoWindowAdapter());

		// Para controlar instancias únicas, no queremos que cada vez que
		// busquemos nos inicie una nueva instancia de la aplicación
		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			new BuscaPosicion().execute(query);
			if (posicionElegida != null)
				tareasDistancia(posicionElegida, direccionBP);
		}
	}

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
											tareasDistancia(posicionElegida,
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
				Toast.makeText(getApplicationContext(),
						getText(R.string.nofindaddress), Toast.LENGTH_LONG)
						.show();
			} else if (result == -2) {
				Toast.makeText(getApplicationContext(),
						getText(R.string.noresults), Toast.LENGTH_LONG).show();
			} else if (result == -3) {
				Toast.makeText(getApplicationContext(),
						getText(R.string.nofindaddress), Toast.LENGTH_LONG)
						.show();
			}

			if (pd != null)
				pd.dismiss();

			if (posicionElegida != null)
				tareasDistancia(posicionElegida, direccionBP);

			MenuItemCompat.collapseActionView(searchItem);

			super.onPostExecute(result);
		}

		private boolean isOnline() {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if (netInfo != null && netInfo.isConnected())
				return true;
			return false;
		}

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
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
		// Configure the search info and add any event listeners
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		// Indicamos que la activity actual sea la buscadora
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		searchView.setSubmitButtonEnabled(false);
		searchView.setQueryRefinementEnabled(true);
		searchView.setIconifiedByDefault(true);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_search:
			return true;
			// case R.id.action_load:
			// cargarDistanciasBD();
			// return true;
		case R.id.menu_legalnotices:
			showGooglePlayServiceLicense();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// private void cargarDistanciasBD() {
	//
	// }

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

	/*
	 * Called when the system detects that this Activity is now visible.
	 */
	@Override
	public void onResume() {
		super.onResume();

		checkPlayServices();
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

	/**
	 * Report location updates to the UI.
	 * 
	 * @param location
	 *            The updated location.
	 */
	@Override
	public void onLocationChanged(Location location) {
		if (current != null)
			current.set(location);
		else
			current = new Location(location);

		if (inicioApp) {
			LatLng latlng = new LatLng(location.getLatitude(),
					location.getLongitude());
			// 17 es un buen nivel de zoom para esta acción
			mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 17));
			inicioApp = false;
		}
	}

	/**
	 * In response to a request to start updates, send a request to Location
	 * Services
	 */
	private void startPeriodicUpdates() {
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	/**
	 * In response to a request to stop updates, send a request to Location
	 * Services
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

		/*
		 * This method must return a Dialog to the DialogFragment.
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}

	private void tareasDistancia(LatLng point, String mensaje) {
		// Borramos los antiguos marcadores y líneas
		mapa.clear();

		// Calculamos la distancia
		distance = calculaDistancia(point);

		// Añadimos el nuevo marcador
		anadeMarcador(point, distance, mensaje);

		// Añadimos la línea
		anadeLinea(point);

		// Aquí hacer la animación de la cámara
		mueveCamaraZoom(
				new LatLng(current.getLatitude(), current.getLongitude()),
				point);
	}

	private void anadeMarcador(LatLng point, String distancia, String mensaje) {
		Marker marcador = mapa.addMarker(new MarkerOptions().position(point)
				.title(mensaje + distancia));
		marcador.setDraggable(true);
		marcador.showInfoWindow();
	}

	private void anadeLinea(LatLng point) {
		if (linea != null) {
			linea.remove();
			linea = null;
		}
		PolylineOptions lineOptions = new PolylineOptions().add(point).add(
				new LatLng(current.getLatitude(), current.getLongitude()));
		lineOptions.width(6);
		lineOptions.color(Color.GREEN);
		linea = mapa.addPolyline(lineOptions);
	}

	private String calculaDistancia(LatLng point) {
		double metros = Haversine.getDistance(point.latitude, point.longitude,
				current.getLatitude(), current.getLongitude());
		return Haversine.normalize(metros,
				getResources().getConfiguration().locale);
	}

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

	private class MyInfoWindowAdapter implements InfoWindowAdapter {

		private final View myContentsView;

		public MyInfoWindowAdapter() {
			myContentsView = getLayoutInflater().inflate(R.layout.custom_info,
					null);
		}

		@Override
		public View getInfoContents(Marker marker) {
			TextView texto = (TextView) myContentsView.findViewById(R.id.datos);
			texto.setText(marker.getTitle());

			return myContentsView;
		}

		@Override
		public View getInfoWindow(Marker marker) {
			return null;
		}

	}

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
}
