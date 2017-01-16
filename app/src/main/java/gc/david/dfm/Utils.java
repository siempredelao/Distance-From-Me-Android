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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

        String intentAsString = "";
        final Bundle bundle = intent.getExtras();

        if (bundle != null) {
            final Set<String> keys = bundle.keySet();
            intentAsString += "intent=[ ";
            for (final String key : keys) {
                intentAsString += key + "=" + bundle.get(key) + ", ";
            }
            intentAsString += " ]";
        } else {
            intentAsString = "intent with empty bundle";
        }
        return intentAsString;
    }

    public static String dumpBundleToString(Bundle bundle) {
        if (bundle == null) {
            return "bundle is null";
        }
        return bundle.toString();
    }

    /**
     * Converts the InputStream with the retrieved data to String.
     *
     * @param inputStream The input stream.
     * @return The InputStream converted to String.
     * @throws IOException
     */
    public static String convertInputStreamToString(final InputStream inputStream) throws IOException {
        DFMLogger.logMessage(TAG, "convertInputStreamToString");

        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        StringBuilder result = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            result.append(line);
        }

        inputStream.close();
        bufferedReader.close();
        return result.toString();
    }

    /**
     * Returns the distance between start and end positions normalized by device
     * locale.
     *
     * @param coordinates position list.
     * @return The distance in metres.
     */
    public static double calculateDistanceInMetres(final List<LatLng> coordinates) {
        DFMLogger.logMessage(TAG, "calculateDistance");

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
        DFMLogger.logMessage(TAG, "convertPositionListToLatLngList");

        final List<LatLng> result = new ArrayList<>();
        for (final Position position : positionList) {
            result.add(new LatLng(position.getLatitude(), position.getLongitude()));
        }
        return result;
    }
}
