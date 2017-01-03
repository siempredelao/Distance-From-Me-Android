package gc.david.dfm.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

import butterknife.BindView;
import gc.david.dfm.R;

import static butterknife.ButterKnife.bind;

public class MarkerInfoWindowAdapter implements InfoWindowAdapter {

    private final View     view;

    @BindView(R.id.infowindow_address_textview)
    protected     TextView tvAddress;

    public MarkerInfoWindowAdapter(final Activity activity) {
        view = activity.getLayoutInflater().inflate(R.layout.custom_info, null);
        bind(this, view);
    }

    @Override
    public View getInfoContents(Marker marker) {
        tvAddress.setText(marker.getTitle());
        return view;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }
}
