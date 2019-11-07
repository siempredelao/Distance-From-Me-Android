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

package gc.david.dfm

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import com.google.android.gms.maps.model.LatLng
import gc.david.dfm.logger.DFMLogger
import gc.david.dfm.map.Haversine
import gc.david.dfm.model.Position
import java.util.*

/**
 * Created by David on 15/10/2014.
 */
object Utils {

    private val TAG = Utils::class.java.simpleName

    fun isReleaseBuild() = "release" == BuildConfig.BUILD_TYPE

    fun toastIt(charSequence: String, context: Context) {
        DFMLogger.logMessage(TAG, "toastIt message=$charSequence")

        Toast.makeText(context, charSequence, Toast.LENGTH_LONG).show()
    }

    fun toastIt(@StringRes stringRes: Int, context: Context) {
        DFMLogger.logMessage(TAG, "toastIt message=" + context.getString(stringRes))

        Toast.makeText(context, stringRes, Toast.LENGTH_LONG).show()
    }

    fun showAlertDialog(action: String,
                        @StringRes title: Int,
                        @StringRes message: Int,
                        @StringRes positiveButton: Int,
                        @StringRes negativeButton: Int,
                        activity: Activity) {
        DFMLogger.logMessage(TAG, "showAlertDialog")

        AlertDialog.Builder(activity).apply {
            setTitle(title)
            setMessage(message)
            setCancelable(false)
            setPositiveButton(positiveButton) { _, _ ->
                val optionsIntent = Intent(action)
                activity.startActivity(optionsIntent)
            }
            setNegativeButton(negativeButton) { dialog, _ -> dialog.cancel() }
        }
                .create()
                .show()
    }

    fun dumpIntentToString(intent: Intent?): String {
        if (intent == null) {
            return "intent is null"
        }

        var intentAsString = StringBuilder()
        val bundle = intent.extras

        if (bundle != null) {
            val keys = bundle.keySet()
            intentAsString.append("intent=[ ")
            for (key in keys) {
                intentAsString.append(key).append("=").append(bundle.get(key)).append(", ")
            }
            intentAsString.append(" ]")
        } else {
            intentAsString = StringBuilder("intent with empty bundle")
        }
        return intentAsString.toString()
    }

    fun dumpBundleToString(bundle: Bundle?): String {
        return bundle?.toString() ?: "bundle is null"
    }

    fun calculateDistanceInMetres(coordinates: List<LatLng>): Double {
        var distanceInMetres = 0.0
        for (i in 0 until coordinates.size - 1) {
            distanceInMetres += Haversine.getDistance(
                    coordinates[i].latitude,
                    coordinates[i].longitude,
                    coordinates[i + 1].latitude,
                    coordinates[i + 1].longitude)
        }
        return distanceInMetres
    }

    fun convertPositionListToLatLngList(positionList: List<Position>): List<LatLng> {
        return positionList.map { LatLng(it.latitude, it.longitude) }
    }
}
