package gc.david.dfm.elevation;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Locale;

import gc.david.dfm.elevation.model.ElevationModel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by david on 05.01.17.
 */
public class ElevationRemoteDataSource implements ElevationRepository {

    private static final int ELEVATION_SAMPLES = 100;

    private final OkHttpClient client = new OkHttpClient();
    private final Gson         gson   = new Gson();

    @Override
    public ElevationModel getElevation(final String coordinatesPath) {
        final Request request = new Request.Builder().url(String.format(Locale.getDefault(),
                                                                        "https://maps.googleapis.com/maps/api/elevation/json?path=%s&samples=%d",
                                                                        coordinatesPath,
                                                                        ELEVATION_SAMPLES))
                                                     .header("content-type", "application/json")
                                                     .build();
        try {
            final Response response = client.newCall(request).execute();
            final ElevationModel elevationModel = gson.fromJson(response.body().charStream(), ElevationModel.class);
            return elevationModel;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
