package gc.david.dfm.map;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

import gc.david.dfm.R;

/**
 * MarkerInfoWindowAdapter controls the info window showed above the marker.
 * 
 * @author David
 * 
 */
public class MarkerInfoWindowAdapter implements InfoWindowAdapter {

	private final View view;

	public MarkerInfoWindowAdapter(final Activity activity) {
		view = activity.getLayoutInflater().inflate(R.layout.custom_info, null);
	}
	
	@Override
	public View getInfoContents(Marker marker) {
		final TextView tvAddress = (TextView) view.findViewById(R.id.infowindow_address_textview);
		tvAddress.setText(marker.getTitle());
		return view;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		return null;
	}
}
