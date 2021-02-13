/*
 * Copyright (c) 2021 David Aguiar Gonzalez
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

package gc.david.dfm.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import gc.david.dfm.Utils
import timber.log.Timber

/**
 * Created by david on 07.11.16.
 */
class OnboardActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        checkPlayServices()
    }

    private fun checkPlayServices() {
        val googleApiAvailabilityInstance = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailabilityInstance.isGooglePlayServicesAvailable(this)

        if (resultCode == ConnectionResult.SUCCESS) {
            Timber.tag(TAG).d("checkPlayServices success")

            val openMainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(openMainActivityIntent)
            finish()
        } else {
            if (googleApiAvailabilityInstance.isUserResolvableError(resultCode)) {
                Timber.tag(TAG).d("checkPlayServices isUserRecoverableError")

                val googlePlayServicesRequestCode = 9000
                googleApiAvailabilityInstance
                        .getErrorDialog(this, resultCode, googlePlayServicesRequestCode)
                        .show()
            } else {
                Timber.tag(TAG).e("checkPlayServices device not supported, finishing")
                Utils.toastIt("This device is not supported by Google Play Services.", this)

                finish()
            }
        }
    }

    companion object {

        private val TAG = "OnboardActivity"
    }
}
