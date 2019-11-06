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

package gc.david.dfm.map

object LocationUtils {

    /**
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    const val CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000

    /*
     * Constants for location update parameters
     */
    // Milliseconds per second
    private const val MILLISECONDS_PER_SECOND = 1000L
    // The update interval
    private const val UPDATE_INTERVAL_IN_SECONDS = 5
    // Update interval in milliseconds
    const val UPDATE_INTERVAL_IN_MILLISECONDS = (MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS)
    // A fast interval ceiling
    private const val FAST_CEILING_IN_SECONDS = 1
    // A fast ceiling of update intervals, used when the app is visible
    const val FAST_INTERVAL_CEILING_IN_MILLISECONDS = (MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS)
}
