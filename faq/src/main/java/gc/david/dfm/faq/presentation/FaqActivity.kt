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

package gc.david.dfm.faq.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import gc.david.dfm.designsystem.DfmTheme
import gc.david.dfm.faq.presentation.model.FaqUiState
import gc.david.dfm.faq.presentation.screen.FaqScreen
import org.koin.androidx.viewmodel.ext.android.viewModel

class FaqActivity : ComponentActivity() {

    private val viewModel: FaqViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val uiState by viewModel.uiState.observeAsState(FaqUiState.Loading)
            DfmTheme {
                FaqScreen(
                    uiState = uiState,
                    onBack = { onBackPressedDispatcher.onBackPressed() },
                )
            }
        }
        viewModel.onStart()
    }

    companion object {

        fun open(activity: Activity) {
            val openFaqActivityIntent = Intent(activity, FaqActivity::class.java)
            activity.startActivity(openFaqActivityIntent)
        }
    }
}