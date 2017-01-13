package gc.david.dfm.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import butterknife.BindView;
import gc.david.dfm.DFMApplication;
import gc.david.dfm.PackageManager;
import gc.david.dfm.R;
import gc.david.dfm.Utils;
import gc.david.dfm.dagger.DaggerRootComponent;
import gc.david.dfm.dagger.RootModule;
import gc.david.dfm.logger.DFMLogger;
import gc.david.dfm.model.DaoSession;
import gc.david.dfm.model.Distance;
import gc.david.dfm.model.Position;

import static butterknife.ButterKnife.bind;
import static gc.david.dfm.Utils.isOnline;
import static gc.david.dfm.Utils.toastIt;

public class ShowInfoActivity extends AppCompatActivity {

    private static final String TAG = ShowInfoActivity.class.getSimpleName();

    public static final  String POSITIONS_LIST_EXTRA_KEY_NAME           = "positionsList";
    public static final  String DISTANCE_EXTRA_KEY_NAME                 = "distancia";
    private static final String ORIGIN_ADDRESS_KEY                      = "originAddress";
    private static final String DESTINATION_ADDRESS_KEY                 = "destinationAddress";
    private static final String DISTANCE_KEY                            = "distance";
    private static final String WAS_SAVING_WHEN_ORIENTATION_CHANGED_KEY = "wasSavingWhenOrientationChanged";
    private static final String ALIAS_HINT_KEY                          = "aliasHint";

    @BindView(R.id.datos1)
    protected TextView tvOriginAddress;
    @BindView(R.id.datos2)
    protected TextView tvDestinationAddress;
    @BindView(R.id.distancia)
    protected TextView tvDistance;
    @BindView(R.id.tbMain)
    protected Toolbar  tbMain;

    @Inject
    protected DaoSession     daoSession;
    @Inject
    protected Context        appContext;
    @Inject
    protected PackageManager packageManager;

    private MenuItem     refreshMenuItem;
    private List<LatLng> positionsList;
    private String       distance;
    private Dialog       savingInDBDialog;
    private EditText     etAlias;

    private String  originAddress                   = "";
    private String  destinationAddress              = "";
    private boolean wasSavingWhenOrientationChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DFMLogger.logMessage(TAG, "onCreate savedInstanceState=" + Utils.dumpBundleToString(savedInstanceState));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_info);
        DaggerRootComponent.builder()
                           .rootModule(new RootModule((DFMApplication) getApplication()))
                           .build()
                           .inject(this);
        bind(this);

        setSupportActionBar(tbMain);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getIntentData();

        if (savedInstanceState == null) {
            DFMLogger.logMessage(TAG, "onCreate savedInstanceState null, filling addresses info");
            fillAddressesInfo();
        } else {
            originAddress = savedInstanceState.getString(ORIGIN_ADDRESS_KEY);
            destinationAddress = savedInstanceState.getString(DESTINATION_ADDRESS_KEY);

            tvOriginAddress.setText(formatAddress(originAddress,
                                                  positionsList.get(0).latitude,
                                                  positionsList.get(0).longitude));

            tvDestinationAddress.setText(formatAddress(destinationAddress,
                                                       positionsList.get(positionsList.size() - 1).latitude,
                                                       positionsList.get(positionsList.size() - 1).longitude));

            // Este se modifica dos veces...
            distance = savedInstanceState.getString(DISTANCE_KEY);

            wasSavingWhenOrientationChanged = savedInstanceState.getBoolean(WAS_SAVING_WHEN_ORIENTATION_CHANGED_KEY);
            if (wasSavingWhenOrientationChanged) {
                final String aliasHint = savedInstanceState.getString(ALIAS_HINT_KEY);
                saveDataToDB(aliasHint);
            }
        }

        fillDistanceInfo();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        DFMLogger.logMessage(TAG, "onSaveInstanceState outState=" + Utils.dumpBundleToString(outState));

        super.onSaveInstanceState(outState);

        outState.putString(ORIGIN_ADDRESS_KEY, originAddress);
        outState.putString(DESTINATION_ADDRESS_KEY, destinationAddress);
        outState.putString(DISTANCE_KEY, distance);
        outState.putBoolean(WAS_SAVING_WHEN_ORIENTATION_CHANGED_KEY, wasSavingWhenOrientationChanged);
        if (wasSavingWhenOrientationChanged) {
            outState.putString(ALIAS_HINT_KEY, etAlias.getText().toString());
            if (savingInDBDialog != null) {
                savingInDBDialog.dismiss();
                savingInDBDialog = null;
            }
        }
    }

    private void getIntentData() {
        DFMLogger.logMessage(TAG, "getIntentData");

        final Intent inputDataIntent = getIntent();
        positionsList = inputDataIntent.getParcelableArrayListExtra(POSITIONS_LIST_EXTRA_KEY_NAME);
        distance = inputDataIntent.getStringExtra(DISTANCE_EXTRA_KEY_NAME);
    }

    private void fillAddressesInfo() {
        DFMLogger.logMessage(TAG, "fillAddressesInfo");

        try {
            originAddress = new GetAddressTask().execute(positionsList.get(0), tvOriginAddress).get();
            destinationAddress = new GetAddressTask().execute(positionsList.get(positionsList.size() - 1),
                                                              tvDestinationAddress).get();

            tvOriginAddress.setText(formatAddress(originAddress,
                                                  positionsList.get(0).latitude,
                                                  positionsList.get(0).longitude));
            tvDestinationAddress.setText(formatAddress(destinationAddress,
                                                       positionsList.get(positionsList.size() - 1).latitude,
                                                       positionsList.get(positionsList.size() - 1).longitude));
        } catch (final InterruptedException | ExecutionException e) {
            e.printStackTrace();
            DFMLogger.logException(e);
        } catch (final CancellationException e) {
            DFMLogger.logException(e);
            // No hay conexión, se cancela la búsqueda de las direcciones
            // No se hace nada aquí, ya lo hace el hilo
        }
    }

    private String formatAddress(final String address, final double latitude, final double longitude) {
        return String.format(Locale.getDefault(), "%s\n\n(%f,%f)", address, latitude, longitude);
    }

    private void fillDistanceInfo() {
        DFMLogger.logMessage(TAG, "fillDistanceInfo");

        tvDistance.setText(getString(R.string.info_distance_title, distance));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        DFMLogger.logMessage(TAG, "onCreateOptionsMenu");

        getMenuInflater().inflate(R.menu.show_info, menu);

        final MenuItem shareItem = menu.findItem(R.id.action_social_share);
        final ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        final Intent shareDistanceIntent = createDefaultShareIntent();
        if (packageManager.isThereAnyActivityForIntent(shareDistanceIntent)) {
            shareActionProvider.setShareIntent(shareDistanceIntent);
        }
        refreshMenuItem = menu.findItem(R.id.refresh);
        return true;
    }

    private Intent createDefaultShareIntent() {
        DFMLogger.logMessage(TAG, "createDefaultShareIntent");

        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Distance From Me (http://goo.gl/0IBHFN)");

        final String extraText = String.format("\nDistance From Me (http://goo.gl/0IBHFN)\n%s\n%s\n\n%s\n%s\n\n%s\n%s",
                                               getString(R.string.share_distance_from_message),
                                               originAddress,
                                               getString(R.string.share_distance_to_message),
                                               destinationAddress,
                                               getString(R.string.share_distance_there_are_message),
                                               distance);
        shareIntent.putExtra(Intent.EXTRA_TEXT, extraText);
        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DFMLogger.logMessage(TAG, "onOptionsItemSelected item=" + item.getItemId());

        switch (item.getItemId()) {
            case R.id.action_social_share:
                return true;
            case R.id.refresh:
                fillAddressesInfo();
                return true;
            case R.id.menu_save:
                saveDataToDB("");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveDataToDB(final String defaultText) {
        DFMLogger.logMessage(TAG, "saveDataToDB defaultText=" + defaultText);

        wasSavingWhenOrientationChanged = true;
        // Pedir al usuario que introduzca un texto descriptivo
        final AlertDialog.Builder builder = new AlertDialog.Builder(ShowInfoActivity.this);
        etAlias = new EditText(appContext);
        etAlias.setTextColor(Color.BLACK);
        etAlias.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        etAlias.setText(defaultText);

        builder.setMessage(getString(R.string.alias_dialog_message))
               .setTitle(getString(R.string.alias_dialog_title))
               .setView(etAlias)
               .setOnCancelListener(new OnCancelListener() {
                   @Override
                   public void onCancel(DialogInterface dialog) {
                       wasSavingWhenOrientationChanged = false;
                   }
               })
               .setPositiveButton(getString(R.string.alias_dialog_accept), new OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       insertDataIntoDatabase(etAlias.getText().toString());
                       wasSavingWhenOrientationChanged = false;
                   }

                   private void insertDataIntoDatabase(final String alias) {
                       DFMLogger.logMessage(TAG, "insertDataIntoDatabase");

                       String aliasToSave = "";
                       if (alias.compareTo("") != 0) {
                           aliasToSave = alias;
                       }
                       // TODO hacer esto en segundo plano
                       final Distance distance1 = new Distance(null, aliasToSave, distance, new Date());
                       final long distanceId = daoSession.insert(distance1);

                       for (LatLng positionAsLatLng : positionsList) {
                           final Position position = new Position(null,
                                                                  positionAsLatLng.latitude,
                                                                  positionAsLatLng.longitude,
                                                                  distanceId);
                           daoSession.insert(position);
                       }

                       // Mostrar un mensaje de que se ha guardado correctamente
                       if (!aliasToSave.equals("")) {
                           toastIt(getString(R.string.alias_dialog_with_name_toast, aliasToSave), appContext);
                       } else {
                           toastIt(getString(R.string.alias_dialog_no_name_toast), appContext);
                       }
                   }
               });
        (savingInDBDialog = builder.create()).show();
    }

    private class GetAddressTask extends AsyncTask<Object, Void, String> {

        private final String TAG = GetAddressTask.class.getSimpleName();

        private Context context;

        @Override
        protected void onPreExecute() {
            DFMLogger.logMessage(TAG, "onPreExecute");

            this.context = appContext;

            showRefreshSpinner();

            if (!isOnline(context)) {
                toastIt(getString(R.string.toast_network_problems), context);

                hideRefreshSpinner();
                cancel(false);
            }
        }

        @Override
        protected String doInBackground(Object... params) {
            DFMLogger.logMessage(TAG, "doInBackground");

            final Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            // Get the current location from the input parameter list
            final LatLng currentLocation = (LatLng) params[0];
            // Create a list to contain the result address
            List<Address> addresses;
            try {
                addresses = geocoder.getFromLocation(currentLocation.latitude,
                                                     currentLocation.longitude, 1);
            } catch (final IOException e1) {
                e1.printStackTrace();
                DFMLogger.logException(e1);
                return (getString(R.string.toast_no_location_found));
            } catch (final IllegalArgumentException e2) {
                // Error message to post in the log
                final String errorString = String.format("Illegal arguments %s.%s passed to address service",
                                                         Double.toString(currentLocation.latitude),
                                                         Double.toString(currentLocation.longitude));
                e2.printStackTrace();
                DFMLogger.logException(e2);
                return errorString;
            }
            // If the reverse geocode returned an address
            if (addresses != null && !addresses.isEmpty()) {
                // Get the first address
                final Address address = addresses.get(0);
                // Format the first line of address (if available), city, and
                // country name.
                return String.format("%s%s%s%s",
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
                return getString(R.string.error_no_address_found_message);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            DFMLogger.logMessage(TAG, "onPostExecute");

            hideRefreshSpinner();
        }

        private void showRefreshSpinner() {
            DFMLogger.logMessage(TAG, "showRefreshSpinner");

            if (refreshMenuItem != null) {
                MenuItemCompat.setActionView(refreshMenuItem, R.layout.actionbar_indeterminate_progress);
                MenuItemCompat.expandActionView(refreshMenuItem);
            }
        }

        private void hideRefreshSpinner() {
            DFMLogger.logMessage(TAG, "hideRefreshSpinner");

            if (refreshMenuItem != null) {
                MenuItemCompat.collapseActionView(refreshMenuItem);
                MenuItemCompat.setActionView(refreshMenuItem, null);
            }
        }
    }

    public static void open(final Activity activity, final List<LatLng> coordinates, final String distanceAsText) {
        final Intent showInfoActivityIntent = new Intent(activity, ShowInfoActivity.class);
        showInfoActivityIntent.putParcelableArrayListExtra(POSITIONS_LIST_EXTRA_KEY_NAME,
                                                           new ArrayList<Parcelable>(coordinates));
        showInfoActivityIntent.putExtra(DISTANCE_EXTRA_KEY_NAME, distanceAsText);
        activity.startActivity(showInfoActivityIntent);
    }
}
