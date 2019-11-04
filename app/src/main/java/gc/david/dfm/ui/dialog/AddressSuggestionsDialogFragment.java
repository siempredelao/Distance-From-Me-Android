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

import java.util.ArrayList;
import java.util.List;

import gc.david.dfm.R;
import gc.david.dfm.address.domain.model.Address;

/**
 * Created by david on 07.02.16.
 */
public class AddressSuggestionsDialogFragment extends DialogFragment {

    private List<Address>          addressList;
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

    // TODO: 13.01.17 improve this formatting, probably text will run out of space
    private List<String> groupAddresses(final List<Address> addressList) {
        final List<String> result = new ArrayList<>();
        for (final Address address : addressList) {
            result.add(address.getFormattedAddress());
        }
        return result;
    }

    public interface OnDialogActionListener {
        void onItemClick(int position);
    }
}
