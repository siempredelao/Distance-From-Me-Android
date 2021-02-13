/*
 * Copyright (c) 2021 David Aguiar Gonzalez
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

package gc.david.dfm.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife.bind
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.Marker
import gc.david.dfm.R

class MarkerInfoWindowAdapter @SuppressLint("InflateParams")
constructor(activity: Activity) : InfoWindowAdapter {

    private val view: View = activity.layoutInflater.inflate(R.layout.custom_info, null)

    @BindView(R.id.infowindow_address_textview)
    lateinit var tvAddress: TextView

    init {
        bind(this, view)
    }

    override fun getInfoContents(marker: Marker): View {
        tvAddress.text = marker.title
        return view
    }

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }
}
