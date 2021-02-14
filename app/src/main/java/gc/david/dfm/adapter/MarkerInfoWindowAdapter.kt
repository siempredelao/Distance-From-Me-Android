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
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.Marker
import gc.david.dfm.databinding.CustomInfoBinding

class MarkerInfoWindowAdapter @SuppressLint("InflateParams")
constructor(activity: Activity) : InfoWindowAdapter {

    private val binding = CustomInfoBinding.inflate(LayoutInflater.from(activity))

    override fun getInfoContents(marker: Marker): View {
        binding.textViewAddress.text = marker.title
        return binding.root
    }

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }
}
