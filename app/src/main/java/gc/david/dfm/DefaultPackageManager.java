package gc.david.dfm;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

import java.util.List;

/**
 * Created by david on 07.12.16.
 */
public class DefaultPackageManager implements PackageManager {

    private final android.content.pm.PackageManager packageManager;
    private final Context                           context;

    public DefaultPackageManager(final Context context) {
        this.packageManager = context.getPackageManager();
        this.context = context;
    }

    @Override
    public String getVersionName() {
        try {
            return packageManager.getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return "Version name not found";
        }
    }

    @Override
    public boolean isThereAnyActivityForIntent(final Intent intent) {
        final List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);
        return !resolveInfos.isEmpty();
    }
}
