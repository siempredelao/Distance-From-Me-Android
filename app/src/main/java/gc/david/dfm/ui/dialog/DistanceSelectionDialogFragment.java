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

package gc.david.dfm.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

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
