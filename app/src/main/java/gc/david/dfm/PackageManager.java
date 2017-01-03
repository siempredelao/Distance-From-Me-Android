package gc.david.dfm;

import android.content.Intent;

/**
 * Created by david on 07.12.16.
 */
public interface PackageManager {

    String getVersionName();

    boolean isThereAnyActivityForIntent(Intent intent);

}