package gc.david.dfm.elevation.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Location {

    @SerializedName("lat")
    @Expose
    public double latitude;
    @SerializedName("lng")
    @Expose
    public double longitude;

}
