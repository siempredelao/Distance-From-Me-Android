package gc.david.dfm.elevation.domain.model;

import java.util.List;

/**
 * Created by david on 13.01.17.
 */
public class Elevation {

    private final List<Double> results;

    public Elevation(final List<Double> results) {
        this.results = results;
    }

    public List<Double> getResults() {
        return results;
    }
}
