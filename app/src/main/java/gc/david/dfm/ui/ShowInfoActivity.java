/*
 * Copyright (c) 2017 David Aguiar Gonzalez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import android.text.TextUtils;
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
import gc.david.dfm.ConnectionManager;
import gc.david.dfm.DFMApplication;
import gc.david.dfm.deviceinfo.PackageManager;
import gc.david.dfm.R;
import gc.david.dfm.Utils;
import gc.david.dfm.dagger.DaggerShowInfoComponent;
import gc.david.dfm.dagger.RootModule;
import gc.david.dfm.dagger.ShowInfoModule;
import gc.david.dfm.dagger.StorageModule;
import gc.david.dfm.distance.domain.InsertDistanceUseCase;
import gc.david.dfm.logger.DFMLogger;
import gc.david.dfm.model.Distance;
import gc.david.dfm.model.Position;

import static butterknife.ButterKnife.bind;
import static gc.david.dfm.Utils.toastIt;

public class ShowInfoActivity extends AppCompatActivity {

    private static final String TAG = ShowInfoActivity.class.getSimpleName();

    private static final String POSITIONS_LIST_EXTRA_KEY                = "positionsList";
    private static final String DISTANCE_EXTRA_KEY                      = "distancia";
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
    protected Context               appContext;
    @Inject
    protected PackageManager        packageManager;
    @Inject
    protected ConnectionManager     connectionManager;
    @Inject
    protected InsertDistanceUseCase insertDistanceUseCase;

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
        DaggerShowInfoComponent.builder()
                               .rootModule(new RootModule((DFMApplication) getApplication()))
                               .storageModule(new StorageModule())
                               .showInfoModule(new ShowInfoModule())
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
        positionsList = inputDataIntent.getParcelableArrayListExtra(POSITIONS_LIST_EXTRA_KEY);
        distance = inputDataIntent.getStringExtra(DISTANCE_EXTRA_KEY);
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

                       // TODO: 16.01.17 move this to presenter
                       final Distance distance1 = new Distance();
                       distance1.setName(alias);
                       distance1.setDistance(distance);
                       distance1.setDate(new Date());

                       final List<Position> positionList = new ArrayList<>();
                       for (final LatLng positionLatLng : positionsList) {
                           final Position position = new Position();
                           position.setLatitude(positionLatLng.latitude);
                           position.setLongitude(positionLatLng.longitude);
                           positionList.add(position);
                       }

                       insertDistanceUseCase.execute(distance1, positionList, new InsertDistanceUseCase.Callback() {
                           @Override
                           public void onInsert() {
                               if (!TextUtils.isEmpty(alias)) {
                                   toastIt(getString(R.string.alias_dialog_with_name_toast, alias), appContext);
                               } else {
                                   toastIt(R.string.alias_dialog_no_name_toast, appContext);
                               }
                           }

                           @Override
                           public void onError() {
                               toastIt("Unable to save distance. Try again later.", appContext);
                               DFMLogger.logException(new Exception("Unable to insert distance into database."));
                           }
                       });
                   }
               });
        (savingInDBDialog = builder.create()).show();
    }

    private class GetAddressTask extends AsyncTask<Object, Void, String> {

        private final String TAG = GetAddressTask.class.getSimpleName();

        private Context context;

        @Override
        protected void onPreExecute() {
            this.context = appContext;

            showRefreshSpinner();

            if (!connectionManager.isOnline()) {
                toastIt(R.string.toast_network_problems, context);

                hideRefreshSpinner();
                cancel(false);
            }
        }

        @Override
        protected String doInBackground(Object... params) {
            DFMLogger.logMessage(TAG, "doInBackground");

            final Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            final LatLng currentLocation = (LatLng) params[0];
            List<Address> addresses;
            try {
                addresses = geocoder.getFromLocation(currentLocation.latitude,
                                                     currentLocation.longitude, 1);
            } catch (final IOException e1) {
                e1.printStackTrace();
                DFMLogger.logException(e1);
                return (getString(R.string.toast_no_location_found));
            } catch (final IllegalArgumentException e2) {
                final String errorString = String.format("Illegal arguments %s.%s passed to address service",
                                                         Double.toString(currentLocation.latitude),
                                                         Double.toString(currentLocation.longitude));
                e2.printStackTrace();
                DFMLogger.logException(e2);
                return errorString;
            }
            if (addresses != null && !addresses.isEmpty()) {
                final Address address = addresses.get(0);
                return String.format("%s%s%s%s",
                                     address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) + "\n" : "",
                                     address.getPostalCode() != null ? address.getPostalCode() + " " : "",
                                     address.getLocality() != null ? address.getLocality() + "\n" : "",
                                     address.getCountryName());
            } else {
                return getString(R.string.error_no_address_found_message);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            hideRefreshSpinner();
        }

        private void showRefreshSpinner() {
            if (refreshMenuItem != null) {
                MenuItemCompat.setActionView(refreshMenuItem, R.layout.actionbar_indeterminate_progress);
                MenuItemCompat.expandActionView(refreshMenuItem);
            }
        }

        private void hideRefreshSpinner() {
            if (refreshMenuItem != null) {
                MenuItemCompat.collapseActionView(refreshMenuItem);
                MenuItemCompat.setActionView(refreshMenuItem, null);
            }
        }
    }

    public static void open(final Activity activity, final List<LatLng> coordinates, final String distanceAsText) {
        final Intent openShowInfoActivityIntent = new Intent(activity, ShowInfoActivity.class);
        openShowInfoActivityIntent.putParcelableArrayListExtra(POSITIONS_LIST_EXTRA_KEY,
                                                               new ArrayList<Parcelable>(coordinates));
        openShowInfoActivityIntent.putExtra(DISTANCE_EXTRA_KEY, distanceAsText);
        activity.startActivity(openShowInfoActivityIntent);
    }
}
