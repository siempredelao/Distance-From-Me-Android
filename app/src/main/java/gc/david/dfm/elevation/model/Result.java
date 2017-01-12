package gc.david.dfm.elevation.model;

import com.google.gson.annotations.Expose;

public class Result {

    @Expose
    private double   elevation;
    @Expose
    private Location location;
    @Expose
    private double   resolution;

    private Result(Builder builder) {
        elevation = builder.elevation;
        location = builder.location;
        resolution = builder.resolution;
    }

    public double getElevation() {
        return elevation;
    }

    public static final class Builder {
        private double   elevation;
        private Location location;
        private double   resolution;

        public Builder() {
        }

        public Builder withElevation(double val) {
            elevation = val;
            return this;
        }

        public Builder withLocation(Location val) {
            location = val;
            return this;
        }

        public Builder withResolution(double val) {
            resolution = val;
            return this;
        }

        public Result build() {
            return new Result(this);
        }
    }
}
