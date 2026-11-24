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
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
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
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.window.core.layout.WindowSizeClass
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import gc.david.dfm.R
import gc.david.dfm.core.address.domain.model.Address
import gc.david.dfm.elevation.presentation.model.ElevationUiModel
import gc.david.dfm.main.presentation.components.AddressSuggestionsDialog
import gc.david.dfm.main.presentation.components.AppNavigationRail
import gc.david.dfm.main.presentation.components.CollapsedNavigationRailWidth
import gc.david.dfm.main.presentation.components.DistanceSelectionDialog
import gc.david.dfm.main.presentation.components.ElevationChart
import gc.david.dfm.main.presentation.components.LottieProgress
import gc.david.dfm.main.presentation.components.MapContent
import gc.david.dfm.main.presentation.components.NavigationRailWidth
import gc.david.dfm.main.presentation.components.TransparentSearchBar
import gc.david.dfm.main.presentation.model.DistanceSelectionUiModel
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
    distancesToLoad: List<DistanceSelectionUiModel>?,
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
    onDistanceSelected: (id: Long, name: String) -> Unit,
    onRateAppClick: () -> Unit,
    onDistanceSelectionDismiss: () -> Unit,
    onAddressSelected: (Address) -> Unit,
    onAddressSuggestionsDismiss: () -> Unit,
    onCameraUpdateHandled: () -> Unit,
    onMapClearHandled: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // From medium widths on, a collapsed rail stays beside the map and expands over it;
    // narrower windows show the rail only while it is expanded, over the map
    val hasPermanentRail = currentWindowAdaptiveInfo().windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    var isRailExpanded by rememberSaveable { mutableStateOf(false) }
    // The scrim, the rail and the FAB offset all follow this one transition, so they move together
    val railTransition = updateTransition(targetState = isRailExpanded, label = "Rail")
    val fabOffsetX by railTransition.animateDp(label = "FAB offset") { expanded ->
        // A permanent rail expands over the map's left edge, clear of the buttons on the right
        if (expanded && !hasPermanentRail) NavigationRailWidth else 0.dp
    }
    // Keeps the map and the search bar clear of the collapsed rail and of the cutout beside it
    val besideRailModifier = if (hasPermanentRail) {
        Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))
            .padding(start = CollapsedNavigationRailWidth)
    } else {
        Modifier
    }

    BackHandler(enabled = isRailExpanded) {
        isRailExpanded = false
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.navigationBarsPadding()
            ) { data ->
                // Beside the action and the close button, long words break on small screens
                Snackbar(snackbarData = data, actionOnNewLine = true)
            }
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
                modifier = Modifier
                    .fillMaxSize()
                    .then(besideRailModifier),
                properties = MapProperties(
                    isMyLocationEnabled = isMyLocationEnabled,
                    mapType = MapType.HYBRID
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false
                )
            )

            // Top UI elements stacked vertically (Search Bar + Elevation Chart)
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .then(besideRailModifier)
            ) {
                TransparentSearchBar(
                    // A permanent rail has its own toggle
                    onMenuClick = if (hasPermanentRail) null else ({ isRailExpanded = !isRailExpanded }),
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
            railTransition.AnimatedVisibility(
                visible = { it },
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
                            onClick = { isRailExpanded = false }
                        )
                )
            }

            // Navigation rail, on top of the scrim
            val onRailItemClick = { itemId: SideNavigationItemId ->
                onNavigationItemClick(itemId)
                isRailExpanded = false
            }
            val onRailToggle = { isRailExpanded = !isRailExpanded }
            if (hasPermanentRail) {
                MainNavigationRail(
                    railTransition = railTransition,
                    collapses = true,
                    sideNavigationState = sideNavigationState,
                    onItemClick = onRailItemClick,
                    onToggle = onRailToggle,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .zIndex(2f)
                )
            } else {
                // Slides in from the left while expanded
                railTransition.AnimatedVisibility(
                    visible = { it },
                    enter = slideInHorizontally { -it },
                    exit = slideOutHorizontally { -it },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .zIndex(2f)
                ) {
                    MainNavigationRail(
                        railTransition = railTransition,
                        collapses = false,
                        sideNavigationState = sideNavigationState,
                        onItemClick = onRailItemClick,
                        onToggle = onRailToggle
                    )
                }
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
                    // Clear of the navigation bar and, in landscape, of the display cutout
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.End))
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
                            contentDescription = stringResource(R.string.fab_show_chart_description)
                        )
                    }
                }

                // My Location FAB
                FloatingActionButton(
                    onClick = onMyLocationClick
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = stringResource(R.string.fab_my_location_description)
                    )
                }
            }
        }

        // Dialogs
        distancesToLoad?.let { items ->
            DistanceSelectionDialog(
                list = items,
                onDistanceSelected = onDistanceSelected,
                onRateAppClick = onRateAppClick,
                onDismiss = onDistanceSelectionDismiss
            )
        }

        addressSuggestions?.let { items ->
            AddressSuggestionsDialog(
                addresses = items,
                onAddressSelected = onAddressSelected,
                onDismiss = onAddressSuggestionsDismiss
            )
        }
    }
}

/**
 * The navigation rail, following [railTransition]. A rail that [collapses] animates between its collapsed and
 * expanded widths; otherwise it is always expanded. Its width is read here so each animation frame only
 * recomposes the rail.
 */
@Composable
private fun MainNavigationRail(
    railTransition: Transition<Boolean>,
    collapses: Boolean,
    sideNavigationState: SideNavigationUiState,
    onItemClick: (SideNavigationItemId) -> Unit,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val width by railTransition.animateDp(label = "Rail width") { expanded ->
        if (expanded || !collapses) NavigationRailWidth else CollapsedNavigationRailWidth
    }
    // Labels appear once the rail is nearly expanded, so they don't wrap, and go as soon as it starts collapsing
    val showsLabels = !collapses || (railTransition.targetState && width >= NavigationRailWidth * 0.9f)
    AppNavigationRail(
        selectedItemId = sideNavigationState.selectedItemId,
        onItemClick = onItemClick,
        isExpanded = showsLabels,
        onToggle = onToggle,
        width = width,
        showLoadMenuItem = sideNavigationState.showLoadMenuItem,
        showDebugOptions = sideNavigationState.showDebugOptions,
        modifier = modifier
    )
}
