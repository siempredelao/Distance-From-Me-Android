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

package gc.david.dfm.address.data;

import android.content.Context;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Locale;

import gc.david.dfm.R;
import gc.david.dfm.address.data.model.AddressCollectionEntity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by david on 12.01.17.
 */
public class AddressRemoteDataSource implements AddressRepository {

    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/geocode/json?%s&key=%s";

    private final OkHttpClient client = new OkHttpClient();
    private final Gson         gson   = new Gson();
    private final String geocodeApiKey;

    public AddressRemoteDataSource(final Context context) {
        this.geocodeApiKey = context.getResources().getString(R.string.maps_geocode_api_key);
    }

    @Override
    public void getNameByCoordinates(final LatLng coordinates, final Callback callback) {
        executeRequest(getNameByCoordinatesUrl(coordinates), callback);
    }

    @Override
    public void getCoordinatesByName(final String name, final Callback callback) {
        executeRequest(getCoordinatesByNameUrl(name), callback);
    }

    @Nullable
    private void executeRequest(final String url, final Callback callback) {
        final Request request = new Request.Builder().url(url).header("content-type", "application/json").build();

        try {
            final Response response = client.newCall(request).execute();
            final AddressCollectionEntity addressCollectionEntity = gson.fromJson(response.body().charStream(),
                                                                                  AddressCollectionEntity.class);
            callback.onSuccess(addressCollectionEntity);
        } catch (IOException exception) {
            callback.onError(exception.getMessage());
        }
    }

    private String getNameByCoordinatesUrl(final LatLng coordinates) {
        return getUrl(String.format("latlng=%s,%s",
                                    String.valueOf(coordinates.latitude),
                                    String.valueOf(coordinates.longitude)));
    }

    private String getCoordinatesByNameUrl(final String name) {
        final String parameterValue = name.replace(" ", "+");
        return getUrl(String.format("address=%s", parameterValue));
    }

    private String getUrl(final String parameter) {
        return String.format(Locale.getDefault(), BASE_URL, parameter, geocodeApiKey);
    }
}
