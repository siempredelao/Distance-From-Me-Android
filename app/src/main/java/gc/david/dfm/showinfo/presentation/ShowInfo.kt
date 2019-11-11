/*
 * Copyright (c) 2019 David Aguiar Gonzalez
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

import com.google.android.gms.maps.model.LatLng

/**
 * Created by david on 15.01.17.
 */
interface ShowInfo {

    interface Presenter {
        fun searchPositionByCoordinates(originLatLng: LatLng, destinationLatLng: LatLng)

        fun saveDistance(name: String, distance: String, latLngPositionList: List<LatLng>)
    }

    interface View {
        fun setPresenter(presenter: Presenter)

        fun showNoInternetError()

        fun showProgress()

        fun hideProgress()

        fun setAddress(address: String, isOrigin: Boolean)

        fun showNoMatchesMessage(isOrigin: Boolean)

        fun showError(errorMessage: String, isOrigin: Boolean)

        fun showSuccessfulSave()

        fun showSuccessfulSaveWithName(distanceName: String)

        fun showFailedSave()
    }

}
