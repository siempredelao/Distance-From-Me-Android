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

package gc.david.dfm.main.presentation

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.LatLng
import gc.david.dfm.R
import gc.david.dfm.address.presentation.AddressViewModel
import gc.david.dfm.common.UiUtils
import gc.david.dfm.common.collectOnStarted
import gc.david.dfm.core.coordinates.domain.model.Coordinates
import gc.david.dfm.designsystem.DfmTheme
import gc.david.dfm.distance.domain.model.DistanceMode
import gc.david.dfm.elevation.presentation.ElevationViewModel
import gc.david.dfm.feedback.InAppReviewHandler
import gc.david.dfm.main.presentation.components.PermissionRationaleDialog
import gc.david.dfm.main.presentation.model.SideNavigationItemId
import gc.david.dfm.main.presentation.screen.MainScreen
import gc.david.dfm.support.navigation.Navigator
import gc.david.dfm.support.navigation.Route
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class MainActivity : FragmentActivity() {

    private val appContext: Context by inject()
    private val navigator: Navigator by inject { parametersOf(this) }
    private val mainViewModel: MainViewModel by viewModel()
    private val elevationViewModel: ElevationViewModel by viewModel()
    private val addressViewModel: AddressViewModel by viewModel()


    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        mainViewModel.onLocationPermissionChanged()
        // Either precise or approximate location is enough
        val granted = permissions.values.any { it }
        if (granted) {
            Timber.tag(TAG).d("permissions GRANTED")
            // onLocationPermissionChanged above starts location tracking
            UiUtils.toastIt(R.string.toast_loading_position, appContext)
        } else {
            Timber.tag(TAG).d("permissions DENIED/INTERRUPTED")
            // Right after a refusal, no rationale means Android won't show its permission dialog again
            mainViewModel.onLocationPermissionDenied(
                permanently = PERMISSIONS.none { shouldShowRequestPermissionRationale(it) },
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag(TAG).d("onCreate savedInstanceState=%s", savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        // Location updates follow the screen: they stop below STARTED and resume when it's visible again
        collectOnStarted { mainViewModel.trackLocation() }

        setContent {
            DfmTheme {
                val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()
                val elevationUiState by elevationViewModel.uiState.collectAsStateWithLifecycle()
                val addressUiState by addressViewModel.uiState.collectAsStateWithLifecycle()
                
                val snackbarHostState = remember { SnackbarHostState() }
                var showChart by rememberSaveable { mutableStateOf(false) }

                // Handle elevation state
                LaunchedEffect(elevationUiState.elevation) {
                    if (elevationUiState.elevation != null) {
                        showChart = true
                    }
                }

                // Handle showChart from both ViewModels
                LaunchedEffect(elevationUiState.showChart, mainUiState.showChart) {
                    if (elevationUiState.showChart) {
                        showChart = true
                        elevationViewModel.onShowChartHandled()
                    }
                    if (!mainUiState.showChart && showChart) {
                        showChart = false
                        mainViewModel.onShowChartHandled()
                    }
                }

                // Handle address state
                LaunchedEffect(addressUiState.errorMessage) {
                    addressUiState.errorMessage?.let {
                        UiUtils.toastIt(it, appContext)
                        addressViewModel.onErrorMessageShown()
                    }
                }

                LaunchedEffect(addressUiState.addressFound) {
                    addressUiState.addressFound?.let { address ->
                        mainViewModel.onPositionByNameResolved(address.coordinates)
                        addressViewModel.onAddressHandled()
                    }
                }

                LaunchedEffect(addressUiState.showConnectionIssue) {
                    if (addressUiState.showConnectionIssue) {
                        UiUtils.toastIt(R.string.toast_no_internet_connection, appContext)
                        addressViewModel.onConnectionIssueShown()
                    }
                }

                // Handle main state
                LaunchedEffect(mainUiState.showConnectionIssue) {
                    if (mainUiState.showConnectionIssue) {
                        UiUtils.toastIt(R.string.toast_no_internet_connection, appContext)
                        mainViewModel.onConnectionIssueShown()
                    }
                }

                LaunchedEffect(mainUiState.errorMessage) {
                    mainUiState.errorMessage?.let {
                        UiUtils.toastIt(it, appContext)
                        mainViewModel.onErrorMessageShown()
                    }
                }

                LaunchedEffect(mainUiState.triggerElevationUpdate) {
                    mainUiState.triggerElevationUpdate?.let { coordinates ->
                        elevationViewModel.onCoordinatesSelected(coordinates)
                        mainViewModel.onElevationUpdateHandled()
                    }
                }

                LaunchedEffect(mainUiState.searchAddress) {
                    mainUiState.searchAddress?.let {
                        addressViewModel.onAddressSearch(it)
                        mainViewModel.onSearchAddressHandled()
                    }
                }

                LaunchedEffect(mainUiState.openShowInfo) {
                    if (mainUiState.openShowInfo) {
                        navigator.navigate(Route.ShowInfo)
                        mainViewModel.onOpenShowInfoHandled()
                    }
                }

                LaunchedEffect(mainUiState.showLocationPermissionSnackbar) {
                    if (mainUiState.showLocationPermissionSnackbar) {
                        val result = snackbarHostState.showSnackbar(
                            message = getString(R.string.snackbar_location_permission_needed),
                            actionLabel = getString(R.string.snackbar_location_permission_action)
                        )
                        if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = "package:$packageName".toUri()
                            startActivity(intent)
                        }
                        mainViewModel.onLocationPermissionSnackbarShown()
                    }
                }

                // Permission rationale dialog - shown when permission is needed
                if (mainUiState.requestLocationPermission) {
                    PermissionRationaleDialog(
                        onRequestPermission = {
                            mainViewModel.onLocationPermissionRequestHandled()
                            permissionLauncher.launch(PERMISSIONS)
                        },
                        onDismiss = {
                            mainViewModel.onLocationPermissionRequestHandled()
                            mainViewModel.onDistanceFromAnyPositionSet()
                        }
                    )
                }

                MainScreen(
                    mapState = mainUiState.mapState,
                    sideNavigationState = mainUiState.sideNavigationState,
                    snackbarHostState = snackbarHostState,
                    isLoading = addressUiState.isLoading,
                    elevationData = elevationUiState.elevation,
                    showChart = showChart,
                    showChartFab = elevationUiState.elevation != null && !showChart,
                    distancesToLoad = mainUiState.selectFromDistancesLoaded,
                    addressSuggestions = addressUiState.multipleAddressesFound,
                    isMyLocationEnabled = mainUiState.distanceMode == DistanceMode.FROM_CURRENT_POINT
                            && mainUiState.isLocationPermissionGranted,
                    onSearchQuery = { query ->
                        mainViewModel.onAddressSearch(query)
                    },
                    onMapClick = { latLng ->
                        mainViewModel.onMapClick(latLng.toCoordinates())
                    },
                    onMapLongClick = { latLng ->
                        mainViewModel.onMapLongClick(latLng.toCoordinates())
                    },
                    onMarkerClick = { 
                        mainViewModel.onInfoWindowClick()
                    },
                    onNavigationItemClick = { itemId ->
                        when (itemId) {
                            SideNavigationItemId.CURRENT_POSITION -> {
                                mainViewModel.onDistanceFromCurrentPositionSet()
                            }
                            SideNavigationItemId.ANY_POSITION -> {
                                mainViewModel.onDistanceFromAnyPositionSet()
                            }
                            SideNavigationItemId.RATE_APP -> {
                                InAppReviewHandler.rateApp(this@MainActivity)
                            }
                            SideNavigationItemId.SETTINGS -> {
                                navigator.navigate(Route.Settings)
                            }
                            SideNavigationItemId.HELP_FEEDBACK -> {
                                navigator.navigate(Route.Faq)
                            }
                            SideNavigationItemId.ABOUT -> {
                                navigator.navigate(Route.About)
                            }
                            SideNavigationItemId.LOAD -> {
                                mainViewModel.onLoadDistancesClick()
                            }
                            SideNavigationItemId.CRASH -> {
                                mainViewModel.onForceCrashClick()
                            }
                        }
                    },
                    onMyLocationClick = {
                        mainViewModel.onMyLocationButtonClick()
                    },
                    onShowChartClick = {
                        showChart = true
                    },
                    onElevationChartClose = {
                        showChart = false
                    },
                    onDistanceSelected = { id, name ->
                        mainViewModel.onDistanceToShowSelected(id, name)
                        mainViewModel.onDistancesLoadedHandled()
                    },
                    onRateAppClick = {
                        InAppReviewHandler.rateApp(this@MainActivity, skipFallbackDialog = true)
                    },
                    onDistanceSelectionDismiss = {
                        mainViewModel.onDistancesLoadedHandled()
                    },
                    onAddressSelected = { address ->
                        addressViewModel.onAddressSelected(address)
                        addressViewModel.onMultipleAddressesHandled()
                    },
                    onAddressSuggestionsDismiss = {
                        addressViewModel.onMultipleAddressesHandled()
                    },
                    onCameraUpdateHandled = {
                        mainViewModel.onCameraUpdateHandled()
                    },
                    onMapClearHandled = {
                        mainViewModel.onMapClearHandled()
                    }
                )
            }
        }

        mainViewModel.onStart()
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.onLocationPermissionChanged()
    }

    companion object {

        private const val TAG = "MainActivity"
        private val PERMISSIONS = arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
    }
}

private fun LatLng.toCoordinates() = Coordinates(latitude, longitude)
