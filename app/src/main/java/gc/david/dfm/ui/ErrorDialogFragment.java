package gc.david.dfm.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import gc.david.dfm.logger.DFMLogger;

/**
 * Created by David on 13/10/2014.
 */
public class ErrorDialogFragment extends DialogFragment {

    private static final String TAG = ErrorDialogFragment.class.getSimpleName();

    private Dialog dialog;

    public ErrorDialogFragment() {
        super();

        dialog = null;
    }

    public void setDialog(final Dialog dialog) {
        DFMLogger.logMessage(TAG, "setDialog");

        this.dialog = dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        DFMLogger.logMessage(TAG, "onCreateDialog");

        return dialog;
    }
}
