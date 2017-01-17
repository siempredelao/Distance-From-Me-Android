/*
 * Copyright (c) 2017 David Aguiar Gonzalez
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

public class Geometry {

    private final Location location;
    @SerializedName("location_type")
    private final String   locationType;
    private final Viewport viewport;

    private Geometry(Builder builder) {
        location = builder.location;
        locationType = builder.locationType;
        viewport = builder.viewport;
    }

    public Location getLocation() {
        return location;
    }

    public static final class Builder {
        private Location location;
        private String   locationType;
        private Viewport viewport;

        public Builder() {
        }

        public Builder withLocation(Location val) {
            location = val;
            return this;
        }

        public Builder withLocationType(String val) {
            locationType = val;
            return this;
        }

        public Builder withViewport(Viewport val) {
            viewport = val;
            return this;
        }

        public Geometry build() {
            return new Geometry(this);
        }
    }
}
