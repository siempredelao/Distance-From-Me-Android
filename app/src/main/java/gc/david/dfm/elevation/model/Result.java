package gc.david.dfm.elevation.model;

import com.google.gson.annotations.Expose;

public class Result {

    @Expose
    private double   elevation;
    @Expose
    private Location location;
    @Expose
    private double   resolution;

    public double getElevation() {
        return elevation;
    }
}
