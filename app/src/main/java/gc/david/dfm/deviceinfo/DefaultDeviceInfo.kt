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

import android.content.Context
import android.os.Build

/**
 * Created by david on 06.12.16.
 */
class DefaultDeviceInfo(
        private val context: Context,
        private val packageManager: PackageManager,
        private val memoryInfo: MemoryInfo
) : DeviceInfo {

    override fun getDeviceInfo(): String = """Important device info for analysis:

Version:
NAME=${packageManager.versionName}
RELEASE=${Build.VERSION.RELEASE}
SDK_INT=${Build.VERSION.SDK_INT}

Device:
MANUFACTURER=${Build.MANUFACTURER}
BRAND=${Build.BRAND}
MODEL=${Build.MODEL}
DEVICE=${Build.DEVICE}
PRODUCT=${Build.PRODUCT}
DENSITY_DPI=${context.resources.displayMetrics.densityDpi}

Other:
BOARD=${Build.BOARD}
BOOTLOADER=${Build.BOOTLOADER}
DISPLAY=${Build.DISPLAY}
FINGERPRINT=${Build.FINGERPRINT}
HARDWARE=${Build.HARDWARE}
HOST=${Build.HOST}
ID=${Build.ID}
TAGS=${Build.TAGS}
TIME=${Build.TIME}
TYPE=${Build.TYPE}
USER=${Build.USER}
$memoryParameters
"""

    private val memoryParameters: String
        get() {
            val freeMemoryMBs = memoryInfo.freeMemory
            val totalMemoryMBs = memoryInfo.availableMemory

            return MemoryPrinter.print(totalMemoryMBs, freeMemoryMBs)
        }


    internal object MemoryPrinter {

        fun print(totalMemoryMbs: Long, freeMemoryMbs: Long): String {
            return "TOTALMEMORYSIZE=${totalMemoryMbs}MB\nFREEMEMORYSIZE=${freeMemoryMbs}MB"
        }
    }
}
