package gc.david.dfm.address.domain;

import android.support.annotation.VisibleForTesting;

import java.util.List;

import gc.david.dfm.address.data.AddressRepository;
import gc.david.dfm.address.data.mapper.AddressCollectionEntityDataMapper;
import gc.david.dfm.address.data.model.AddressCollectionEntity;
import gc.david.dfm.address.domain.model.Address;
import gc.david.dfm.address.domain.model.AddressCollection;
import gc.david.dfm.executor.Executor;
import gc.david.dfm.executor.Interactor;
import gc.david.dfm.executor.MainThread;

import static android.support.annotation.VisibleForTesting.PRIVATE;

/**
 * Created by david on 13.01.17.
 */
abstract class GetAddressAbstractInteractor<T> implements Interactor, GetAddressUseCase<T> {

    @VisibleForTesting(otherwise = PRIVATE)
    static final String STATUS_OK               = "OK";
    @VisibleForTesting(otherwise = PRIVATE)
    static final String STATUS_ZERO_RESULTS     = "ZERO_RESULTS";
    @VisibleForTesting(otherwise = PRIVATE)
    static final String STATUS_INVALID_REQUEST  = "INVALID_REQUEST";
    @VisibleForTesting(otherwise = PRIVATE)
    static final String STATUS_OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";
    @VisibleForTesting(otherwise = PRIVATE)
    static final String STATUS_REQUEST_DENIED   = "REQUEST_DENIED";
    @VisibleForTesting(otherwise = PRIVATE)
    static final String STATUS_UNKNOWN_ERROR    = "UNKNOWN_ERROR";

    private final   Executor                          executor;
    private final   MainThread                        mainThread;
    private final   AddressCollectionEntityDataMapper addressCollectionEntityDataMapper;
    protected final AddressRepository                 repository;

    private T        t;
    private int      maxResults;
    private Callback callback;

    GetAddressAbstractInteractor(final Executor executor,
                                 final MainThread mainThread,
                                 final AddressCollectionEntityDataMapper addressCollectionEntityDataMapper,
                                 final AddressRepository repository) {
        this.executor = executor;
        this.mainThread = mainThread;
        this.addressCollectionEntityDataMapper = addressCollectionEntityDataMapper;
        this.repository = repository;
    }

    @Override
    public void execute(T t, int maxResults, Callback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback can't be null, the client of this interactor needs to get the response in the callback");
        }
        this.t = t;
        this.maxResults = maxResults;
        this.callback = callback;
        this.executor.run(this);
    }

    @Override
    public void run() {
        repositoryCall(t, new AddressRepository.Callback() {
            @Override
            public void onSuccess(final AddressCollectionEntity addressCollectionEntity) {
                if (STATUS_OK.equals(addressCollectionEntity.getStatus()) ||
                    STATUS_ZERO_RESULTS.equals(addressCollectionEntity.getStatus())) {
                    final AddressCollection addressCollection = addressCollectionEntityDataMapper.transform(
                            addressCollectionEntity);
                    final AddressCollection limitedAddressCollection = limitAddress(addressCollection);
                    mainThread.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onAddressLoaded(limitedAddressCollection);
                        }
                    });
                } else {
                    notifyError(addressCollectionEntity.getStatus());
                }
            }

            @Override
            public void onError(final String message) {
                notifyError(message);
            }
        });
    }

    private void notifyError(final String message) {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                callback.onError(message);
            }
        });
    }

    private AddressCollection limitAddress(final AddressCollection addressCollection) {
        final List<Address> addressList = addressCollection.getAddressList();
        if (addressList.size() > maxResults) {
            addressList.subList(maxResults, addressList.size()).clear();
        }
        return addressCollection;
    }

    protected abstract void repositoryCall(T t, AddressRepository.Callback callback);
}
