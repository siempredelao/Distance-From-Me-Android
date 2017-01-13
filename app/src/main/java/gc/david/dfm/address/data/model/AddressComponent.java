package gc.david.dfm.address.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AddressComponent {

    @SerializedName("long_name")
    private String       longName;
    @SerializedName("short_name")
    private String       shortName;
    private List<String> types;

}
