package gc.david.dfm.dagger;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import gc.david.dfm.DFMApplication;
import gc.david.dfm.DeviceInfo;
import gc.david.dfm.DeviceInfoApi16Decorator;
import gc.david.dfm.DeviceInfoBase;
import gc.david.dfm.model.DaoSession;

@Module
public class RootModule {

    private final DFMApplication application;

    public RootModule(DFMApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    public DFMApplication getApplication() {
        return application;
    }

    @Provides
    @Singleton
    Context getContext() {
        return application.getApplicationContext();
    }

    @Provides
    @Singleton
    DaoSession getDaoSession(DFMApplication application) {
        return application.getDaoSession();
    }

    @Provides
    @Singleton
    PackageManager getPackageManager(DFMApplication application) {
        return application.getPackageManager();
    }

    @Provides
    @Singleton
    DeviceInfo getDeviceInfo(Context context) {
        final DeviceInfoBase deviceInfoBase = new DeviceInfoBase(context);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return deviceInfoBase;
        } else {
            return new DeviceInfoApi16Decorator(context, deviceInfoBase);
        }
    }
}
