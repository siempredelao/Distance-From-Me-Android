package gc.david.dfm.address.data.mapper;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import gc.david.dfm.address.data.model.AddressCollectionEntity;
import gc.david.dfm.address.data.model.Location;
import gc.david.dfm.address.data.model.Result;
import gc.david.dfm.address.domain.model.Address;
import gc.david.dfm.address.domain.model.AddressCollection;

/**
 * Created by david on 13.01.17.
 * <p>
 * Mapper class used to transform {@link AddressCollectionEntity} in the Data layer
 * to {@link AddressCollection} in the Domain layer.
 */
@Singleton
public class AddressCollectionEntityDataMapper {

    @Inject
    public AddressCollectionEntityDataMapper() {
    }

    public AddressCollection transform(final AddressCollectionEntity addressCollectionEntity) {
        AddressCollection addressCollection = null;
        if (addressCollectionEntity != null) {
            final List<Address> addressList = new ArrayList<>();
            for (final Result result : addressCollectionEntity.getResults()) {
                final Location location = result.getGeometry().getLocation();
                final Address address = new Address(result.getFormattedAddress(),
                                                    new LatLng(location.getLatitude(), location.getLongitude()));
                addressList.add(address);
            }
            addressCollection = new AddressCollection(addressList);
        }
        return addressCollection;
    }
}
