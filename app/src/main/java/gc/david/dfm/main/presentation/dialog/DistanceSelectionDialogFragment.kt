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

package gc.david.dfm.main.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.fragment.app.DialogFragment
import gc.david.dfm.R
import gc.david.dfm.core.distances.domain.model.Distance
import gc.david.dfm.designsystem.DfmTheme
import gc.david.dfm.designsystem.Spacing
import java.text.SimpleDateFormat
import java.util.Locale

class DistanceSelectionDialogFragment : DialogFragment() {

    private lateinit var distanceList: List<Distance>

    private var onDialogActionListener: ((Int) -> Unit)? = null

    fun setDistanceList(allDistances: List<Distance>) {
        this.distanceList = allDistances
    }

    fun setOnDialogActionListener(onDialogActionListener: ((Int) -> Unit)) {
        this.onDialogActionListener = onDialogActionListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            setContent {
                DfmTheme {
                    AlertDialog(
                        onDismissRequest = { dismiss() },
                        title = { Text(stringResource(R.string.dialog_load_distances_title)) },
                        text = {
                            LazyColumn {
                                itemsIndexed(distanceList) { index, distance ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onDialogActionListener?.invoke(index)
                                                dismiss()
                                            }
                                            .padding(vertical = Spacing.s),
                                    ) {
                                        Text(
                                            text = distance.name,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Text(
                                                text = distance.distance,
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                            Text(
                                                // TODO provide this text trough the model itself
                                                text = DATE_FORMAT.format(distance.date),
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                    )
                }
            }
        }

    companion object {

        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }
}
