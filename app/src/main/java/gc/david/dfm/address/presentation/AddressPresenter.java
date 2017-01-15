package gc.david.dfm.address.presentation;

import com.google.android.gms.maps.model.LatLng;

import gc.david.dfm.ConnectionManager;
import gc.david.dfm.address.domain.GetAddressUseCase;
import gc.david.dfm.address.domain.model.AddressCollection;

/**
 * Created by david on 13.01.17.
 */
public class AddressPresenter implements Address.Presenter {

    private static final int MAX_BY_NAME  = 5;
    private static final int MAX_BY_COORD = 1;

    private final Address.View      addressView;
    private final GetAddressUseCase getAddressCoordinatesByNameUseCase;
    private final GetAddressUseCase getAddressNameByCoordinatesUseCase;
    private final ConnectionManager connectionManager;

    public AddressPresenter(final Address.View addressView,
                            final GetAddressUseCase getAddressCoordinatesByNameUseCase,
                            final GetAddressUseCase getAddressNameByCoordinatesUseCase,
                            final ConnectionManager connectionManager) {
        this.addressView = addressView;
        this.getAddressCoordinatesByNameUseCase = getAddressCoordinatesByNameUseCase;
        this.getAddressNameByCoordinatesUseCase = getAddressNameByCoordinatesUseCase;
        this.connectionManager = connectionManager;
        this.addressView.setPresenter(this);
    }

    @Override
    public void searchPositionByName(final String locationName) {
        if (!connectionManager.isOnline()) {
            addressView.showConnectionProblemsDialog();
        } else {
            addressView.showProgressDialog();

            getAddressCoordinatesByNameUseCase.execute(locationName, MAX_BY_NAME, new GetAddressUseCase.Callback() {
                @Override
                public void onAddressLoaded(final AddressCollection addressCollection) {
                    addressView.hideProgressDialog();

                    if (addressCollection.getAddressList().isEmpty()) {
                        addressView.showNoMatchesMessage();
                    } else if (addressCollection.getAddressList().size() == 1) {
                        addressView.showPositionByName(addressCollection.getAddressList().get(0));
                    } else {
                        addressView.showAddressSelectionDialog(addressCollection.getAddressList());
                    }
                }

                @Override
                public void onError(final String errorMessage) {
                    addressView.hideProgressDialog();

                    addressView.showCallError(errorMessage);
                }
            });
        }
    }

    @Override
    public void selectAddressInDialog(final gc.david.dfm.address.domain.model.Address address) {
        addressView.showPositionByName(address);
    }

    @Override
    public void searchPositionByCoordinates(final LatLng coordinates) {
        if (!connectionManager.isOnline()) {
            addressView.showConnectionProblemsDialog();
        } else {
            addressView.showProgressDialog();

            getAddressNameByCoordinatesUseCase.execute(coordinates, MAX_BY_COORD, new GetAddressUseCase.Callback() {
                @Override
                public void onAddressLoaded(final AddressCollection addressCollection) {
                    addressView.hideProgressDialog();

                    if (addressCollection.getAddressList().isEmpty()) {
                        addressView.showNoMatchesMessage();
                    } else {
                        addressView.showPositionByCoordinates(addressCollection.getAddressList().get(0));
                    }
                }

                @Override
                public void onError(final String errorMessage) {
                    addressView.hideProgressDialog();

                    addressView.showCallError(errorMessage);
                }
            });
        }
    }
}
