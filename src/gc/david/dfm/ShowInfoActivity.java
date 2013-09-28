package gc.david.dfm;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.content.Intent;
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

public class ShowInfoActivity extends ActionBarActivity {

	private LatLng origen = null;
	private LatLng destino = null;

	private TextView datos1 = null;
	private TextView datos2 = null;

	private MenuItem menuItem;
	private String direccion1 = null;
	private String direccion2 = null;
	private String dist = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_info);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		Intent datosEntrantes = extraeDatosIntent();

		TextView titulo1 = (TextView) findViewById(R.id.titulo_datos1);
		TextView titulo2 = (TextView) findViewById(R.id.titulo_datos2);
		rellenaTitulos(titulo1, titulo2);

		datos1 = (TextView) findViewById(R.id.datos1);
		datos2 = (TextView) findViewById(R.id.datos2);
		rellenaDirecciones();

		dist = datosEntrantes.getStringExtra("distancia");
		TextView distancia = (TextView) findViewById(R.id.distancia);
		rellenaDistancia(distancia);
	}

	private Intent extraeDatosIntent() {
		Intent datosEntrantes = getIntent();
		this.origen = (LatLng) datosEntrantes.getParcelableExtra("origen");
		this.destino = (LatLng) datosEntrantes.getParcelableExtra("destino");
		return datosEntrantes;
	}

	private void rellenaTitulos(TextView titulo1, TextView titulo2) {
		titulo1.setText(getText(R.string.current) + ":");
		titulo2.setText(getText(R.string.destination) + ":");
	}

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
		} catch (CancellationException e){
			// No hay conexión, se cancela la búsqueda de las direcciones
			// No se hace nada aquí, ya lo hace el hilo
		}
	}

	private void rellenaDistancia(TextView distancia) {
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
		mShareActionProvider.setShareIntent(getDefaultShareIntent());

		return true;
	}

	private Intent getDefaultShareIntent() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, "Distance From Me");
		intent.putExtra(Intent.EXTRA_TEXT, "\n" + getText(R.string.from) + "\n"
				+ direccion1 + "\n\n" + getText(R.string.to) + "\n"
				+ direccion2 + "\n\n" + getText(R.string.space) + "\n" + dist);
		return intent;
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
//		case R.id.menu_save:
//			guardarDatosBD();
//			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

//	private void guardarDatosBD() {
//
//	}

	/**
	 * A subclass of AsyncTask that calls getFromLocation() in the background.
	 * The class definition has these generic types: Location - A Location
	 * object containing the current location. Void - indicates that progress
	 * units are not used String - An address passed to onPostExecute()
	 */
	private class GetAddresTask extends AsyncTask<Object, Void, String> {

		Context mContext;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			this.mContext = getApplicationContext();


			MenuItemCompat.setActionView(menuItem,
					R.layout.actionbar_indeterminate_progress);
			MenuItemCompat.expandActionView(menuItem);
			
			if (!isOnline()) {
				Toast.makeText(getApplicationContext(),
						getText(R.string.nonetwork), Toast.LENGTH_LONG).show();

				MenuItemCompat.collapseActionView(menuItem);
				MenuItemCompat.setActionView(menuItem, null);
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
			MenuItemCompat.collapseActionView(menuItem);
			MenuItemCompat.setActionView(menuItem, null);

			super.onPostExecute(result);
		}

		private boolean isOnline() {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if (netInfo != null && netInfo.isConnected())
				return true;
			return false;
		}
	}
}
