package gc.david.dfm;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by david on 10.01.17.
 */
public class DefaultConnectionManager implements ConnectionManager {

    private Context context;

    public DefaultConnectionManager(final Context context) {
        this.context = context;
    }

    @Override
    public boolean isOnline() {
        return isOnline(context);
    }

    private boolean isOnline(final Context context) {
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
