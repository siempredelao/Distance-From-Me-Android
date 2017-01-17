/*
 * Copyright (c) 2017 David Aguiar Gonzalez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gc.david.dfm.adapter;

import android.annotation.SuppressLint;
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

    @SuppressLint("InflateParams")
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
