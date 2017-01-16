package gc.david.dfm.address.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Result {

    @SerializedName("address_components")
    private List<AddressComponent> addressComponents;
    @SerializedName("formatted_address")
    private String                 formattedAddress;
    private Geometry               geometry;
    @SerializedName("place_id")
    private String                 placeId;
    private List<String>           types;

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
