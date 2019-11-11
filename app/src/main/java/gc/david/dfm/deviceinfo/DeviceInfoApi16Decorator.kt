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

package gc.david.dfm.deviceinfo

import android.os.Build.VERSION_CODES.JELLY_BEAN
import androidx.annotation.RequiresApi

/**
 * Created by david on 07.12.16.
 */
@RequiresApi(api = JELLY_BEAN)
@Deprecated("")
class DeviceInfoApi16Decorator(
        decoratedDeviceInfo: DeviceInfo,
        private val memoryInfo: MemoryInfo
) : DeviceInfoDecorator(decoratedDeviceInfo) {

    override val deviceInfo: String
        get() = DeviceInfoPrinter.print(super.deviceInfo, memoryParameters)

    private val memoryParameters: String
        get() {
            val freeMemoryMBs = memoryInfo.freeMemory
            val totalMemoryMBs = memoryInfo.availableMemory

            return MemoryPrinter.print(totalMemoryMBs, freeMemoryMBs)
        }

    internal object DeviceInfoPrinter {

        fun print(deviceInfo: String, memoryInfo: String): String {
            return "$deviceInfo\n$memoryInfo"
        }
    }

    internal object MemoryPrinter {

        fun print(totalMemoryMbs: Long, freeMemoryMbs: Long): String {
            return "TOTALMEMORYSIZE=${totalMemoryMbs}MB\nFREEMEMORYSIZE=${freeMemoryMbs}MB"
        }
    }

}
