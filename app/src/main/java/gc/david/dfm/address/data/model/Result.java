package gc.david.dfm.address.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Result {

    @SerializedName("address_components")
    private List<AddressComponent> addressComponents;
    @SerializedName("formatted_address")
    private String                 formattedAddress;
    private Geometry               geometry;
    @SerializedName("place_id")
    private String                 placeId;
    private List<String>           types;

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public Geometry getGeometry() {
        return geometry;
    }
}
