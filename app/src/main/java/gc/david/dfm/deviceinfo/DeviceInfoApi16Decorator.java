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

package gc.david.dfm.deviceinfo;

import androidx.annotation.RequiresApi;

import java.util.Locale;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;

/**
 * Created by david on 07.12.16.
 */
@RequiresApi(api = JELLY_BEAN)
public class DeviceInfoApi16Decorator extends DeviceInfoDecorator {

    private final MemoryInfo memoryInfo;

    public DeviceInfoApi16Decorator(final DeviceInfo decoratedDeviceInfo, final MemoryInfo memoryInfo) {
        super(decoratedDeviceInfo);
        this.memoryInfo = memoryInfo;
    }

    @Override
    public String getDeviceInfo() {
        return DeviceInfoPrinter.print(super.getDeviceInfo(), getMemoryParameters());
    }

    private String getMemoryParameters() {
        final long freeMemoryMBs = memoryInfo.getFreeMemory();
        final long totalMemoryMBs = memoryInfo.getAvailableMemory();

        return MemoryPrinter.print(totalMemoryMBs, freeMemoryMBs);
    }

    static class DeviceInfoPrinter {
        static String print(final String deviceInfo, final String memoryInfo) {
            return String.format(Locale.getDefault(), "%s\n%s", deviceInfo, memoryInfo);
        }
    }

    static class MemoryPrinter {
        static String print(final long totalMemoryMbs, final long freeMemoryMbs) {
            return String.format(Locale.getDefault(),
                                 "TOTALMEMORYSIZE=%dMB\nFREEMEMORYSIZE=%dMB",
                                 totalMemoryMbs,
                                 freeMemoryMbs);
        }
    }

}
