package gc.david.dfm;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider; // Esta clase da algún tipo de error
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

/**
 * ShowInfoActivity shows information about the distance to the user.
 * 
 * @author David
 * 
 */
public class ShowInfoActivity extends ActionBarActivity {

	private LatLng origen		= null;
	private LatLng destino		= null;

	private TextView titulo1	= null;
	private TextView titulo2	= null;
	private TextView datos1		= null;
	private TextView datos2		= null;
	private TextView distancia	= null;

	private MenuItem menuItem	= null;
	private String direccion1	= null;
	private String direccion2	= null;
	private String dist			= null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_info);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		extraeDatosIntent();

		titulo1 = (TextView) findViewById(R.id.titulo_datos1);
		titulo2 = (TextView) findViewById(R.id.titulo_datos2);
		rellenaTitulos();

		datos1 = (TextView) findViewById(R.id.datos1);
		datos2 = (TextView) findViewById(R.id.datos2);
		rellenaDirecciones();

		distancia = (TextView) findViewById(R.id.distancia);
		rellenaDistancia();
	}

	/**
	 * Get data form the Intent.
	 */
	private void extraeDatosIntent() {
		Intent datosEntrantes = getIntent();
		this.origen = (LatLng) datosEntrantes.getParcelableExtra("origen");
		this.destino = (LatLng) datosEntrantes.getParcelableExtra("destino");
		this.dist = datosEntrantes.getStringExtra("distancia");
	}

	/**
	 * Fill Textviews titles.
	 */
	private void rellenaTitulos() {
		titulo1.setText(getText(R.string.current) + ":");
		titulo2.setText(getText(R.string.destination) + ":");
	}

	/**
	 * Get the addresses associated to LatLng points and fill the Textviews.
	 */
	private void rellenaDirecciones() {
		try {
			direccion1 = new GetAddresTask().execute(origen, datos1).get();
			direccion2 = new GetAddresTask().execute(destino, datos2).get();

			// Esto a lo mejor hay que ponerlo en el onPostExecute!
			this.datos1.setText(direccion1 + "\n\n(" + origen.latitude + ", "
					+ origen.longitude + ")");
			this.datos2.setText(direccion2 + "\n\n(" + destino.latitude + ", "
					+ destino.longitude + ")");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (CancellationException e) {
			// No hay conexión, se cancela la búsqueda de las direcciones
			// No se hace nada aquí, ya lo hace el hilo
		}
	}
	
	/**
	 * Makes easy to toast!
	 * 
	 * @param text
	 *            The string to show.
	 */
	private void toastIt(String text){
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG)
				.show();
	}

	/**
	 * Fill Textview distance.
	 */
	private void rellenaDistancia() {
		distancia.setText(getText(R.string.distance) + ": " + dist);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_info, menu);

		// Establecer el menu Compartir
		MenuItem shareItem = menu.findItem(R.id.action_social_share);
		ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat
				.getActionProvider(shareItem);
		Intent shareIntent = getDefaultShareIntent();
		if (verifyAppReceiveIntent(shareIntent))
			mShareActionProvider.setShareIntent(shareIntent);
		// else mostrar un Toast
		return true;
	}

	/**
	 * Creates an Intent to share data.
	 * 
	 * @return A new Intent to show different options to share data.
	 */
	private Intent getDefaultShareIntent() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, "Distance From Me");
		intent.putExtra(Intent.EXTRA_TEXT, "\n" + getText(R.string.from) + "\n"
				+ direccion1 + "\n\n" + getText(R.string.to) + "\n"
				+ direccion2 + "\n\n" + getText(R.string.space) + "\n" + dist);
		return intent;
	}
	
	/**
	 * Verify if there are applications that can handle the intent.
	 * 
	 * @param intent
	 *            The intent to verify.
	 * @return Returns true if there are applications; false, otherwise.
	 */
	private boolean verifyAppReceiveIntent(Intent intent){
		PackageManager packageManager = getPackageManager();
		List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
		if (activities.size() > 0)
			return true;
		return false;
	}

	// Este procedimiento ya no sirve para nada
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_social_share:
			return true;
		case R.id.refresh:
			menuItem = item;
			rellenaDirecciones();
			return true;
			// case R.id.menu_save:
			// guardarDatosBD();
			// return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// private void guardarDatosBD() {
	//
	// }

	/**
	 * A subclass of AsyncTask that calls getFromLocation() in the background.
	 */
	private class GetAddresTask extends AsyncTask<Object, Void, String> {

		Context mContext;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			this.mContext = getApplicationContext();

			empezarActualizacion();

			if (!isOnline()) {
				toastIt(getText(R.string.nonetwork).toString());

				terminarActualizacion();
				cancel(false);
			}
		}

		@Override
		protected String doInBackground(Object... params) {
			Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
			// Get the current location from the input parameter list
			LatLng loc = (LatLng) params[0];
			// Create a list to contain the result address
			List<Address> addresses = null;
			try {
				addresses = geocoder.getFromLocation(loc.latitude,
						loc.longitude, 1);
			} catch (IOException e1) {
				e1.printStackTrace();
				// return ("IO Exception trying to get address");
				return (getText(R.string.nolocation).toString());
			} catch (IllegalArgumentException e2) {
				// Error message to post in the log
				String errorString = "Illegal arguments "
						+ Double.toString(loc.latitude) + " , "
						+ Double.toString(loc.longitude)
						+ " passed to address service";
				e2.printStackTrace();
				return errorString;
			}
			// If the reverse geocode returned an address
			if (addresses != null && addresses.size() > 0) {
				// Get the first address
				Address address = addresses.get(0);
				// Format the first line of address (if available), city, and
				// country name.
				String addressText = String.format(
						"%s%s%s%s",
						// If there's a street address, add it
						address.getMaxAddressLineIndex() > 0 ? address
								.getAddressLine(0) + "\n" : "",
						// Añadimos también el código postal
						address.getPostalCode() != null ? address
								.getPostalCode() + " " : "",
						// Locality is usually a city
						address.getLocality() != null ? address.getLocality()
								+ "\n" : "",
						// The country of the address
						address.getCountryName());
				// Return the text
				return addressText;
				// If there aren't any addresses, post a message
			} else
				return getText(R.string.noaddress).toString();
		}

		@Override
		protected void onPostExecute(String result) {
			terminarActualizacion();

			super.onPostExecute(result);
		}

		/**
		 * Function to know the current network status.
		 * 
		 * @return Returns true if the device is connected to a network;
		 *         otherwise, returns false.
		 */
		private boolean isOnline() {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if (netInfo != null && netInfo.isConnected())
				return true;
			return false;
		}

		/**
		 * Change the appearance of the refresh button to a ProgressBar.
		 */
		private void empezarActualizacion() {
			if (menuItem != null) {
				MenuItemCompat.setActionView(menuItem,
						R.layout.actionbar_indeterminate_progress);
				MenuItemCompat.expandActionView(menuItem);
			}
		}

		/**
		 * Restore the refresh button to his normal appearance.
		 */
		private void terminarActualizacion() {
			if (menuItem != null) {
				MenuItemCompat.collapseActionView(menuItem);
				MenuItemCompat.setActionView(menuItem, null);
			}
		}
	}
}
