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

package gc.david.dfm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gc.david.dfm.logger.DFMLogger;
import gc.david.dfm.map.Haversine;
import gc.david.dfm.model.Position;

/**
 * Created by David on 15/10/2014.
 */
public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    public static void toastIt(final String charSequence, final Context context) {
        DFMLogger.logMessage(TAG, "toastIt message=" + charSequence);

        Toast.makeText(context, charSequence, Toast.LENGTH_LONG).show();
    }

    public static void toastIt(final @StringRes int stringRes, final Context context) {
        DFMLogger.logMessage(TAG, "toastIt message=" + context.getString(stringRes));

        Toast.makeText(context, stringRes, Toast.LENGTH_LONG).show();
    }

    public static void showAlertDialog(final String action,
                                       final @StringRes int title,
                                       final @StringRes int message,
                                       final @StringRes int positiveButton,
                                       final @StringRes int negativeButton,
                                       final Activity activity) {
        DFMLogger.logMessage(TAG, "showAlertDialog");

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title)
               .setMessage(message)
               .setCancelable(false)
               .setPositiveButton(positiveButton,
                                  new DialogInterface.OnClickListener() {
                                      public void onClick(DialogInterface dialog, int id) {
                                          final Intent optionsIntent = new Intent(action);
                                          activity.startActivity(optionsIntent);
                                      }
                                  })
               .setNegativeButton(negativeButton,
                                  new DialogInterface.OnClickListener() {
                                      public void onClick(DialogInterface dialog, int id) {
                                          dialog.cancel();
                                      }
                                  })
               .create()
               .show();
    }

    public static String dumpIntentToString(final Intent intent) {
        if (intent == null) {
            return "intent is null";
        }

        StringBuilder intentAsString = new StringBuilder();
        final Bundle bundle = intent.getExtras();

        if (bundle != null) {
            final Set<String> keys = bundle.keySet();
            intentAsString.append("intent=[ ");
            for (final String key : keys) {
                intentAsString.append(key).append("=").append(bundle.get(key)).append(", ");
            }
            intentAsString.append(" ]");
        } else {
            intentAsString = new StringBuilder("intent with empty bundle");
        }
        return intentAsString.toString();
    }

    public static String dumpBundleToString(Bundle bundle) {
        if (bundle == null) {
            return "bundle is null";
        }
        return bundle.toString();
    }

    public static double calculateDistanceInMetres(final List<LatLng> coordinates) {
        double distanceInMetres = 0D;
        for (int i = 0; i < coordinates.size() - 1; i++) {
            distanceInMetres += Haversine.getDistance(coordinates.get(i).latitude,
                                                      coordinates.get(i).longitude,
                                                      coordinates.get(i + 1).latitude,
                                                      coordinates.get(i + 1).longitude);
        }
        return distanceInMetres;
    }

    public static List<LatLng> convertPositionListToLatLngList(final List<Position> positionList) {
        final List<LatLng> result = new ArrayList<>();
        for (final Position position : positionList) {
            result.add(new LatLng(position.getLatitude(), position.getLongitude()));
        }
        return result;
    }

    public static boolean isReleaseBuild() {
        return "release".equals(BuildConfig.BUILD_TYPE);
    }
}
