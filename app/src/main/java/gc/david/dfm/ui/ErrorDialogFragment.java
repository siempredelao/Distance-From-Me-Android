package gc.david.dfm.ui;

/**
 * Created by David on 13/10/2014.
 */

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import gc.david.dfm.logger.DFMLogger;

/**
 * Defines a DialogFragment to display the error dialog generated in
 * showErrorDialog.
 */
public class ErrorDialogFragment extends DialogFragment {

    private static final String TAG = ErrorDialogFragment.class.getSimpleName();

    // Global field to contain the error dialog
    private Dialog dialog;

    /**
     * Default constructor. Sets the dialog field to null
     */
    public ErrorDialogFragment() {
        super();
        DFMLogger.logMessage(TAG, "Constructor");

        dialog = null;
    }

    /**
     * Set the dialog to display
     *
     * @param dialog An error dialog
     */
    public void setDialog(final Dialog dialog) {
        DFMLogger.logMessage(TAG, "setDialog");

        this.dialog = dialog;
    }

    /**
     * This method must return a Dialog to the DialogFragment.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        DFMLogger.logMessage(TAG, "onCreateDialog");

        return dialog;
    }
}
