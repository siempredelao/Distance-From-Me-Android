package gc.david.dfm.address.domain;

import gc.david.dfm.address.domain.model.AddressCollection;

/**
 * Created by david on 12.01.17.
 */
public interface GetAddressUseCase<T> {

    interface Callback {

        void onAddressLoaded(AddressCollection addressCollection);

        void onError(String errorMessage);

    }

    void execute(T t, int maxResults, Callback callback);
}
