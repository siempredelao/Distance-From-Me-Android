/*
 * Copyright (c) 2018 David Aguiar Gonzalez
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

package gc.david.dfm.dagger;

import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import gc.david.dfm.ConnectionManager;
import gc.david.dfm.DFMApplication;
import gc.david.dfm.DefaultConnectionManager;
import gc.david.dfm.deviceinfo.DefaultMemoryInfo;
import gc.david.dfm.deviceinfo.DefaultPackageManager;
import gc.david.dfm.deviceinfo.DeviceInfo;
import gc.david.dfm.deviceinfo.DeviceInfoApi16Decorator;
import gc.david.dfm.deviceinfo.DeviceInfoBase;
import gc.david.dfm.deviceinfo.MemoryInfo;
import gc.david.dfm.deviceinfo.PackageManager;
import gc.david.dfm.executor.Executor;
import gc.david.dfm.executor.MainThread;
import gc.david.dfm.executor.MainThreadBase;
import gc.david.dfm.executor.ThreadExecutor;

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
    PackageManager getPackageManager(Context context) {
        return new DefaultPackageManager(context);
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    @Provides
    @Singleton
    MemoryInfo getMemoryInfo(Context context) {
        return new DefaultMemoryInfo(context);
    }

    @Provides
    @Singleton
    DeviceInfo getDeviceInfo(Context context, PackageManager packageManager, MemoryInfo memoryInfo) {
        final DeviceInfoBase deviceInfoBase = new DeviceInfoBase(context, packageManager);
        return new DeviceInfoApi16Decorator(deviceInfoBase, memoryInfo);
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
