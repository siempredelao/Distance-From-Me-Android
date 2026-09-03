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

package gc.david.dfm.main.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import gc.david.dfm.address.domain.model.Address
import gc.david.dfm.core.distances.domain.model.Distance
import gc.david.dfm.elevation.presentation.model.ElevationUiModel
import gc.david.dfm.main.presentation.components.AddressSuggestionsDialog
import gc.david.dfm.main.presentation.components.AppNavigationRail
import gc.david.dfm.main.presentation.components.DistanceSelectionDialog
import gc.david.dfm.main.presentation.components.ElevationChart
import gc.david.dfm.main.presentation.components.LottieProgress
import gc.david.dfm.main.presentation.components.MapContent
import gc.david.dfm.main.presentation.components.TransparentSearchBar
import gc.david.dfm.main.presentation.model.MapUiState
import gc.david.dfm.main.presentation.model.MarkerData
import gc.david.dfm.main.presentation.model.SideNavigationItemId
import gc.david.dfm.main.presentation.model.SideNavigationUiState

/**
 * Main screen composable containing the map, navigation rail, search bar, and FABs.
 * This is the main UI for the app showing distances on a map.
 */
@Composable
fun MainScreen(
    mapState: MapUiState,
    sideNavigationState: SideNavigationUiState,
    snackbarHostState: SnackbarHostState,
    isLoading: Boolean,
    elevationData: ElevationUiModel?,
    showChart: Boolean,
    showChartFab: Boolean,
    distancesToLoad: List<Distance>?,
    addressSuggestions: List<Address>?,
    isMyLocationEnabled: Boolean,
    onSearchQuery: (String) -> Unit,
    onMapClick: (LatLng) -> Unit,
    onMapLongClick: (LatLng) -> Unit,
    onMarkerClick: (MarkerData) -> Unit,
    onNavigationItemClick: (SideNavigationItemId) -> Unit,
    onMyLocationClick: () -> Unit,
    onShowChartClick: () -> Unit,
    onElevationChartClose: () -> Unit,
    onDistanceSelected: (Distance) -> Unit,
    onDistanceSelectionDismiss: () -> Unit,
    onAddressSelected: (Address) -> Unit,
    onAddressSuggestionsDismiss: () -> Unit,
    onCameraUpdateHandled: () -> Unit,
    onMapClearHandled: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isRailVisible by rememberSaveable { mutableStateOf(false) }
    val fabOffsetX by animateDpAsState(
        targetValue = if (isRailVisible) 256.dp else 0.dp,
        label = "FAB offset animation"
    )

    BackHandler(enabled = isRailVisible) {
        isRailVisible = false
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.navigationBarsPadding()
            )
        },
        contentWindowInsets = WindowInsets(0),
        modifier = modifier
    ) { _ ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Map as background
            MapContent(
                mapState = mapState,
                onMapClick = onMapClick,
                onMapLongClick = onMapLongClick,
                onMarkerClick = onMarkerClick,
                onCameraUpdateHandled = onCameraUpdateHandled,
                onMapClearHandled = onMapClearHandled,
                modifier = Modifier.fillMaxSize(),
                properties = MapProperties(
                    isMyLocationEnabled = isMyLocationEnabled,
                    mapType = MapType.HYBRID
                ),
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            )

            // Top UI elements stacked vertically (Search Bar + Elevation Chart)
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
            ) {
                TransparentSearchBar(
                    onMenuClick = { isRailVisible = !isRailVisible },
                    onSearchQuery = onSearchQuery
                )

                ElevationChart(
                    isVisible = showChart,
                    elevationData = elevationData,
                    onClose = onElevationChartClose
                )
            }

            // This is just a scrim overlay that appears when the navigation rail is visible.
            // It swallows pointer events and captures clicks to close the rail.
            // Therefore, the map becomes unclickable.
            AnimatedVisibility(
                visible = isRailVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DrawerDefaults.scrimColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { isRailVisible = false }
                        )
                )
            }

            // Navigation Rail (slides in from left) - Rendered on top with higher z-index
            AnimatedVisibility(
                visible = isRailVisible,
                enter = slideInHorizontally { -it },
                exit = slideOutHorizontally { -it },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .zIndex(2f)
            ) {
                AppNavigationRail(
                    isExpanded = true,
                    selectedItemId = sideNavigationState.selectedItemId,
                    onItemClick = { itemId ->
                        onNavigationItemClick(itemId)
                        // Close rail after item click
                        isRailVisible = false
                    },
                    onClose = { isRailVisible = false },
                    showLoadMenuItem = sideNavigationState.showLoadMenuItem,
                    showCrashMenuItem = sideNavigationState.showCrashMenuItem
                )
            }



            // Lottie Progress overlay
            LottieProgress(
                isLoading = isLoading,
                modifier = Modifier.align(Alignment.Center)
            )

            // FABs at bottom right (offset when rail is visible)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = -fabOffsetX)
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                // Show Chart FAB
                if (showChartFab) {
                    FloatingActionButton(
                        onClick = onShowChartClick,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Show Chart"
                        )
                    }
                }

                // My Location FAB
                FloatingActionButton(
                    onClick = onMyLocationClick
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "My Location"
                    )
                }
            }
        }

        // Dialogs
        distancesToLoad?.let { distances ->
            DistanceSelectionDialog(
                distances = distances,
                onDistanceSelected = onDistanceSelected,
                onDismiss = onDistanceSelectionDismiss
            )
        }

        addressSuggestions?.let { addresses ->
            AddressSuggestionsDialog(
                addresses = addresses,
                onAddressSelected = onAddressSelected,
                onDismiss = onAddressSuggestionsDismiss
            )
        }
    }
}

