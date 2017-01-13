package gc.david.dfm.address.data.model;

import com.google.gson.annotations.SerializedName;

public class Location {

    @SerializedName("lat")
    private double latitude;
    @SerializedName("lng")
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
