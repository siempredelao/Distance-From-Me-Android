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

package gc.david.dfm.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import gc.david.dfm.R
import gc.david.dfm.adapter.DistanceAdapter
import gc.david.dfm.model.Distance

/**
 * Created by david on 07.02.16.
 */
class DistanceSelectionDialogFragment : DialogFragment() {

    private lateinit var distanceList: List<Distance>

    private var onDialogActionListener: ((Int) -> Unit)? = null

    fun setDistanceList(allDistances: List<Distance>) {
        this.distanceList = allDistances
    }

    fun setOnDialogActionListener(onDialogActionListener: ((Int) -> Unit)) {
        this.onDialogActionListener = onDialogActionListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val distanceAdapter = DistanceAdapter(requireActivity(), distanceList)
        return AlertDialog.Builder(requireActivity())
                .setTitle(getString(R.string.dialog_load_distances_title))
                .setAdapter(distanceAdapter) { _, which ->
                    onDialogActionListener?.invoke(which)
                }
                .create()
    }
}
