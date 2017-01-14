package gc.david.dfm.elevation.data;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Locale;

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

    @Override
    public void getElevation(final String coordinatesPath, final int maxSamples, final Callback callback) {
        final Request request = new Request.Builder().url(String.format(Locale.getDefault(),
                                                                        "https://maps.googleapis.com/maps/api/elevation/json?path=%s&samples=%d",
                                                                        coordinatesPath,
                                                                        maxSamples))
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
