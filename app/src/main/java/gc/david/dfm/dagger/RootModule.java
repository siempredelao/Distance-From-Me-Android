package gc.david.dfm.dagger;

import android.content.Context;
import android.os.Build;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import gc.david.dfm.ConnectionManager;
import gc.david.dfm.DFMApplication;
import gc.david.dfm.DefaultConnectionManager;
import gc.david.dfm.DefaultPackageManager;
import gc.david.dfm.DeviceInfo;
import gc.david.dfm.DeviceInfoApi16Decorator;
import gc.david.dfm.DeviceInfoBase;
import gc.david.dfm.PackageManager;
import gc.david.dfm.executor.Executor;
import gc.david.dfm.executor.MainThread;
import gc.david.dfm.executor.MainThreadBase;
import gc.david.dfm.executor.ThreadExecutor;
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
    PackageManager getPackageManager(Context context) {
        return new DefaultPackageManager(context);
    }

    @Provides
    @Singleton
    DeviceInfo getDeviceInfo(Context context, PackageManager packageManager) {
        final DeviceInfoBase deviceInfoBase = new DeviceInfoBase(context, packageManager);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return deviceInfoBase;
        } else {
            return new DeviceInfoApi16Decorator(context, deviceInfoBase);
        }
    }

    @Provides
    @Singleton
    MainThread provideMainThread() {
        return new MainThreadBase();
    }

    @Provides
    @Singleton
    Executor provideExecutor() {
        return new ThreadExecutor();
    }

    @Provides
    @Singleton
    ConnectionManager provideConnectionManager(Context context) {
        return new DefaultConnectionManager(context);
    }
}
