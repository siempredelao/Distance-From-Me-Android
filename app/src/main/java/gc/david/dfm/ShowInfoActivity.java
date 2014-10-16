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

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import gc.david.dfm.db.DistancesDataSource;

import static gc.david.dfm.Utils.isOnline;
import static gc.david.dfm.Utils.toastIt;

/**
 * ShowInfoActivity shows information about the distance to the user.
 * 
 * @author David
 * 
 */
public class ShowInfoActivity extends ActionBarActivity {

	private LatLng originLatLng					= null;
	private LatLng destinationLatLng			= null;
	private TextView tvHeaderOriginAddress		= null;
	private TextView tvHeaderDestinationAddress	= null;

	private TextView tvOriginAddress			= null;
	private TextView tvDestinationAddress		= null;
	private TextView tvDistance					= null;
	private MenuItem menuItem					= null;
	private String originAddress				= "";

	private String destinationAddress			= "";
	private String distance						= null;
	private boolean wasSavingWhenOrientationChanged = false;
	private Dialog savingInDBDialog				= null;

	private EditText etAlias					= null;
	private final String originAddressKey = "originAddress";
	private final String destinationAddressKey = "destinationAddress";
	private final String distanceKey = "distance";
	private final String wasSavingWhenOrientationChangedKey = "wasSavingWhenOrientationChanged";
	private final String aliasHintKey = "aliasHint";
	public static final String originExtraKeyName = "origen";
	public static final String destinationExtraKeyName = "destino";
	public static final String distanceExtraKeyName = "distancia";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_info);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		getIntentData();

		tvHeaderOriginAddress = (TextView) findViewById(R.id.titulo_datos1);
		tvHeaderDestinationAddress = (TextView) findViewById(R.id.titulo_datos2);
		fillTitlesHeaders();

		tvOriginAddress = (TextView) findViewById(R.id.datos1);
		tvDestinationAddress = (TextView) findViewById(R.id.datos2);

		if (savedInstanceState == null){
			fillAddressesInfo();
		} else {
			originAddress = savedInstanceState.getString(originAddressKey);
			destinationAddress = savedInstanceState.getString(destinationAddressKey);

			tvOriginAddress.setText(originAddress + "\n\n(" + originLatLng.latitude + ", "
					+ originLatLng.longitude + ")");
			tvDestinationAddress.setText(destinationAddress + "\n\n(" + destinationLatLng.latitude + ", "
					+ destinationLatLng.longitude + ")");

			// Este se modifica dos veces...
			distance = savedInstanceState.getString(distanceKey);

			wasSavingWhenOrientationChanged = savedInstanceState.getBoolean(wasSavingWhenOrientationChangedKey);
			if (wasSavingWhenOrientationChanged) {
				final String aliasHint = savedInstanceState.getString(aliasHintKey);
				saveDataToDB(aliasHint);
			}
		}

		tvDistance = (TextView) findViewById(R.id.distancia);
		fillDistanceInfo();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putString(originAddressKey, originAddress);
		outState.putString(destinationAddressKey, destinationAddress);
		outState.putString(distanceKey, distance);
		outState.putBoolean(wasSavingWhenOrientationChangedKey, wasSavingWhenOrientationChanged);
		if (wasSavingWhenOrientationChanged) {
			outState.putString(aliasHintKey, etAlias.getText().toString());
			if (savingInDBDialog != null) {
				savingInDBDialog.dismiss();
				savingInDBDialog = null;
			}
		}
	}

	/**
	 * Get data form the Intent.
	 */
	private void getIntentData() {
		final Intent inputDataIntent = getIntent();
		originLatLng = (LatLng) inputDataIntent.getParcelableExtra(originExtraKeyName);
		destinationLatLng = (LatLng) inputDataIntent.getParcelableExtra(destinationExtraKeyName);
		distance = inputDataIntent.getStringExtra(distanceExtraKeyName);
	}

	/**
	 * Fill Textviews titles.
	 */
	private void fillTitlesHeaders() {
		tvHeaderOriginAddress.setText(getText(R.string.current) + ":");
		tvHeaderDestinationAddress.setText(getText(R.string.destination) + ":");
	}

	/**
	 * Get the addresses associated to LatLng points and fill the Textviews.
	 */
	private void fillAddressesInfo() {
		try {
			originAddress = new GetAddressTask().execute(originLatLng, tvOriginAddress).get();
			destinationAddress = new GetAddressTask().execute(destinationLatLng, tvDestinationAddress).get();

			// Esto a lo mejor hay que ponerlo en el onPostExecute!
			tvOriginAddress.setText(originAddress + "\n\n(" + originLatLng.latitude + ", "
					+ originLatLng.longitude + ")");
			tvDestinationAddress.setText(destinationAddress + "\n\n(" + destinationLatLng.latitude + ", "
					+ destinationLatLng.longitude + ")");
		} catch (final InterruptedException e) {
			e.printStackTrace();
		} catch (final ExecutionException e) {
			e.printStackTrace();
		} catch (final CancellationException e) {
			// No hay conexión, se cancela la búsqueda de las direcciones
			// No se hace nada aquí, ya lo hace el hilo
		}
	}
	
	/**
	 * Fill Textview tvDistance.
	 */
	private void fillDistanceInfo() {
		tvDistance.setText(getText(R.string.distance) + ": " + distance);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_info, menu);

		// Establecer el menu Compartir
		final MenuItem shareItem = menu.findItem(R.id.action_social_share);
		final ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat
				.getActionProvider(shareItem);
		final Intent shareDistanceIntent = createDefaultShareIntent();
		if (verifyAppReceiveIntent(shareDistanceIntent)) {
			mShareActionProvider.setShareIntent(shareDistanceIntent);
		}
		// else mostrar un Toast
		return true;
	}

	/**
	 * Creates an Intent to share data.
	 * 
	 * @return A new Intent to show different options to share data.
	 */
	private Intent createDefaultShareIntent() {
		final Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Distance From Me (http://goo.gl/0IBHFN)");
		shareIntent.putExtra(Intent.EXTRA_TEXT, "\nDistance From Me (http://goo.gl/0IBHFN)\n"
				+ getText(R.string.from) + "\n"
				+ originAddress + "\n\n" + getText(R.string.to) + "\n"
				+ destinationAddress + "\n\n" + getText(R.string.space) + "\n" + distance);
		return shareIntent;
	}
	
	/**
	 * Verify if there are applications that can handle the intent.
	 * 
	 * @param intent
	 *            The intent to verify.
	 * @return Returns <code>true</code> if there are applications; <code>false</code>, otherwise.
	 */
	private boolean verifyAppReceiveIntent(final Intent intent){
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
			fillAddressesInfo();
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
	 * @param defaultText String text when orientation changes.
	 */
	private void saveDataToDB(final String defaultText) {
		wasSavingWhenOrientationChanged = true;
		// Pedir al usuario que introduzca un texto descriptivo
		final AlertDialog.Builder builder = new AlertDialog.Builder(ShowInfoActivity.this);
		etAlias = new EditText(getApplicationContext());
		etAlias.setTextColor(Color.BLACK);
		etAlias.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		etAlias.setText(defaultText);
		
		builder.setMessage(getText(R.string.alias_dialog_message))
				.setTitle(getText(R.string.alias_dialog_title))
				.setView(etAlias)
				.setInverseBackgroundForced(false)
				.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						wasSavingWhenOrientationChanged = false;
					}
				})
				.setPositiveButton(getText(R.string.alias_dialog_accept), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						insertDataIntoDatabase(etAlias.getText().toString());
						wasSavingWhenOrientationChanged = false;
					}
					
					/**
					 * Adds a new entry to the database with the current
					 * data and shows the user a message.
					 * 
					 * @param alias Alias written by the user.
					 */
					private void insertDataIntoDatabase(final String alias) {
						String aliasToSave = "";
						if (alias.compareTo("") != 0) {
							aliasToSave = alias;
						}
						// TODO hacer esto en segundo plano
						final DistancesDataSource distancesDataSource = new DistancesDataSource(getApplicationContext());
						distancesDataSource.open();
						// Si no introduce nada, poner "No title" o algo así
						distancesDataSource.insert(aliasToSave, originLatLng, destinationLatLng, distance);
						// Mostrar un mensaje de que se ha guardado correctamente
						if (!aliasToSave.equals("")) {
							toastIt(getText(R.string.alias_dialog_toast_1) + aliasToSave + getText(R.string.alias_dialog_toast_2), getApplicationContext());
						} else {
							toastIt(getText(R.string.alias_dialog_toast_3), getApplicationContext());
						}
						distancesDataSource.close();
					}
				});
		(savingInDBDialog = builder.create()).show();
	}
	
	/**
	 * A subclass of AsyncTask that calls getFromLocation() in the background.
	 */
	private class GetAddressTask extends AsyncTask<Object, Void, String> {

		private Context context;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			this.context = getApplicationContext();

			startUpdate();

			if (!isOnline(context)) {
				toastIt(getText(R.string.nonetwork), context);

				endUpdate();
				cancel(false);
			}
		}

		@Override
		protected String doInBackground(Object... params) {
			final Geocoder geocoder = new Geocoder(context, Locale.getDefault());
			// Get the current location from the input parameter list
			final LatLng currentLocation = (LatLng) params[0];
			// Create a list to contain the result address
			List<Address> addresses = null;
			try {
				addresses = geocoder.getFromLocation(currentLocation.latitude,
						currentLocation.longitude, 1);
			} catch (final IOException e1) {
				e1.printStackTrace();
				return (getText(R.string.nolocation).toString());
			} catch (final IllegalArgumentException e2) {
				// Error message to post in the log
				final String errorString = "Illegal arguments "
						+ Double.toString(currentLocation.latitude)
						+ " , "
						+ Double.toString(currentLocation.longitude)
						+ " passed to address service";
				e2.printStackTrace();
				return errorString;
			}
			// If the reverse geocode returned an address
			if (addresses != null && addresses.size() > 0) {
				// Get the first address
				final Address address = addresses.get(0);
				// Format the first line of address (if available), city, and
				// country name.
				return String.format(
						"%s%s%s%s",
						// If there's a street address, add it
						address.getMaxAddressLineIndex() > 0 ?
								address.getAddressLine(0) + "\n" : "",
						// Añadimos también el código postal
						address.getPostalCode() != null ?
								address.getPostalCode() + " " : "",
						// Locality is usually a city
						address.getLocality() != null ?
								address.getLocality() + "\n" : "",
						// The country of the address
						address.getCountryName());
			} else {
				// If there aren't any addresses, post a message
				return getText(R.string.noaddress).toString();
			}
		}

		@Override
		protected void onPostExecute(String result) {
			endUpdate();

			super.onPostExecute(result);
		}

		/**
		 * Change the appearance of the refresh button to a ProgressBar.
		 */
		private void startUpdate() {
			if (menuItem != null) {
				MenuItemCompat.setActionView(menuItem, R.layout.actionbar_indeterminate_progress);
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
