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

package gc.david.dfm.showinfo.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import gc.david.dfm.designsystem.DfmTheme
import gc.david.dfm.showinfo.presentation.savedistance.SaveDistanceDialog
import gc.david.dfm.showinfo.presentation.savedistance.SaveDistanceViewModel
import gc.david.dfm.showinfo.presentation.screen.ShowInfoScreen
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ShowInfoActivity : ComponentActivity() {

    private val showInfoViewModel: ShowInfoViewModel by viewModel()
    private val saveDistanceViewModel: SaveDistanceViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag(TAG).d("onCreate")
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            showInfoViewModel.onStart()
        }

        setContent {
            val uiState by showInfoViewModel.uiState.collectAsState()
            val saveUserMessage by saveDistanceViewModel.userMessage.collectAsState()

            if (uiState.shouldFinish) {
                finish()
            }

            DfmTheme {
                ShowInfoScreen(
                    uiState = uiState,
                    saveUserMessage = saveUserMessage,
                    onBackPress = { onBackPressedDispatcher.onBackPressed() },
                    onShare = showInfoViewModel::onShare,
                    onRefresh = showInfoViewModel::onRefresh,
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

    companion object {

        private const val TAG = "ShowInfoActivity"

        fun open(activity: Activity) {
            activity.startActivity(Intent(activity, ShowInfoActivity::class.java))
        }
    }
}