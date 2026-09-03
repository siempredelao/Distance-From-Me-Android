/*
 * Copyright (c) 2026 David Aguiar Gonzalez
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

package gc.david.dfm.common

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import timber.log.Timber

object UiUtils {

    private const val TAG = "UiUtils"

    fun toastIt(charSequence: String, context: Context) {
        Timber.tag(TAG).d("toastIt message=$charSequence")

        Toast.makeText(context, charSequence, Toast.LENGTH_LONG).show()
    }

    fun toastIt(@StringRes stringRes: Int, context: Context) {
        Timber.tag(TAG).d("toastIt message=%s", context.getString(stringRes))

        Toast.makeText(context, stringRes, Toast.LENGTH_LONG).show()
    }

    fun dumpBundleToString(bundle: Bundle?): String {
        return bundle?.toString() ?: "bundle is null"
    }
}
