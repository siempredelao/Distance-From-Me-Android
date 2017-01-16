package gc.david.dfm.address.domain.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by david on 13.01.17.
 */
public class Address {

    private final String formattedAddress;
    private final LatLng coordinates;

    public Address(final String formattedAddress, final LatLng coordinates) {
        this.formattedAddress = formattedAddress;
        this.coordinates = coordinates;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }
}
