package gc.david.dfm.address.domain;

import gc.david.dfm.address.data.AddressRepository;
import gc.david.dfm.address.data.mapper.AddressCollectionEntityDataMapper;
import gc.david.dfm.executor.Executor;
import gc.david.dfm.executor.MainThread;

/**
 * Created by david on 12.01.17.
 */
public class GetAddressCoordinatesByNameInteractor extends GetAddressAbstractInteractor<String> {

    public GetAddressCoordinatesByNameInteractor(final Executor executor,
                                                 final MainThread mainThread,
                                                 final AddressCollectionEntityDataMapper addressCollectionEntityDataMapper,
                                                 final AddressRepository repository) {
        super(executor, mainThread, addressCollectionEntityDataMapper, repository);
    }

    @Override
    protected void repositoryCall(final String locationName, final AddressRepository.Callback callback) {
        repository.getCoordinatesByName(locationName, callback);
    }
}
