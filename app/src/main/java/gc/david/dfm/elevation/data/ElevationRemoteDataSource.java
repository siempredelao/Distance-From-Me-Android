/*
 * Copyright (c) 2018 David Aguiar Gonzalez
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

package gc.david.dfm.elevation.data;

import android.content.Context;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Locale;

import gc.david.dfm.R;
import gc.david.dfm.elevation.data.model.ElevationEntity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by david on 05.01.17.
 */
public class ElevationRemoteDataSource implements ElevationRepository {

    private final OkHttpClient client = new OkHttpClient();
    private final Gson         gson   = new Gson();
    private final String geocodeApiKey;

    public ElevationRemoteDataSource(final Context context) {
        this.geocodeApiKey = context.getResources().getString(R.string.maps_geocode_api_key);
    }

    @Override
    public void getElevation(final String coordinatesPath, final int maxSamples, final Callback callback) {
        final String url = String.format(
                Locale.getDefault(),
                "https://maps.googleapis.com/maps/api/elevation/json?path=%s&samples=%d&key=%s",
                coordinatesPath,
                maxSamples,
                geocodeApiKey);
        final Request request = new Request.Builder().url(url)
                                                     .header("content-type", "application/json")
                                                     .build();
        try {
            final Response response = client.newCall(request).execute();
            final ElevationEntity elevationEntity = gson.fromJson(response.body().charStream(), ElevationEntity.class);
            callback.onSuccess(elevationEntity);
        } catch (IOException exception) {
            callback.onError(exception.getMessage());
        }
    }
}
