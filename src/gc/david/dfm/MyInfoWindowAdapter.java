package gc.david.dfm;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

/**
 * MyInfoWindowAdapter controls the info window showed above the marker.
 * 
 * @author David
 * 
 */
public class MyInfoWindowAdapter implements InfoWindowAdapter {

	private final View myContentsView;

	public MyInfoWindowAdapter(Activity activity) {
		myContentsView = activity.getLayoutInflater().inflate(R.layout.custom_info,
				null);
	}
	
	@Override
	public View getInfoContents(Marker marker) {
		TextView texto = (TextView) myContentsView.findViewById(R.id.datos);
		texto.setText(marker.getTitle());

		return myContentsView;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		return null;
	}

}
