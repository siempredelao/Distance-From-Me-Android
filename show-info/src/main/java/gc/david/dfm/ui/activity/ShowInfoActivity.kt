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

package gc.david.dfm.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.IntentCompat
import com.google.android.gms.maps.model.LatLng
import gc.david.dfm.designsystem.DfmTheme
import gc.david.dfm.showinfo.presentation.SaveDistanceViewModel
import gc.david.dfm.showinfo.presentation.ShowInfoViewModel
import gc.david.dfm.showinfo.presentation.ui.SaveDistanceDialog
import gc.david.dfm.showinfo.presentation.ui.ShowInfoScreen
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.*

class ShowInfoActivity : ComponentActivity() {

    private val showInfoViewModel: ShowInfoViewModel by viewModel()
    private val saveDistanceViewModel: SaveDistanceViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag(TAG).d("onCreate")
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            loadData()
        }

        setContent {
            val uiState by showInfoViewModel.uiState.collectAsState()
            val saveUserMessage by saveDistanceViewModel.userMessage.collectAsState()

            DfmTheme {
                ShowInfoScreen(
                    uiState = uiState,
                    saveUserMessage = saveUserMessage,
                    onBackPress = { onBackPressedDispatcher.onBackPressed() },
                    onShare = showInfoViewModel::onShare,
                    onRefresh = ::loadData,
                    onSave = showInfoViewModel::onSave,
                    onUserMessageShown = showInfoViewModel::onUserMessageShown,
                    onShareDialogShown = showInfoViewModel::onShareDialogShown,
                    onSaveUserMessageShown = saveDistanceViewModel::onUserMessageShown,
                )

                if (uiState.showSaveDialog) {
                    SaveDistanceDialog(
                        onDismiss = showInfoViewModel::onSaveDialogDismissed,
                        onConfirm = { alias ->
                            // TODO store position list and distance in a repository to avoid asking
                            //  the viewmodel for information twice
                            val data = showInfoViewModel.getSaveDistanceData()
                            saveDistanceViewModel.onStart(data.positionsList, data.distance)
                            saveDistanceViewModel.onSave(alias)
                            showInfoViewModel.onSaveDialogDismissed()
                        },
                    )
                }
            }
        }
    }

    private fun loadData() {
        Timber.tag(TAG).d("loadData")

        val positionsList =
            IntentCompat.getParcelableArrayListExtra(
                intent,
                POSITIONS_LIST_EXTRA_KEY,
                LatLng::class.java
            )
                ?: error("No positions available")
        val distance = intent.getStringExtra(DISTANCE_EXTRA_KEY)!!
        showInfoViewModel.onStart(positionsList, distance)
    }

    companion object {

        private const val TAG = "ShowInfoActivity"

        private const val POSITIONS_LIST_EXTRA_KEY = "positionsList"
        private const val DISTANCE_EXTRA_KEY = "distance"

        fun open(activity: Activity, coordinates: List<LatLng>, distanceAsText: String) {
            val openShowInfoActivityIntent = Intent(activity, ShowInfoActivity::class.java)
            openShowInfoActivityIntent.putParcelableArrayListExtra(
                POSITIONS_LIST_EXTRA_KEY,
                ArrayList<Parcelable>(coordinates)
            )
            openShowInfoActivityIntent.putExtra(DISTANCE_EXTRA_KEY, distanceAsText)
            activity.startActivity(openShowInfoActivityIntent)
        }
    }
}
