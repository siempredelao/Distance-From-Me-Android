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
    public AddressCollectionEntity getNameByCoordinates(final LatLng coordinates) {
        return executeRequest(getNameByCoordinatesUrl(coordinates));
    }

    @Override
    public AddressCollectionEntity getCoordinatesByName(final String name) {
        return executeRequest(getCoordinatesByNameUrl(name));
    }

    @Nullable
    private AddressCollectionEntity executeRequest(final String url) {
        final Request request = new Request.Builder().url(url).header("content-type", "application/json").build();

        try {
            final Response response = client.newCall(request).execute();
            final AddressCollectionEntity addressCollectionEntity = gson.fromJson(response.body().charStream(),
                                                                                  AddressCollectionEntity.class);
            return addressCollectionEntity;
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
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
