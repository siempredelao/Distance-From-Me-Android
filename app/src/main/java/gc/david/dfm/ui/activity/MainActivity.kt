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
import android.app.SearchManager
import android.content.*
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import gc.david.dfm.*
import gc.david.dfm.Utils.toPoint
import gc.david.dfm.adapter.MarkerInfoWindowAdapter
import gc.david.dfm.systemService
import gc.david.dfm.address.presentation.AddressViewModel
import gc.david.dfm.connectivity.ConnectionIssuesDialogFragment
import gc.david.dfm.core.distances.data.database.Distance
import gc.david.dfm.databinding.ActivityMainBinding
import gc.david.dfm.elevation.presentation.ElevationViewModel
import gc.david.dfm.elevation.presentation.model.ElevationModel
import gc.david.dfm.faq.presentation.activity.FaqActivity
import gc.david.dfm.feedback.InAppReviewHandler
import gc.david.dfm.opensource.presentation.activity.AboutActivity
import gc.david.dfm.main.presentation.MainViewModel
import gc.david.dfm.main.presentation.model.DrawDistanceModel
import gc.david.dfm.service.GeofencingService
import gc.david.dfm.ui.animation.AnimatorUtil
import gc.david.dfm.ui.dialog.AddressSuggestionsDialogFragment
import gc.david.dfm.ui.dialog.DistanceSelectionDialogFragment
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
    private val mapDrawer: MapDrawer by inject()
    private val mainViewModel: MainViewModel by viewModel()
    private val elevationViewModel: ElevationViewModel by viewModel()
    private val addressViewModel: AddressViewModel by viewModel()

    private lateinit var binding: ActivityMainBinding

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val latitude = intent.getDoubleExtra(GeofencingService.GEOFENCE_RECEIVER_LATITUDE_KEY, 0.0)
            val longitude = intent.getDoubleExtra(GeofencingService.GEOFENCE_RECEIVER_LONGITUDE_KEY, 0.0)
            val location = Location("").apply {
                this.latitude = latitude
                this.longitude = longitude
            }
            onLocationChanged(location)
        }
    }

    private var googleMap: GoogleMap? = null
    private var drawDistanceModel = DrawDistanceModel.EMPTY

    private val isLocationPermissionGranted: Boolean
        get() = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED

    private fun isMinimiseButtonShown(): Boolean = binding.fabShowChart.isShown

    private val onNavigationItemSelectedListener: NavigationView.OnNavigationItemSelectedListener
        get() = NavigationView.OnNavigationItemSelectedListener { menuItem ->
            binding.drawerLayout.closeDrawers()
            when (menuItem.itemId) {
                R.id.menu_current_position -> {
                    mainViewModel.onDistanceFromCurrentPositionSet()
                    menuItem.isChecked = true
                    if (!isLocationPermissionGranted) {
                        Snackbar.make(binding.drawerLayout,
                                R.string.snackbar_location_permission_needed,
                                Snackbar.LENGTH_INDEFINITE)
                                .setAction(R.string.snackbar_location_permission_action) {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    intent.data = "package:$packageName".toUri()
                                    startActivity(intent)
                                }
                                .show()
                    }
                    return@OnNavigationItemSelectedListener true
                }
                R.id.menu_any_position -> {
                    mainViewModel.onDistanceFromAnyPositionSet()
                    menuItem.isChecked = true
                    return@OnNavigationItemSelectedListener true
                }
                R.id.menu_rate_app -> {
                    showRateDialog()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.menu_settings -> {
                    SettingsActivity.open(this@MainActivity)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.menu_help_feedback -> {
                    FaqActivity.open(this@MainActivity)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.menu_about -> {
                    AboutActivity.open(this@MainActivity)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag(TAG).d("onCreate savedInstanceState=%s", Utils.dumpBundleToString(savedInstanceState))

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

            nvDrawer.setNavigationItemSelectedListener(onNavigationItemSelectedListener)
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

        observeElevationViewModel()
        observeAddressViewModel()
        observeMainViewModel()
        mainViewModel.onStart()

        handleIntents(intent)
    }

    private fun observeElevationViewModel() {
        with(elevationViewModel) {
            elevationSamples.observe(this@MainActivity) { elevationModel ->
                buildChart(elevationModel)
            }
            hideChartEvent.observe(this@MainActivity) { event ->
                event.getContentIfNotHandled()?.let { hideChart() }
            }
        }
    }

    private fun observeAddressViewModel() {
        with(addressViewModel) {
            connectionIssueEvent.observe(this@MainActivity) { event ->
                event.getContentIfNotHandled()?.let {
                    ConnectionIssuesDialogFragment().show(supportFragmentManager, null)
                }
            }
            progressVisibility.observe(this@MainActivity) { visible ->
                binding.progressView.isVisible = visible
            }
            errorMessage.observe(this@MainActivity) { event ->
                event.getContentIfNotHandled()?.let { Utils.toastIt(it, appContext) }
            }
            addressFoundEvent.observe(this@MainActivity) { event ->
                event.getContentIfNotHandled()?.let { showPositionByName(it) }
            }
            multipleAddressesFoundEvent.observe(this@MainActivity) { event ->
                event.getContentIfNotHandled()?.let { showAddressSelectionDialog(it) }
            }
        }
    }

    private fun observeMainViewModel() {
        with(mainViewModel) {
            connectionIssueEvent.observe(this@MainActivity) { event ->
                event.getContentIfNotHandled()?.let {
                    ConnectionIssuesDialogFragment().show(supportFragmentManager, null)
                }
            }
            errorMessage.observe(this@MainActivity) { event ->
                event.getContentIfNotHandled()?.let { Utils.toastIt(it, appContext) }
            }
            showLoadDistancesItem.observe(this@MainActivity) { visible ->
                Timber.tag(TAG).d("showLoadDistancesItem $visible")
                val loadItem = binding.tbMain.root.menu.findItem(R.id.action_load)
                loadItem?.isVisible = visible
            }
            showForceCrashItem.observe(this@MainActivity) { visible ->
                val crashItem = binding.tbMain.root.menu.findItem(R.id.action_crash)
                crashItem?.isVisible = visible
            }
            selectFromDistancesLoaded.observe(this@MainActivity) { event ->
                event.getContentIfNotHandled()?.let { showLoadedDistancesDialog(it) }
            }
            drawDistance.observe(this@MainActivity) { model ->
                drawAndShowMultipleDistances(model)
            }
            drawPoints.observe(this@MainActivity) { list ->
                googleMap?.let { map ->
                    map.clear()
                    list.forEach { map.addMarker(MarkerOptions().position(it)) }
                }
            }
            zoomMapInto.observe(this@MainActivity) { event ->
                event.getContentIfNotHandled()?.let {
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 17F))
                }
            }
            centerMapInto.observe(this@MainActivity) { event ->
                event.getContentIfNotHandled()?.let {
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLng(it))
                }
            }
            searchAddress.observe(this@MainActivity) { event ->
                event.getContentIfNotHandled()?.let {
                    addressViewModel.onAddressSearch(it)
                }
            }
            resetMap.observe(this@MainActivity) { googleMap?.clear() }
            hideChart.observe(this@MainActivity) { hideChart() }
        }
    }

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

        if (!isLocationPermissionGranted) {
            requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE)
        } else {
            Utils.toastIt(R.string.toast_loading_position, appContext)
            googleMap?.isMyLocationEnabled = true
            binding.fabMyLocation.isVisible = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isEmpty()) { // happens when the permissions request interaction with the user is interrupted
                Timber.tag(TAG).d("onRequestPermissionsResult INTERRUPTED")
                binding.fabMyLocation.isVisible = false
                binding.nvDrawer.menu.findItem(R.id.menu_any_position).isChecked = true
                resetMap()
            } else {
                // no need to check both permissions, they fall under location group
                if (grantResults.first() == PERMISSION_GRANTED) {
                    Timber.tag(TAG).d("onRequestPermissionsResult GRANTED")

                    Utils.toastIt(R.string.toast_loading_position, appContext)
                    googleMap?.isMyLocationEnabled = true
                    binding.fabMyLocation.isVisible = true

                    registerLocationReceiver()
                    startService(Intent(this, GeofencingService::class.java))
                } else {
                    Timber.tag(TAG).d("onRequestPermissionsResult DENIED")
                    binding.fabMyLocation.isVisible = false
                    binding.nvDrawer.menu.findItem(R.id.menu_any_position).isChecked = true
                    resetMap()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onMapLongClick(point: LatLng) {
        mainViewModel.onMapLongClick(point)
    }

    override fun onMapClick(point: LatLng) {
        mainViewModel.onMapClick(point)
    }

    override fun onInfoWindowClick(marker: Marker) {
        Timber.tag(TAG).d("onInfoWindowClick")

        with(drawDistanceModel) {
            ShowInfoActivity.open(this@MainActivity, positionList, formattedDistance)
        }
    }

    private fun resetMap() {
        Timber.tag(TAG).d("resetMap")
        mainViewModel.resetMap()
    }

    override fun onNewIntent(intent: Intent) {
        Timber.tag(TAG).d("onNewIntent %s", Utils.dumpIntentToString(intent))
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

    public override fun onStop() {
        Timber.tag(TAG).d("onStop")

        super.onStop()
        try {
            unregisterLocationReceiver()
        } catch (exception: IllegalArgumentException) {
            Timber.tag(TAG).d("onStop receiver not registered, do nothing")
        }

        stopService(Intent(this, GeofencingService::class.java))
    }

    /**
     * Called when the Activity is restarted, even before it becomes visible.
     */
    public override fun onStart() {
        Timber.tag(TAG).d("onStart")

        super.onStart()
        if (isLocationPermissionGranted) {
            registerLocationReceiver()
            startService(Intent(this, GeofencingService::class.java))
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

    fun onLocationChanged(location: Location) {
        mainViewModel.onLocationChanged(location)
    }

    private fun drawAndShowMultipleDistances(model: DrawDistanceModel) {
        Timber.tag(TAG).d("drawAndShowMultipleDistances ${toString(model.positionList)}")

        drawDistanceModel = model
        elevationViewModel.onCoordinatesSelected(model.positionList)
        googleMap?.let {
            mapDrawer.drawDistance(
                it,
                model,
                DFMPreferences.getAnimationPreference(baseContext)
            )
        }
    }

    private fun toString(list: MutableList<LatLng>) = list.joinToString { it.toPoint().toString() }

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

        val addressCoordinates = address.coordinates
        mainViewModel.onPositionByNameResolved(addressCoordinates)
    }

    private fun registerLocationReceiver() {
        ContextCompat.registerReceiver(
            this,
            locationReceiver,
            IntentFilter(GeofencingService.GEOFENCE_RECEIVER_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun unregisterLocationReceiver() {
        unregisterReceiver(locationReceiver)
    }

    companion object {

        private const val TAG = "MainActivity"
        private val PERMISSIONS = arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
        private const val PERMISSIONS_REQUEST_CODE = 2
    }
}
