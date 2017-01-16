package gc.david.dfm.address.domain.model;

import java.util.List;

/**
 * Created by david on 13.01.17.
 */
public class AddressCollection {

    private final List<Address> addressList;

    public AddressCollection(final List<Address> addressList) {
        this.addressList = addressList;
    }

    public List<Address> getAddressList() {
        return addressList;
    }
}
