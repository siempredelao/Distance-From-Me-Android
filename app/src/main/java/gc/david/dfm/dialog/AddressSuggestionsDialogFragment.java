package gc.david.dfm.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.google.common.collect.Lists;

import java.util.List;

import gc.david.dfm.R;
import gc.david.dfm.Utils;
import gc.david.dfm.logger.DFMLogger;

/**
 * Created by david on 07.02.16.
 */
public class AddressSuggestionsDialogFragment extends DialogFragment {

    private static final String TAG = AddressSuggestionsDialogFragment.class.getSimpleName();

    private List<Address> addressList;
    private OnDialogActionListener onDialogActionListener;

    public void setAddressList(final List<Address> addressList) {
        this.addressList = addressList;
    }

    public void setOnDialogActionListener(final OnDialogActionListener onDialogActionListener) {
        this.onDialogActionListener = onDialogActionListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DFMLogger.logMessage(TAG, "onCreateDialog bundle=" + Utils.dumpBundleToString(savedInstanceState));

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_select_address_title));
        builder.setItems(groupAddresses(addressList).toArray(new String[addressList.size()]),
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int item) {
                                 if (onDialogActionListener != null) {
                                     onDialogActionListener.onItemClick(item);
                                 }
                             }
                         });
        return builder.create();
    }

    private List<String> groupAddresses(final List<Address> addressList) {
        DFMLogger.logMessage(TAG, "groupAddresses");

        final List<String> result = Lists.newArrayList();
        StringBuilder stringBuilder;
        for (final Address l : addressList) {
            stringBuilder = new StringBuilder();
            for (int j = 0; j < l.getMaxAddressLineIndex() + 1; j++) {
                stringBuilder.append(l.getAddressLine(j)).append("\n");
            }
            result.add(stringBuilder.toString());
        }
        return result;
    }

    public interface OnDialogActionListener {
        void onItemClick(int position);
    }
}
