/*
 * Copyright (c) 2019 David Aguiar Gonzalez
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

package gc.david.dfm.dagger

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.Module
import dagger.Provides
import gc.david.dfm.ConnectionManager
import gc.david.dfm.DFMApplication
import gc.david.dfm.DefaultConnectionManager
import gc.david.dfm.deviceinfo.*
import gc.david.dfm.executor.Executor
import gc.david.dfm.executor.MainThread
import gc.david.dfm.executor.MainThreadBase
import gc.david.dfm.executor.ThreadExecutor
import javax.inject.Singleton

@Module
class RootModule(@get:Provides
                 @get:Singleton
                 val application: DFMApplication
) {

    @Provides
    @Singleton
    fun getContext(): Context = application.applicationContext

    @Provides
    @Singleton
    fun getPackageManager(context: Context): PackageManager {
        return DefaultPackageManager(context)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    @Provides
    @Singleton
    fun getMemoryInfo(context: Context): MemoryInfo {
        return DefaultMemoryInfo(context)
    }

    @Provides
    @Singleton
    fun getDeviceInfo(
            context: Context,
            packageManager: PackageManager,
            memoryInfo: MemoryInfo
    ): DeviceInfo {
        val deviceInfoBase = DeviceInfoBase(context, packageManager)
        return DeviceInfoApi16Decorator(deviceInfoBase, memoryInfo)
    }

    @Provides
    @Singleton
    fun provideMainThread(): MainThread {
        return MainThreadBase()
    }

    @Provides
    @Singleton
    fun provideExecutor(): Executor {
        return ThreadExecutor()
    }

    @Provides
    @Singleton
    fun provideConnectionManager(context: Context): ConnectionManager {
        return DefaultConnectionManager(context)
    }
}
