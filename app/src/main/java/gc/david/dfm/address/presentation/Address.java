package gc.david.dfm.address.presentation;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by david on 13.01.17.
 */
public interface Address {

    interface Presenter {
        void searchPositionByName(String locationName);

        void selectAddressInDialog(gc.david.dfm.address.domain.model.Address address);

        void searchPositionByCoordinates(LatLng coordinates);
    }

    interface View {
        void setPresenter(Presenter presenter);

        void showConnectionProblemsDialog();

        void showProgressDialog();

        void hideProgressDialog();

        void showCallError(); // TODO: 13.01.17 find a better name

        void showNoMatchesMessage();

        void showAddressSelectionDialog(List<gc.david.dfm.address.domain.model.Address> addressList);

        void showPositionByName(gc.david.dfm.address.domain.model.Address address);

        void showPositionByCoordinates(gc.david.dfm.address.domain.model.Address address);
    }

}
