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
import androidx.annotation.StringRes

class ResourceProvider(private val context: Context) {

    fun get(@StringRes stringId: Int): String = context.getString(stringId)

    // In order to spread the vararg, we need to use the asterisk operator;
    // otherwise, Kotlin considers it as one single parameter
    fun get(@StringRes stringId: Int, vararg params: Any) = context.getString(stringId, *params)
}