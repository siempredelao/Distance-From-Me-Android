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

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.*
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import gc.david.dfm.*
import gc.david.dfm.common.Coordinates
import gc.david.dfm.common.UiUtils
import gc.david.dfm.adapter.MarkerInfoWindowAdapter
import gc.david.dfm.collectOnStarted
import gc.david.dfm.systemService
import gc.david.dfm.address.presentation.AddressViewModel
import gc.david.dfm.connectivity.ConnectionIssuesDialogFragment
import gc.david.dfm.core.distances.domain.model.Distance
import gc.david.dfm.databinding.ActivityMainBinding
import gc.david.dfm.elevation.presentation.ElevationViewModel
import gc.david.dfm.elevation.presentation.model.ElevationModel
import gc.david.dfm.faq.presentation.FaqActivity
import gc.david.dfm.feedback.InAppReviewHandler
import gc.david.dfm.location.GeofencingLocationManager
import gc.david.dfm.opensource.presentation.AboutActivity
import gc.david.dfm.main.presentation.MainViewModel
import gc.david.dfm.settings.presentation.SettingsActivity
import gc.david.dfm.showinfo.presentation.ShowInfoActivity
import gc.david.dfm.ui.animation.AnimatorUtil
import gc.david.dfm.ui.dialog.AddressSuggestionsDialogFragment
import gc.david.dfm.ui.dialog.DistanceSelectionDialogFragment
import gc.david.dfm.address.domain.model.Coordinates as AddressCoordinate
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class MainActivity :
        AppCompatActivity(),
        OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnInfoWindowClickListener {

    private val appContext: Context by inject()
    private val mapRenderer: MapRenderer by inject()
    private val permissionChecker: PermissionChecker by inject()
    private val locationManager: GeofencingLocationManager by inject()
    private val mainViewModel: MainViewModel by viewModel()
    private val elevationViewModel: ElevationViewModel by viewModel()
    private val addressViewModel: AddressViewModel by viewModel()

    private lateinit var binding: ActivityMainBinding

    private var googleMap: GoogleMap? = null

    @SuppressLint("MissingPermission")
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            Timber.tag(TAG).d("permissions GRANTED")
            UiUtils.toastIt(R.string.toast_loading_position, appContext)
            googleMap?.isMyLocationEnabled = true
            binding.fabMyLocation.isVisible = true
            locationManager.startAfterPermissionGranted()
        } else {
            Timber.tag(TAG).d("permissions DENIED/INTERRUPTED")
            binding.fabMyLocation.isVisible = false
            binding.nvDrawer.menu.findItem(R.id.menu_any_position).isChecked = true
            resetMap()
        }
    }

    private fun isMinimiseButtonShown(): Boolean = binding.fabShowChart.isShown

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag(TAG).d("onCreate savedInstanceState=%s", UiUtils.dumpBundleToString(savedInstanceState))

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
            fabMyLocation.setOnClickListener { onMyLocationClick() }
            fabShowChart.setOnClickListener { onShowChartClick() }

            setSupportActionBar(tbMain.root)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                val upArrow = ContextCompat.getDrawable(appContext, R.drawable.ic_menu_white_24dp)
                setHomeAsUpIndicator(upArrow)
            }

            nvDrawer.setNavigationItemSelectedListener { menuItem ->
                drawerLayout.closeDrawers()
                when (menuItem.itemId) {
                    R.id.menu_current_position -> {
                        mainViewModel.onDistanceFromCurrentPositionSet()
                        menuItem.isChecked = true
                        true
                    }
                    R.id.menu_any_position -> {
                        mainViewModel.onDistanceFromAnyPositionSet()
                        menuItem.isChecked = true
                        true
                    }
                    R.id.menu_rate_app -> {
                        showRateDialog()
                        true
                    }
                    R.id.menu_settings -> {
                        SettingsActivity.open(this@MainActivity)
                        true
                    }
                    R.id.menu_help_feedback -> {
                        FaqActivity.open(this@MainActivity)
                        true
                    }
                    R.id.menu_about -> {
                        AboutActivity.open(this@MainActivity)
                        true
                    }
                    else -> false
                }
            }
            elevationChartView.setOnCloseListener { animateHideChart() }

            val mapFragment = map2.getFragment<SupportMapFragment>()
            mapFragment.getMapAsync(this@MainActivity)
        }

        onBackPressedDispatcher.addCallback(this) {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }

        lifecycle.addObserver(locationManager)
        locationManager.setOnLocationChangedListener(mainViewModel::onLocationChanged)

        observeElevationViewModel()
        observeAddressViewModel()
        observeMainViewModel()
        mainViewModel.onStart()

        handleIntents(intent)
    }

    private fun observeElevationViewModel() = collectOnStarted {
        elevationViewModel.uiState.collect { state ->
            if (state.hideChart) {
                hideChart()
                elevationViewModel.onHideChartHandled()
            }
            state.elevationModel?.let { buildChart(it) }
        }
    }

    private fun observeAddressViewModel() = collectOnStarted {
        addressViewModel.uiState.collect { state ->
            binding.progressView.isVisible = state.isLoading

            if (state.showConnectionIssue) {
                ConnectionIssuesDialogFragment().show(supportFragmentManager, null)
                addressViewModel.onConnectionIssueShown()
            }

            state.errorMessage?.let {
                UiUtils.toastIt(it, appContext)
                addressViewModel.onErrorMessageShown()
            }

            state.addressFound?.let {
                showPositionByName(it)
                addressViewModel.onAddressHandled()
            }

            state.multipleAddressesFound?.let {
                showAddressSelectionDialog(it)
                addressViewModel.onMultipleAddressesHandled()
            }
        }
    }

    private fun observeMainViewModel() = collectOnStarted {
        mainViewModel.uiState.collect { state ->
            with(binding.tbMain.root.menu) {
                Timber.tag(TAG).d("showLoadDistancesItem ${state.showLoadDistancesItem}")
                findItem(R.id.action_load)?.isVisible = state.showLoadDistancesItem
                findItem(R.id.action_crash)?.isVisible = state.showForceCrashItem
            }

            if (state.showConnectionIssue) {
                ConnectionIssuesDialogFragment().show(supportFragmentManager, null)
                mainViewModel.onConnectionIssueShown()
            }

            state.errorMessage?.let {
                UiUtils.toastIt(it, appContext)
                mainViewModel.onErrorMessageShown()
            }

            state.selectFromDistancesLoaded?.let {
                showLoadedDistancesDialog(it)
                mainViewModel.onDistancesLoadedHandled()
            }

            state.triggerElevationUpdate?.let { coordinates ->
                elevationViewModel.onCoordinatesSelected(coordinates)
                mainViewModel.onElevationUpdateHandled()
            }

            // Render map state using MapRenderer
            googleMap?.let { map ->
                mapRenderer.render(map, state.mapState)
                
                // Handle one-time events after rendering
                if (state.mapState.clearMap) {
                    mainViewModel.onMapClearHandled()
                }
                if (state.mapState.cameraUpdate != null) {
                    mainViewModel.onCameraUpdateHandled()
                }
            }

            state.searchAddress?.let {
                addressViewModel.onAddressSearch(it)
                mainViewModel.onSearchAddressHandled()
            }

            if (state.hideChart) {
                hideChart()
                mainViewModel.onHideChartHandled()
            }

            state.openShowInfo?.let {
                ShowInfoActivity.open(this@MainActivity, it.formattedDistance)
                mainViewModel.onOpenShowInfoHandled()
            }

            if (state.showLocationPermissionSnackbar) {
                Snackbar
                    .make(
                        binding.drawerLayout,
                        R.string.snackbar_location_permission_needed,
                        Snackbar.LENGTH_INDEFINITE
                    )
                    .setAction(R.string.snackbar_location_permission_action) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = "package:$packageName".toUri()
                        startActivity(intent)
                    }
                    .show()
                mainViewModel.onLocationPermissionSnackbarShown()
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        googleMap = map.apply {
            uiSettings.isMyLocationButtonEnabled = false
            mapType = GoogleMap.MAP_TYPE_HYBRID
            setOnMapLongClickListener(this@MainActivity)
            setOnMapClickListener(this@MainActivity)
            setOnInfoWindowClickListener(this@MainActivity)
            setInfoWindowAdapter(MarkerInfoWindowAdapter(this@MainActivity))
        }

        resetMap()

        if (!permissionChecker.isLocationPermissionGranted()) {
            permissionLauncher.launch(PERMISSIONS)
        } else {
            UiUtils.toastIt(R.string.toast_loading_position, appContext)
            googleMap?.isMyLocationEnabled = true
            binding.fabMyLocation.isVisible = true
        }
    }

    override fun onMapLongClick(point: LatLng) {
        mainViewModel.onMapLongClick(point.toCoordinates())
    }

    override fun onMapClick(point: LatLng) {
        mainViewModel.onMapClick(point.toCoordinates())
    }

    override fun onInfoWindowClick(marker: Marker) {
        Timber.tag(TAG).d("onInfoWindowClick")
        mainViewModel.onInfoWindowClick()
    }

    private fun resetMap() {
        Timber.tag(TAG).d("resetMap")
        mainViewModel.resetMap()
    }

    override fun onNewIntent(intent: Intent) {
        Timber.tag(TAG).d("onNewIntent %s", UiUtils.dumpIntentToString(intent))
        super.onNewIntent(intent)

        setIntent(intent)
        handleIntents(intent)
    }

    //region intent handling
    private fun handleIntents(intent: Intent?) {
        intent ?: return
        when (intent.action) {
            Intent.ACTION_SEARCH -> handleSearchIntent(intent)
        }
    }

    private fun handleSearchIntent(intent: Intent) {
        val query = intent.getStringExtra(SearchManager.QUERY) ?: return
        mainViewModel.handleSearchIntent(query)
        binding.tbMain.root.menu.findItem(R.id.action_search).collapseActionView()
    }
    //endregion

    //region menu handing
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        menu.findItem(R.id.action_search).apply {
            with(actionView as SearchView) {
                val searchManager = systemService<SearchManager>(Context.SEARCH_SERVICE)
                setSearchableInfo(searchManager.getSearchableInfo(componentName))
                isSubmitButtonEnabled = false
                isQueryRefinementEnabled = true
                setIconifiedByDefault(true)
            }
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        mainViewModel.onMenuReady()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                Timber.tag(TAG).d("onOptionsItemSelected home")
                binding.drawerLayout.openDrawer(GravityCompat.START)
                return true
            }
            R.id.action_search -> {
                Timber.tag(TAG).d("onOptionsItemSelected search")
                return true
            }
            R.id.action_load -> {
                Timber.tag(TAG).d("onOptionsItemSelected load distances from ddbb")
                mainViewModel.onLoadDistancesClick()
                return true
            }
            R.id.action_crash -> {
                Timber.tag(TAG).d("onOptionsItemSelected crash")
                mainViewModel.onForceCrashClick()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
    //endregion menu handling

    private fun showLoadedDistancesDialog(distances: List<Distance>) {
        DistanceSelectionDialogFragment()
                .apply {
                    setDistanceList(distances)
                    setOnDialogActionListener { position ->
                        val distance = distances[position]
                        mainViewModel.onDistanceToShowSelected(distance)
                    }
                }
                .show(supportFragmentManager, null)
    }

    private fun showRateDialog() {
        Timber.tag(TAG).d("showRateDialog")

        InAppReviewHandler.rateApp(this)
    }

    /**
     * Called when the Activity is restarted, even before it becomes visible.
     */
    @SuppressLint("MissingPermission")
    public override fun onStart() {
        Timber.tag(TAG).d("onStart")

        super.onStart()
        if (permissionChecker.isLocationPermissionGranted()) {
            googleMap?.isMyLocationEnabled = true
            binding.fabMyLocation.isVisible = true
        } else {
            binding.fabMyLocation.isVisible = false
        }
    }

    public override fun onDestroy() {
        Timber.tag(TAG).d("onDestroy")

        hideChart()
        super.onDestroy()
    }

    private fun fixMapPadding() {
        Timber.tag(TAG).d("fixMapPadding elevationChartShown ${binding.elevationChartView.isShown}")
        googleMap?.setPadding(
                0,
                if (binding.elevationChartView.isShown) binding.elevationChartView.height else 0,
                0,
                0)
    }

    private fun hideChart() {
        binding.elevationChartView.isInvisible = true
        binding.fabShowChart.isInvisible = true
        fixMapPadding()
    }

    private fun showChart() {
        binding.elevationChartView.isVisible = true
        fixMapPadding()
    }

    private fun buildChart(elevationModel: ElevationModel) {
        binding.elevationChartView.setElevationProfile(elevationModel.elevationList)
        binding.elevationChartView.setTitle(elevationModel.altitudeUnit)

        if (!isMinimiseButtonShown()) {
            showChart()
        }
    }

    private fun animateHideChart() {
        AnimatorUtil.replaceViews(binding.elevationChartView, binding.fabShowChart)
    }

    private fun animateShowChart() {
        AnimatorUtil.replaceViews(binding.fabShowChart, binding.elevationChartView)
    }

    private fun onShowChartClick() {
        animateShowChart()
    }

    private fun onMyLocationClick() {
        mainViewModel.onMyLocationButtonClick()
    }

    private fun showAddressSelectionDialog(addressList: List<gc.david.dfm.address.domain.model.Address>) {
        val addressSuggestionsDialogFragment = AddressSuggestionsDialogFragment()
        addressSuggestionsDialogFragment.setAddressList(addressList)
        addressSuggestionsDialogFragment.setOnDialogActionListener {
            position -> addressViewModel.onAddressSelected(addressList[position])
        }
        addressSuggestionsDialogFragment.show(supportFragmentManager, null)
    }

    private fun showPositionByName(address: gc.david.dfm.address.domain.model.Address) {
        Timber.tag(TAG).d("showPositionByName $address")

        mainViewModel.onPositionByNameResolved(address.coordinates.toCommonCoordinates())
    }

    companion object {

        private const val TAG = "MainActivity"
        private val PERMISSIONS = arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
    }
}

private fun LatLng.toCoordinates() = Coordinates(latitude, longitude)
private fun Coordinates.toLatLng() = LatLng(latitude, longitude)
private fun AddressCoordinate.toCommonCoordinates() = Coordinates(latitude, longitude)
