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

package gc.david.dfm.opensource.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.net.toUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import gc.david.dfm.designsystem.DfmTheme
import gc.david.dfm.opensource.presentation.model.OpenSourceUiState
import gc.david.dfm.opensource.presentation.screen.OpenSourceDetailScreen
import gc.david.dfm.opensource.presentation.screen.OpenSourceListScreen
import org.koin.androidx.viewmodel.ext.android.viewModel

class AboutActivity : ComponentActivity() {

    private val viewModel: OpenSourceViewModel by viewModel()

    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val uiState by viewModel.uiState.observeAsState(OpenSourceUiState.Loading)
            val navController = rememberNavController()

            DfmTheme {
                SharedTransitionLayout {
                    NavHost(navController = navController, startDestination = "list") {
                        composable("list") {
                            OpenSourceListScreen(
                                uiState = uiState,
                                animatedVisibilityScope = this@composable,
                                onLibraryClick = { index -> navController.navigate("detail/$index") },
                                onBack = { onBackPressedDispatcher.onBackPressed() },
                            )
                        }
                        composable("detail/{index}") { backStackEntry ->
                            val index = backStackEntry.arguments
                                ?.getString("index")
                                ?.toIntOrNull()
                                ?: return@composable
                            val library = (uiState as? OpenSourceUiState.Content)
                                ?.libraries
                                ?.getOrNull(index)
                                ?: return@composable

                            OpenSourceDetailScreen(
                                library = library,
                                index = index,
                                animatedVisibilityScope = this@composable,
                                onBack = { navController.popBackStack() },
                                onOpenInBrowser = { url ->
                                    startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                                },
                            )
                        }
                    }
                }
            }
        }
        viewModel.onStart()
    }

    companion object {

        fun open(activity: Activity) {
            val openAboutActivityIntent = Intent(activity, AboutActivity::class.java)
            activity.startActivity(openAboutActivityIntent)
        }
    }
}