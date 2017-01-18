/*
 * Copyright (c) 2017 David Aguiar Gonzalez
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

package gc.david.dfm;

import android.app.ActivityManager;
import android.content.Context;
import android.support.annotation.RequiresApi;

import java.util.Locale;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.os.Build.VERSION_CODES.JELLY_BEAN;

/**
 * Created by david on 07.12.16.
 */
public class DeviceInfoApi16Decorator extends DeviceInfoDecorator {

    private final Context context;

    public DeviceInfoApi16Decorator(final DeviceInfo decoratedDeviceInfo,final Context context) {
        super(decoratedDeviceInfo);
        this.context = context;
    }

    @RequiresApi(api = JELLY_BEAN)
    @Override
    public String getDeviceInfo() {
        return super.getDeviceInfo() + "\n" + getMemoryParameters();
    }

    @RequiresApi(api = JELLY_BEAN)
    private String getMemoryParameters() {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        final long freeMemoryMBs = memoryInfo.availMem / 1048576L; // 1MB
        final long totalMemoryMBs = memoryInfo.totalMem / 1048576L; // 1MB

        return String.format(Locale.getDefault(),
                             "TOTALMEMORYSIZE=%dMB\nFREEMEMORYSIZE=%dMB",
                             totalMemoryMBs,
                             freeMemoryMBs);
    }
}
