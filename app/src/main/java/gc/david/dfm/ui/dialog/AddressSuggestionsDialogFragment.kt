/*
 * Copyright (c) 2019 David Aguiar Gonzalez
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

package gc.david.dfm.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import gc.david.dfm.R
import gc.david.dfm.address.domain.model.Address

/**
 * Created by david on 07.02.16.
 */
class AddressSuggestionsDialogFragment : DialogFragment() {

    private lateinit var addressList: List<Address>

    private var onDialogActionListener: ((Int) -> Unit)? = null

    fun setAddressList(addressList: List<Address>) {
        this.addressList = addressList
    }

    fun setOnDialogActionListener(onDialogActionListener: ((Int) -> Unit)) {
        this.onDialogActionListener = onDialogActionListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity())
                .setTitle(getString(R.string.dialog_select_address_title))
                .setItems(groupAddresses(addressList).toTypedArray()
                ) { _, item ->
                    onDialogActionListener?.invoke(item)
                }.create()
    }

    // TODO: 13.01.17 improve this formatting, probably text will run out of space
    private fun groupAddresses(addressList: List<Address>): List<String> {
        return addressList.map { it.formattedAddress }
    }
}
