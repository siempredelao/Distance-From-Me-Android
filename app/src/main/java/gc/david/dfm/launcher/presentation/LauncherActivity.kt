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

package gc.david.dfm.launcher.presentation

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import gc.david.dfm.R
import gc.david.dfm.main.presentation.MainActivity
import gc.david.dfm.support.android.UiUtils
import timber.log.Timber

/**
 * Created by david on 07.11.16.
 */
class LauncherActivity : AppCompatActivity() {

    private var playServicesDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
    }

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

                // Every resume checks again, so don't stack a second dialog on one that is still open
                if (playServicesDialog?.isShowing == true) return

                val googlePlayServicesRequestCode = 9000
                // There is nothing behind the dialog, so cancelling it closes the app
                playServicesDialog = googleApiAvailabilityInstance
                        .getErrorDialog(this, resultCode, googlePlayServicesRequestCode) { finish() }
                        ?.apply { show() }
            } else {
                Timber.tag(TAG).e("checkPlayServices device not supported, finishing")
                UiUtils.toastIt(R.string.play_services_not_supported, this)

                finish()
            }
        }
    }

    companion object {

        private const val TAG = "LauncherActivity"
    }
}
