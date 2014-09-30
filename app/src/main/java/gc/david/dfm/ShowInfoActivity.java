package gc.david.dfm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import gc.david.dfm.db.DistancesDataSource;

/**
 * ShowInfoActivity shows information about the distance to the user.
 * 
 * @author David
 * 
 */
public class ShowInfoActivity extends ActionBarActivity {

	private LatLng source			= null;
	private LatLng destination		= null;

	private TextView title1			= null;
	private TextView title2			= null;
	private TextView data1			= null;
	private TextView data2			= null;
	private TextView distance		= null;

	private MenuItem menuItem		= null;
	private String address1			= "";
	private String address2			= "";
	private String dist				= null;

	private boolean savingDistance	= false;
	private String aliasHint		= "";
	private Dialog savingDialog		= null;
	private EditText textoAlias		= null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_info);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		getIntentData();
		
		title1 = (TextView) findViewById(R.id.titulo_datos1);
		title2 = (TextView) findViewById(R.id.titulo_datos2);
		fillTitles();
		
		data1 = (TextView) findViewById(R.id.datos1);
		data2 = (TextView) findViewById(R.id.datos2);
		
		if (savedInstanceState == null){
			fillAddresses();
		} else {
			this.address1 = savedInstanceState.getString("address1");
			this.address2 = savedInstanceState.getString("address2");
			
			this.data1.setText(address1 + "\n\n(" + source.latitude + ", "
					+ source.longitude + ")");
			this.data2.setText(address2 + "\n\n(" + destination.latitude + ", "
					+ destination.longitude + ")");
			
			// Este se modifica dos veces...
			this.dist = savedInstanceState.getString("distance");
			
			this.savingDistance = savedInstanceState.getBoolean("savingDistance");
			if (this.savingDistance){
				this.aliasHint = savedInstanceState.getString("aliasHint");
				saveDataToDB(aliasHint);
			}
		}

		distance = (TextView) findViewById(R.id.distancia);
		fillDistance();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putString("address1", this.address1);
		outState.putString("address2", this.address2);
		outState.putString("distance", this.dist);
		outState.putBoolean("savingDistance", savingDistance);
		if (this.savingDistance){
			outState.putString("aliasHint", textoAlias.getText().toString());
			if (savingDialog !=  null){
				savingDialog.dismiss();
				savingDialog = null;
			}
		}
	}

	/**
	 * Get data form the Intent.
	 */
	private void getIntentData() {
		Intent datosEntrantes = getIntent();
		this.source = (LatLng) datosEntrantes.getParcelableExtra("origen");
		this.destination = (LatLng) datosEntrantes.getParcelableExtra("destino");
		this.dist = datosEntrantes.getStringExtra("distancia");
	}

	/**
	 * Fill Textviews titles.
	 */
	private void fillTitles() {
		title1.setText(getText(R.string.current) + ":");
		title2.setText(getText(R.string.destination) + ":");
	}

	/**
	 * Get the addresses associated to LatLng points and fill the Textviews.
	 */
	private void fillAddresses() {
		try {
			address1 = new GetAddresTask().execute(source, data1).get();
			address2 = new GetAddresTask().execute(destination, data2).get();

			// Esto a lo mejor hay que ponerlo en el onPostExecute!
			this.data1.setText(address1 + "\n\n(" + source.latitude + ", "
					+ source.longitude + ")");
			this.data2.setText(address2 + "\n\n(" + destination.latitude + ", "
					+ destination.longitude + ")");
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
	 * Makes toasting easy!
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
	private void fillDistance() {
		distance.setText(getText(R.string.distance) + ": " + dist);
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
		intent.putExtra(Intent.EXTRA_SUBJECT, "Distance From Me (http://goo.gl/0IBHFN)");
		intent.putExtra(Intent.EXTRA_TEXT, "\nDistance From Me (http://goo.gl/0IBHFN)\n"
				+ getText(R.string.from) + "\n"
				+ address1 + "\n\n" + getText(R.string.to) + "\n"
				+ address2 + "\n\n" + getText(R.string.space) + "\n" + dist);
		return intent;
	}
	
	/**
	 * Verify if there are applications that can handle the intent.
	 * 
	 * @param intent
	 *            The intent to verify.
	 * @return Returns <code>true</code> if there are applications; <code>false</code>, otherwise.
	 */
	private boolean verifyAppReceiveIntent(Intent intent){
		final PackageManager packageManager = getPackageManager();
		final List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
		return activities.size() > 0;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_social_share:
			return true;
		case R.id.refresh:
			menuItem = item;
			fillAddresses();
			return true;
		case R.id.menu_save:
			saveDataToDB("");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * Saves the current data into the database.
	 * 
	 * @param text String text when orientation changes.
	 */
	private void saveDataToDB(String text) {
		this.savingDistance = true;
		// Pedir al usuario que introduzca un texto descriptivo
		AlertDialog.Builder builder = new AlertDialog.Builder(ShowInfoActivity.this);
		this.textoAlias = new EditText(getApplicationContext());
		this.textoAlias.setTextColor(Color.BLACK);
		this.textoAlias.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		this.textoAlias.setText(text);
		
		builder.setMessage(getText(R.string.alias_dialog_message))
				.setTitle(getText(R.string.alias_dialog_title))
				.setView(this.textoAlias)
				.setInverseBackgroundForced(false)
				.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						savingDistance = false;
					}
				})
				.setPositiveButton(getText(R.string.alias_dialog_accept), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						insertDataIntoDatabase(textoAlias.getText().toString());
						savingDistance = false;
					}
					
					/**
					 * Adds a new entry to the database with the current
					 * data and shows the user a message.
					 * 
					 * @param textoAlias
					 *            EditText which receives the alias of
					 *            the database entry.
					 */
					private void insertDataIntoDatabase(
							String textoAlias) {
						String alias = "";
						if (textoAlias.compareTo("") != 0)
							alias = textoAlias;
						
						DistancesDataSource dds = new DistancesDataSource(getApplicationContext());
						dds.open();
						// Si no introduce nada, poner "No title" o algo así
						dds.insert(alias, source, destination, dist);
						// Mostrar un mensaje de que se ha guardado correctamente
						if (!alias.equals(""))
							toastIt(getText(R.string.alias_dialog_toast_1) + alias + getText(R.string.alias_dialog_toast_2));
						else
							toastIt(getText(R.string.alias_dialog_toast_3).toString());
						dds.close();
					}
				});
		(savingDialog = builder.create()).show();
	}
	
	/**
	 * A subclass of AsyncTask that calls getFromLocation() in the background.
	 */
	private class GetAddresTask extends AsyncTask<Object, Void, String> {

		Context mContext;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			this.mContext = getApplicationContext();

			startUpdate();

			if (!isOnline()) {
				toastIt(getText(R.string.nonetwork).toString());

				endUpdate();
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
				return (getText(R.string.nolocation).toString());
			} catch (IllegalArgumentException e2) {
				// Error message to post in the log
				StringBuilder errorString = new StringBuilder();
				errorString.append("Illegal arguments ")
						.append(Double.toString(loc.latitude))
						.append(" , ")
						.append(Double.toString(loc.longitude))
						.append(" passed to address service");
				e2.printStackTrace();
				return errorString.toString();
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
			endUpdate();

			super.onPostExecute(result);
		}

		/**
		 * Function to know the current network status.
		 * 
		 * @return Returns <code>true</code> if the device is connected to a network;
		 *         otherwise, returns <code>false</code>.
		 */
		private boolean isOnline() {
			final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			final NetworkInfo netInfo = cm.getActiveNetworkInfo();
			return netInfo != null && netInfo.isConnected();
		}

		/**
		 * Change the appearance of the refresh button to a ProgressBar.
		 */
		private void startUpdate() {
			if (menuItem != null) {
				MenuItemCompat.setActionView(menuItem,
						R.layout.actionbar_indeterminate_progress);
				MenuItemCompat.expandActionView(menuItem);
			}
		}

		/**
		 * Restore the refresh button to his normal appearance.
		 */
		private void endUpdate() {
			if (menuItem != null) {
				MenuItemCompat.collapseActionView(menuItem);
				MenuItemCompat.setActionView(menuItem, null);
			}
		}
	}
}
