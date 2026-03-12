/*
 * Copyright (c) 2026 David Aguiar Gonzalez
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

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.model.LatLng
import gc.david.dfm.R
import gc.david.dfm.common.UiUtils
import gc.david.dfm.designsystem.DfmTheme
import gc.david.dfm.designsystem.Spacing
import gc.david.dfm.showinfo.presentation.SaveDistanceViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SaveDistanceDialogFragment : DialogFragment() {

    private val viewModel: SaveDistanceViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseBundle(arguments)
        with(viewModel) {
            errorMessage.observe(this@SaveDistanceDialogFragment) { message ->
                UiUtils.toastIt(message, requireContext())
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            setContent {
                DfmTheme {
                    var alias by remember { mutableStateOf("") }

                    AlertDialog(
                        onDismissRequest = { dismiss() },
                        title = { Text(stringResource(R.string.alias_dialog_title)) },
                        text = {
                            Column {
                                Text(stringResource(R.string.alias_dialog_message))
                                Spacer(Modifier.height(Spacing.s))
                                OutlinedTextField(
                                    value = alias,
                                    onValueChange = { alias = it },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Sentences,
                                    ),
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.onSave(alias)
                                dismiss()
                            }) {
                                Text(stringResource(R.string.alias_dialog_accept))
                            }
                        },
                    )
                }
            }
        }

    private fun parseBundle(bundle: Bundle?) {
        if (bundle == null || bundle.isEmpty) {
            error("Arguments should not be empty.")
        }
        val positionsList = requireNotNull(
            BundleCompat.getParcelableArrayList(bundle, BUNDLE_POSITION_LIST, LatLng::class.java)
        )
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
