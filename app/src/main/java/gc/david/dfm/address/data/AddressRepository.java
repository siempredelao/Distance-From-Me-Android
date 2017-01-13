package gc.david.dfm.address.data;

import com.google.android.gms.maps.model.LatLng;

import gc.david.dfm.address.data.model.AddressCollectionEntity;

/**
 * Created by david on 12.01.17.
 */
public interface AddressRepository {

    AddressCollectionEntity getNameByCoordinates(LatLng latLng);

    AddressCollectionEntity getCoordinatesByName(String name);

}
