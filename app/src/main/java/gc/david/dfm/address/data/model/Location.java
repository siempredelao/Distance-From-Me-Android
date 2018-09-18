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

package gc.david.dfm.address.data.model;

import com.google.gson.annotations.SerializedName;

public class Location {

    @SerializedName("lat")
    private final double latitude;
    @SerializedName("lng")
    private final double longitude;

    private Location(Builder builder) {
        latitude = builder.latitude;
        longitude = builder.longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public static final class Builder {
        private double latitude;
        private double longitude;

        public Builder() {
        }

        public Builder withLatitude(double val) {
            latitude = val;
            return this;
        }

        public Builder withLongitude(double val) {
            longitude = val;
            return this;
        }

        public Location build() {
            return new Location(this);
        }
    }
}
