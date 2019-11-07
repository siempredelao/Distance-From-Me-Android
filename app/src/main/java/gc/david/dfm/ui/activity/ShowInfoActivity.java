/*
 * Copyright (c) 2018 David Aguiar Gonzalez
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

package gc.david.dfm.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import gc.david.dfm.ConnectionManager;
import gc.david.dfm.DFMApplication;
import gc.david.dfm.R;
import gc.david.dfm.Utils;
import gc.david.dfm.address.domain.GetAddressNameByCoordinatesUseCase;
import gc.david.dfm.dagger.DaggerShowInfoComponent;
import gc.david.dfm.dagger.RootModule;
import gc.david.dfm.dagger.ShowInfoModule;
import gc.david.dfm.dagger.StorageModule;
import gc.david.dfm.deviceinfo.PackageManager;
import gc.david.dfm.distance.domain.InsertDistanceUseCase;
import gc.david.dfm.logger.DFMLogger;
import gc.david.dfm.showinfo.presentation.ShowInfo;
import gc.david.dfm.showinfo.presentation.ShowInfoPresenter;

import static butterknife.ButterKnife.bind;

public class ShowInfoActivity extends AppCompatActivity implements ShowInfo.View {

    private static final String TAG = ShowInfoActivity.class.getSimpleName();

    private static final String POSITIONS_LIST_EXTRA_KEY       = "positionsList";
    private static final String DISTANCE_EXTRA_KEY             = "distance";
    private static final String ORIGIN_ADDRESS_STATE_KEY       = "originAddressState";
    private static final String DESTINATION_ADDRESS_STATE_KEY  = "destinationAddressState";
    private static final String DISTANCE_STATE_KEY             = "distanceState";
    private static final String WAS_SAVING_STATE_KEY           = "wasSavingState";
    private static final String DISTANCE_DIALOG_NAME_STATE_KEY = "distanceDialogName";

    @BindView(R.id.showinfo_activity_origin_address_textview)
    protected TextView tvOriginAddress;
    @BindView(R.id.showinfo_activity_destination_address_textview)
    protected TextView tvDestinationAddress;
    @BindView(R.id.showinfo_activity_distance_textview)
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
    @Inject
    protected GetAddressNameByCoordinatesUseCase getOriginAddressNameByCoordinatesUseCase;
    @Inject
    protected GetAddressNameByCoordinatesUseCase getDestinationAddressNameByCoordinatesUseCase;

    private MenuItem           refreshMenuItem;
    private List<LatLng>       positionsList;
    private String             distance;
    private Dialog             savingInDBDialog;
    private EditText           etAlias;
    private ShowInfo.Presenter showInfoPresenter;

    private String  originAddress                   = "";
    private String  destinationAddress              = "";
    private boolean wasSavingWhenOrientationChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DFMLogger.INSTANCE.logMessage(TAG, "onCreate savedInstanceState=" + Utils.INSTANCE.dumpBundleToString(savedInstanceState));

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

        // TODO: 06.02.17 store presenter in singleton Map, Loader,... to restore after config change
        showInfoPresenter = new ShowInfoPresenter(this,
                                                  getOriginAddressNameByCoordinatesUseCase,
                                                  getDestinationAddressNameByCoordinatesUseCase,
                                                  insertDistanceUseCase,
                                                  connectionManager);

        getIntentData();

        if (savedInstanceState == null) {
            DFMLogger.INSTANCE.logMessage(TAG, "onCreate savedInstanceState null, filling addresses info");

            fillAddressesInfo();
        } else {
            restorePreviousState(savedInstanceState);
        }

        fillDistanceInfo();
    }

    private void restorePreviousState(final Bundle savedInstanceState) {
        originAddress = savedInstanceState.getString(ORIGIN_ADDRESS_STATE_KEY);
        destinationAddress = savedInstanceState.getString(DESTINATION_ADDRESS_STATE_KEY);

        tvOriginAddress.setText(formatAddress(originAddress,
                                              positionsList.get(0).latitude,
                                              positionsList.get(0).longitude));
        tvDestinationAddress.setText(formatAddress(destinationAddress,
                                                   positionsList.get(positionsList.size() - 1).latitude,
                                                   positionsList.get(positionsList.size() - 1).longitude));
        distance = savedInstanceState.getString(DISTANCE_STATE_KEY);

        wasSavingWhenOrientationChanged = savedInstanceState.getBoolean(WAS_SAVING_STATE_KEY);
        if (wasSavingWhenOrientationChanged) {
            final String aliasHint = savedInstanceState.getString(DISTANCE_DIALOG_NAME_STATE_KEY);
            saveDataToDB(aliasHint);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        DFMLogger.INSTANCE.logMessage(TAG, "onSaveInstanceState outState=" + Utils.INSTANCE.dumpBundleToString(outState));

        super.onSaveInstanceState(outState);

        outState.putString(ORIGIN_ADDRESS_STATE_KEY, originAddress);
        outState.putString(DESTINATION_ADDRESS_STATE_KEY, destinationAddress);
        outState.putString(DISTANCE_STATE_KEY, distance);

        if (wasSavingWhenOrientationChanged) {
            outState.putBoolean(WAS_SAVING_STATE_KEY, wasSavingWhenOrientationChanged);
            outState.putString(DISTANCE_DIALOG_NAME_STATE_KEY, etAlias.getText().toString());

            if (savingInDBDialog != null) {
                savingInDBDialog.dismiss();
                savingInDBDialog = null;
            }
        }
    }

    private void getIntentData() {
        DFMLogger.INSTANCE.logMessage(TAG, "getIntentData");

        final Intent inputDataIntent = getIntent();
        positionsList = inputDataIntent.getParcelableArrayListExtra(POSITIONS_LIST_EXTRA_KEY);
        distance = inputDataIntent.getStringExtra(DISTANCE_EXTRA_KEY);
    }

    private void fillAddressesInfo() {
        DFMLogger.INSTANCE.logMessage(TAG, "fillAddressesInfo");

        showInfoPresenter.searchPositionByCoordinates(positionsList.get(0),
                                                      positionsList.get(positionsList.size() - 1));
    }

    private String formatAddress(final String address, final double latitude, final double longitude) {
        return String.format(Locale.getDefault(), "%s\n\n(%f,%f)", address, latitude, longitude);
    }

    private void fillDistanceInfo() {
        DFMLogger.INSTANCE.logMessage(TAG, "fillDistanceInfo");

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
        wasSavingWhenOrientationChanged = true;

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
                       showInfoPresenter.saveDistance(etAlias.getText().toString(), distance, positionsList);
                       wasSavingWhenOrientationChanged = false;
                   }
               });
        (savingInDBDialog = builder.create()).show();
    }

    @Override
    public void setPresenter(final ShowInfo.Presenter presenter) {
        this.showInfoPresenter = presenter;
    }

    @Override
    public void showNoInternetError() {
        Utils.INSTANCE.toastIt(getString(R.string.toast_network_problems), getApplicationContext());
    }

    @Override
    public void showProgress() {
        if (refreshMenuItem != null) {
            refreshMenuItem.setActionView(R.layout.actionbar_indeterminate_progress);
        }
    }

    @Override
    public void hideProgress() {
        if (refreshMenuItem != null) {
            refreshMenuItem.setActionView(null);
        }
    }

    @Override
    public void setAddress(final String address, final boolean isOrigin) {
        if (isOrigin) {
            originAddress = address;
            tvOriginAddress.setText(formatAddress(originAddress,
                                                  positionsList.get(0).latitude,
                                                  positionsList.get(0).longitude));
        } else {
            destinationAddress = address;
            tvDestinationAddress.setText(formatAddress(destinationAddress,
                                                       positionsList.get(positionsList.size() - 1).latitude,
                                                       positionsList.get(positionsList.size() - 1).longitude));
        }
    }

    @Override
    public void showNoMatchesMessage(final boolean isOrigin) {
        if (isOrigin) {
            tvOriginAddress.setText(R.string.error_no_address_found_message);
        } else {
            tvDestinationAddress.setText(R.string.error_no_address_found_message);
        }
    }

    @Override
    public void showError(final String errorMessage, final boolean isOrigin) {
        if (isOrigin) {
            tvOriginAddress.setText(R.string.toast_no_location_found);
        } else {
            tvDestinationAddress.setText(R.string.toast_no_location_found);
        }
        DFMLogger.INSTANCE.logException(new Exception(errorMessage));
    }

    @Override
    public void showSuccessfulSave() {
        Utils.INSTANCE.toastIt(R.string.alias_dialog_no_name_toast, appContext);
    }

    @Override
    public void showSuccessfulSaveWithName(final String distanceName) {
        Utils.INSTANCE.toastIt(getString(R.string.alias_dialog_with_name_toast, distanceName), appContext);
    }

    @Override
    public void showFailedSave() {
        Utils.INSTANCE.toastIt("Unable to save distance. Try again later.", appContext);
        DFMLogger.INSTANCE.logException(new Exception("Unable to insert distance into database."));
    }

    public static void open(final Activity activity, final List<LatLng> coordinates, final String distanceAsText) {
        final Intent openShowInfoActivityIntent = new Intent(activity, ShowInfoActivity.class);
        openShowInfoActivityIntent.putParcelableArrayListExtra(POSITIONS_LIST_EXTRA_KEY,
                                                               new ArrayList<Parcelable>(coordinates));
        openShowInfoActivityIntent.putExtra(DISTANCE_EXTRA_KEY, distanceAsText);
        activity.startActivity(openShowInfoActivityIntent);
    }
}
