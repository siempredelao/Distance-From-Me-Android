/*
 * Copyright (c) 2022 David Aguiar Gonzalez
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

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import gc.david.dfm.*
import gc.david.dfm.Utils.toLatLng
import gc.david.dfm.Utils.toPoint
import gc.david.dfm.address.presentation.ConnectionIssuesData
import gc.david.dfm.database.Distance
import gc.david.dfm.database.Position
import gc.david.dfm.distance.data.CurrentLocationProvider
import gc.david.dfm.distance.data.DistanceMode
import gc.david.dfm.distance.data.DistanceModeProvider
import gc.david.dfm.distance.domain.GetPositionListInteractor
import gc.david.dfm.distance.domain.LoadDistancesInteractor
import gc.david.dfm.main.presentation.model.DrawDistanceModel
import gc.david.dfm.map.Haversine
import timber.log.Timber
import java.util.*

class MainViewModel(
    private val loadDistancesUseCase: LoadDistancesInteractor,
    private val getPositionListUseCase: GetPositionListInteractor,
    private val connectionManager: ConnectionManager,
    private val resourceProvider: ResourceProvider,
    private val preferencesProvider: PreferencesProvider,
    private val distanceModeProvider: DistanceModeProvider,
    private val currentLocationProvider: CurrentLocationProvider
) : ViewModel() {

    val connectionIssueEvent = MutableLiveData<Event<ConnectionIssuesData>>()
    val showLoadDistancesItem = MutableLiveData<Boolean>()
    val showForceCrashItem = MutableLiveData<Boolean>()
    val selectFromDistancesLoaded = MutableLiveData<Event<List<Distance>>>()
    val drawDistance = MutableLiveData<DrawDistanceModel>()
    val drawPoints = MutableLiveData<List<LatLng>>()
    val errorMessage = MutableLiveData<Event<String>>()
    val zoomMapInto = MutableLiveData<Event<LatLng>>()
    val centerMapInto = MutableLiveData<Event<LatLng>>()
    val searchAddress = MutableLiveData<Event<String>>()
    val resetMap = MutableLiveData<Unit>()
    val hideChart = MutableLiveData<Unit>()

    // Moves to current position if app has just started
    private var appHasJustStarted = true
    // Determines whether a multi-point distance is being marked on the map
    private var calculatingDistance: Boolean = false
    private var positionList = mutableListOf<LatLng>()

    private val locale: Locale
        get() {
            val defaultUnit = preferencesProvider.getMeasureUnitPreference()
            return if (DFMPreferences.MEASURE_AMERICAN_UNIT_VALUE == defaultUnit) Locale.US else Locale.FRANCE
        }

    fun onStart() {
        if (!connectionManager.isOnline()) {
            val connectionIssuesData = getConnectionIssuesData()
            connectionIssueEvent.value = Event(connectionIssuesData)
        }
    }

    private fun getConnectionIssuesData() = ConnectionIssuesData(
        resourceProvider.get(R.string.dialog_connection_problems_title),
        resourceProvider.get(R.string.dialog_connection_problems_message),
        resourceProvider.get(R.string.dialog_connection_problems_positive_button),
        resourceProvider.get(R.string.dialog_connection_problems_negative_button)
    )

    fun onResume() {
        // Reloading distances in case a new one was saved into database
        // TODO transform use case to observable to avoid this workaround
        loadDistancesItem()
    }

    /**
     * Triggered when the menu is already built and ready to be updated.
     */
    fun onMenuReady() {
        loadDistancesItem()
        showForceCrashItem.value = !Utils.isReleaseBuild()
    }

    private fun loadDistancesItem() {
        loadDistancesUseCase.execute(object : LoadDistancesInteractor.Callback {
            override fun onDistanceListLoaded(distanceList: List<Distance>) {
                showLoadDistancesItem.value = distanceList.isNotEmpty()
            }

            override fun onError() {
                showLoadDistancesItem.value = false
            }
        })
    }

    /**
     * Triggered when the user taps on the "Show distances" menu item.
     */
    fun onLoadDistancesClick() {
        loadDistancesUseCase.execute(object : LoadDistancesInteractor.Callback {
            override fun onDistanceListLoaded(distanceList: List<Distance>) {
                selectFromDistancesLoaded.value = Event(distanceList)
            }

            override fun onError() {
                Timber.tag(TAG).e(Exception("Unable to load distances."))
            }
        })
    }

    /**
     * Triggered when the user selects a distance from the loaded distances dialog.
     */
    fun onDistanceToShowSelected(distance: Distance) {
        getPositionListUseCase.execute(distance.id!!, object : GetPositionListInteractor.Callback {
            override fun onPositionListLoaded(positionList: List<Position>) {
                val distanceInMetres = Utils.calculateDistanceInMetres2(positionList)
                drawDistance.value =
                    DrawDistanceModel(
                        positionList.toLatLng().toMutableList(),
                        distance.name + "\n",
                        distanceInMetres,
                        Haversine.normalizeDistance(distanceInMetres, locale),
                        DrawDistanceModel.Source.DATABASE,
                        distanceModeProvider.get()
                    )
            }

            override fun onError() {
                Timber.tag(TAG).e(Exception("Unable to get position by id."))
            }
        })
    }

    fun onDistanceFromCurrentPositionSet() {
        distanceModeProvider.set(DistanceMode.FROM_CURRENT_POINT)
        resetMap()
    }

    fun onDistanceFromAnyPositionSet() {
        distanceModeProvider.set(DistanceMode.FROM_ANY_POINT)
        resetMap()
    }

    fun onMyLocationButtonClick() {
        val currentLocation = currentLocationProvider.get()
        if (currentLocation != CurrentLocationProvider.UNDEFINED) {
            val latLng = LatLng(currentLocation.lat, currentLocation.lon)
            centerMapInto.value = Event(latLng)
        }
    }

    // TODO this should be moved to a repooooo!!!
    fun onLocationChanged(location: Location) {
        Timber.tag(TAG).d("onLocationChanged ${location.toPoint()}")
        currentLocationProvider.set(location)

        if (appHasJustStarted) {
            Timber.tag(TAG).d("onLocationChanged appHasJustStarted")

            val latlng = LatLng(location.latitude, location.longitude)
            zoomMapInto.value = Event(latlng)
            appHasJustStarted = false
        }
    }

    fun onMapClick(point: LatLng) {
        Timber.tag(TAG).d("onMapClick ${point.toPoint()}")
        if (distanceModeProvider.get() == DistanceMode.FROM_ANY_POINT) {
            if (!calculatingDistance) {
                positionList.clear()
            }

            calculatingDistance = true
        } else {
            val currentLocation = currentLocationProvider.get()
            if (currentLocation == CurrentLocationProvider.UNDEFINED) {
                calculatingDistance = false
                return // Without current location, we cannot calculate any distance
            }

            if (!calculatingDistance) {
                positionList.clear()
            }
            calculatingDistance = true

            // To calculate the distance from the current position,
            // we effectively need the current position ;)
            if (positionList.isEmpty()) {
                positionList.add(LatLng(currentLocation.lat, currentLocation.lon))
            }
        }
        positionList.add(point)
        drawPoints.value = positionList
    }

    fun onPositionByNameResolved(point: LatLng) {
        if (distanceModeProvider.get() == DistanceMode.FROM_ANY_POINT) {
            if (positionList.isNotEmpty()) {
                onMapLongClick(point)
            } else {
                positionList.add(point)
                drawPoints.value = positionList
                centerMapInto.value = Event(point)
            }
        } else {
            onMapLongClick(point)
        }
    }

    fun onMapLongClick(point: LatLng) {
        Timber.tag(TAG).d("onMapLongClick ${point.toPoint()}")
        calculatingDistance = true

        if (distanceModeProvider.get() == DistanceMode.FROM_ANY_POINT) {
            if (positionList.isEmpty()) {
                errorMessage.value = Event(resourceProvider.get(R.string.toast_first_point_needed))
                return
            }
        } else {
            val currentLocation = currentLocationProvider.get()
            if (currentLocation == CurrentLocationProvider.UNDEFINED) {
                calculatingDistance = false
                return // Without current location, we cannot calculate any distance
            }

            // To calculate the distance from the current position,
            // we effectively need the current position ;)
            if (positionList.isEmpty()) {
                positionList.add(0, LatLng(currentLocation.lat, currentLocation.lon))
            }
        }

        positionList.add(point)

        val distanceInMetres = Utils.calculateDistanceInMetres(positionList)
        drawDistance.value = DrawDistanceModel(
            positionList,
            "",
            distanceInMetres,
            Haversine.normalizeDistance(distanceInMetres, locale),
            DrawDistanceModel.Source.MANUAL,
            distanceModeProvider.get()
        )

        calculatingDistance = false
    }

    fun handleSearchIntent(query: String) {
        Timber.tag(TAG).d("handleSearchIntent $query")
        val currentLocation = currentLocationProvider.get()
        if (currentLocation != CurrentLocationProvider.UNDEFINED) {
            searchAddress.value = Event(query)
        }
    }

    fun resetMap() {
        calculatingDistance = false
        positionList.clear()
        resetMap.value = Unit
        hideChart.value = Unit

    }

    fun onForceCrashClick() {
        throw RuntimeException("User forced crash")
    }

    companion object {

        private const val TAG = "MainViewModel"
    }
}
