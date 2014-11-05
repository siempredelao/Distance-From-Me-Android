package gc.david.dfm.map;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

import butterknife.InjectView;
import gc.david.dfm.R;

import static butterknife.ButterKnife.inject;

/**
 * MarkerInfoWindowAdapter controls the info window showed above the marker.
 * 
 * @author David
 * 
 */
public class MarkerInfoWindowAdapter implements InfoWindowAdapter {

	@InjectView(R.id.infowindow_address_textview)
	protected TextView tvAddress;

	private final View view;

	public MarkerInfoWindowAdapter(final Activity activity) {
		view = activity.getLayoutInflater().inflate(R.layout.custom_info, null);
		inject(this, view);
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
