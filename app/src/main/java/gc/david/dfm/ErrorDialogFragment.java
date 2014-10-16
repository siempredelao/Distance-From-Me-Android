package gc.david.dfm;

/**
 * Created by David on 13/10/2014.
 */

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Defines a DialogFragment to display the error dialog generated in
 * showErrorDialog.
 */
public class ErrorDialogFragment extends DialogFragment {

	// Global field to contain the error dialog
	private Dialog dialog;

	/**
	 * Default constructor. Sets the dialog field to null
	 */
	public ErrorDialogFragment() {
		super();
		dialog = null;
	}

	/**
	 * Set the dialog to display
	 *
	 * @param dialog An error dialog
	 */
	public void setDialog(final Dialog dialog) {
		this.dialog = dialog;
	}

	/**
	 * This method must return a Dialog to the DialogFragment.
	 */
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		return dialog;
	}
}
