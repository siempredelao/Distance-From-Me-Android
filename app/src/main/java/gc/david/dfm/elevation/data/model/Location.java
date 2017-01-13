package gc.david.dfm.elevation.data.model;

import com.google.gson.annotations.SerializedName;

public class Location {

    @SerializedName("lat")
    public double latitude;
    @SerializedName("lng")
    public double longitude;

}
