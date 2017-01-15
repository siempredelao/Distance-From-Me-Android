package gc.david.dfm.address.data.model;

import com.google.gson.annotations.SerializedName;

public class Geometry {

    private Location location;
    @SerializedName("location_type")
    private String   locationType;
    private Viewport viewport;

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
