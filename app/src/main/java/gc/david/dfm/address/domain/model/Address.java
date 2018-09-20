/*
 * Copyright (c) 2018 David Aguiar Gonzalez
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

package gc.david.dfm.address.domain.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by david on 13.01.17.
 */
public class Address {

    private final String formattedAddress;
    private final LatLng coordinates;

    public Address(final String formattedAddress, final LatLng coordinates) {
        this.formattedAddress = formattedAddress;
        this.coordinates = coordinates;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }
}
