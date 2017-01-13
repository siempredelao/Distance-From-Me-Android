package gc.david.dfm.address.data.model;

import com.google.gson.annotations.SerializedName;

public class Geometry {

    private Location location;
    @SerializedName("location_type")
    private String   locationType;
    private Viewport viewport;

    public Location getLocation() {
        return location;
    }
}
