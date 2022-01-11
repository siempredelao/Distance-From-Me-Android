/*
 * Copyright (c) 2022 David Aguiar Gonzalez
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

package gc.david.dfm

import android.content.Context
import android.net.ConnectivityManager
import gc.david.dfm.adapter.systemService

/**
 * Created by david on 10.01.17.
 */
class DefaultConnectionManager(private val context: Context) : ConnectionManager {

    override fun isOnline(): Boolean = isOnline(context)

    private fun isOnline(context: Context): Boolean {
        val connectivityManager = context.systemService<ConnectivityManager>(Context.CONNECTIVITY_SERVICE)
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo?.isConnected == true
    }
}
