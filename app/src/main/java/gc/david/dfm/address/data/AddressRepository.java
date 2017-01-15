package gc.david.dfm.address.data;

import com.google.android.gms.maps.model.LatLng;

import gc.david.dfm.address.data.model.AddressCollectionEntity;

/**
 * Created by david on 12.01.17.
 */
public interface AddressRepository {

    interface Callback {

        void onSuccess(AddressCollectionEntity addressCollectionEntity);

        void onError(String message);

    }

    void getNameByCoordinates(LatLng latLng, Callback callback);

    void getCoordinatesByName(String name, Callback callback);

}
