package gc.david.dfm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Set;

import gc.david.dfm.logger.DFMLogger;

/**
 * Created by David on 15/10/2014.
 */
public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    /**
     * Makes toasting easy!
     *
     * @param charSequence The string to show.
     * @param context      Activity context.
     */
    public static void toastIt(final String charSequence, final Context context) {
        DFMLogger.logMessage(TAG, "toastIt");

        Toast.makeText(context, charSequence, Toast.LENGTH_LONG).show();
    }


    /**
     * Function to know the current network status.
     *
     * @return Returns <code>true</code> if the device is connected to a network;
     * otherwise, returns <code>false</code>.
     */
    public static boolean isOnline(final Context context) {
        DFMLogger.logMessage(TAG, "isOnline");

        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    /**
     * Shows an AlertDialog with a message, positive and negative button, and
     * executes an action if needed.
     *
     * @param action         Action to execute.
     * @param message        Message to show to the user.
     * @param positiveButton Positive button text.
     * @param negativeButton Negative button text.
     * @param activity       Activity which runs this method.
     */
    public static void showAlertDialog(final String action,
                                       final String title,
                                       final String message,
                                       final String positiveButton,
                                       final String negativeButton,
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
                                  });
        builder.setNegativeButton(negativeButton,
                                  new DialogInterface.OnClickListener() {
                                      public void onClick(DialogInterface dialog, int id) {
                                          dialog.cancel();
                                      }
                                  });
        builder.create().show();
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
}
