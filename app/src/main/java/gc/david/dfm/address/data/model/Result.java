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

import java.util.List;

public class Result {

    @SerializedName("address_components")
    private final List<AddressComponent> addressComponents;
    @SerializedName("formatted_address")
    private final String                 formattedAddress;
    private final Geometry               geometry;
    @SerializedName("place_id")
    private final String                 placeId;
    private final List<String>           types;

    private Result(Builder builder) {
        addressComponents = builder.addressComponents;
        formattedAddress = builder.formattedAddress;
        geometry = builder.geometry;
        placeId = builder.placeId;
        types = builder.types;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public static final class Builder {
        private List<AddressComponent> addressComponents;
        private String                 formattedAddress;
        private Geometry               geometry;
        private String                 placeId;
        private List<String>           types;

        public Builder() {
        }

        public Builder withAddressComponents(List<AddressComponent> val) {
            addressComponents = val;
            return this;
        }

        public Builder withFormattedAddress(String val) {
            formattedAddress = val;
            return this;
        }

        public Builder withGeometry(Geometry val) {
            geometry = val;
            return this;
        }

        public Builder withPlaceId(String val) {
            placeId = val;
            return this;
        }

        public Builder withTypes(List<String> val) {
            types = val;
            return this;
        }

        public Result build() {
            return new Result(this);
        }
    }
}
