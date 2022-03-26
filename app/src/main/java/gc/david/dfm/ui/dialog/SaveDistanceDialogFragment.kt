/*
 * Copyright (c) 2022 David Aguiar Gonzalez
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

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.text.InputType
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.model.LatLng
import gc.david.dfm.R
import gc.david.dfm.Utils
import gc.david.dfm.showinfo.presentation.SaveDistanceViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SaveDistanceDialogFragment : DialogFragment() {

    private val viewModel: SaveDistanceViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseBundle(arguments)
        with(viewModel) {
            errorMessage.observe(this@SaveDistanceDialogFragment, { message ->
                Utils.toastIt(message, requireContext())
            })
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val editTextAlias = EditText(context).apply { // TODO fix problem when config change
            id = View.generateViewId()
            setTextColor(Color.BLACK)
            inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }

        return AlertDialog.Builder(requireActivity())
                .setMessage(getString(R.string.alias_dialog_message))
                .setTitle(getString(R.string.alias_dialog_title))
                .setView(editTextAlias)
                .setPositiveButton(getString(R.string.alias_dialog_accept)) { _, _ ->
                    // TODO transform to real DialogFragment
                    //  and check that save operation is finished before the dialog is closed
                    viewModel.onSave(editTextAlias.text.toString())
                }
                .create()
    }

    private fun parseBundle(bundle: Bundle?) {
        if (bundle == null || bundle.isEmpty) {
            error("Arguments should not be empty.")
        }
        val positionsList = requireNotNull(bundle.getParcelableArrayList<LatLng>(BUNDLE_POSITION_LIST))
        val distance = requireNotNull(bundle.getString(BUNDLE_DISTANCE))

        viewModel.onStart(positionsList, distance)
    }

    companion object {

        private const val BUNDLE_POSITION_LIST = "BUNDLE_POSITION_LIST"
        private const val BUNDLE_DISTANCE = "BUNDLE_DISTANCE"

        fun newInstance(positionsList: List<LatLng>, distance: String) =
                SaveDistanceDialogFragment().apply {
                    val apply = bundleOf(
                            BUNDLE_DISTANCE to distance
                    ).apply {
                        putParcelableArrayList(BUNDLE_POSITION_LIST, ArrayList<Parcelable>(positionsList))
                    }
                    arguments = apply
                }
    }
}
