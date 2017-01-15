package gc.david.dfm.address.domain;

import com.google.android.gms.maps.model.LatLng;

import gc.david.dfm.address.data.AddressRepository;
import gc.david.dfm.address.data.mapper.AddressCollectionEntityDataMapper;
import gc.david.dfm.executor.Executor;
import gc.david.dfm.executor.MainThread;

/**
 * Created by david on 12.01.17.
 */
public class GetAddressNameByCoordinatesInteractor extends GetAddressAbstractInteractor<LatLng> {

    public GetAddressNameByCoordinatesInteractor(final Executor executor,
                                                 final MainThread mainThread,
                                                 final AddressCollectionEntityDataMapper addressCollectionEntityDataMapper,
                                                 final AddressRepository repository) {
        super(executor, mainThread, addressCollectionEntityDataMapper, repository);
    }

    @Override
    protected void repositoryCall(final LatLng coordinates, final AddressRepository.Callback callback) {
        repository.getNameByCoordinates(coordinates, callback);
    }
}
