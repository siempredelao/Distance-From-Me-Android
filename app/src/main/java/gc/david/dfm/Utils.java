package gc.david.dfm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by David on 15/10/2014.
 */
public class Utils {
    /**
     * Makes toasting easy!
     *
     * @param charSequence The string to show.
     * @param context      Activity context.
     */
    public static void toastIt(final CharSequence charSequence, final Context context) {
        Toast.makeText(context, charSequence.toString(), Toast.LENGTH_LONG).show();
    }


    /**
     * Function to know the current network status.
     *
     * @return Returns <code>true</code> if the device is connected to a network;
     * otherwise, returns <code>false</code>.
     */
    public static boolean isOnline(final Context context) {
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
                                       final CharSequence title,
                                       final CharSequence message,
                                       final CharSequence positiveButton,
                                       final CharSequence negativeButton,
                                       final Activity activity) {
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
}
