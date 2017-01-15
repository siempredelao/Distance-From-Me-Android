package gc.david.dfm.address.data.model;

import com.google.gson.annotations.SerializedName;

public class Location {

    @SerializedName("lat")
    private double latitude;
    @SerializedName("lng")
    private double longitude;

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
