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

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.os.Build.VERSION_CODES.JELLY_BEAN
import androidx.annotation.RequiresApi
import gc.david.dfm.adapter.systemService

/**
 * Created by david on 18.01.17.
 */
@RequiresApi(api = JELLY_BEAN)
class DefaultMemoryInfo(context: Context) : MemoryInfo {

    private val memoryInfo: ActivityManager.MemoryInfo

    init {
        val activityManager = context.systemService<ActivityManager>(ACTIVITY_SERVICE)
        memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
    }

    override val availableMemory: Long
        get() = memoryInfo.totalMem / MB_IN_BYTES

    override val freeMemory: Long
        get() = memoryInfo.availMem / MB_IN_BYTES

    companion object {

        private const val MB_IN_BYTES = 1_048_576L
    }
}
