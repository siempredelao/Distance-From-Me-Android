package gc.david.dfm;

import android.content.Context;

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
        return Utils.isOnline(context);
    }
}
