package gc.david.dfm.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import java.util.List;

import gc.david.dfm.R;
import gc.david.dfm.adapter.DistanceAdapter;
import gc.david.dfm.model.Distance;

/**
 * Created by david on 07.02.16.
 */
public class DistanceSelectionDialogFragment extends DialogFragment {

    private List<Distance> distanceList;
    private OnDialogActionListener onDialogActionListener;

    public void setDistanceList(final List<Distance> allDistances) {
        this.distanceList = allDistances;
    }

    public void setOnDialogActionListener(final OnDialogActionListener onDialogActionListener) {
        this.onDialogActionListener = onDialogActionListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final DistanceAdapter distanceAdapter = new DistanceAdapter(getActivity(), distanceList);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_load_distances_title))
               .setAdapter(distanceAdapter, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       if (onDialogActionListener != null) {
                           onDialogActionListener.onItemClick(which);
                       }
                   }
               });
        return builder.create();
    }

    public interface OnDialogActionListener {
        void onItemClick(int position);
    }
}
