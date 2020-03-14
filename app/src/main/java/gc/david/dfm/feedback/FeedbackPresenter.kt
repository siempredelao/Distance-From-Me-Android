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

package gc.david.dfm.feedback

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.core.os.bundleOf
import gc.david.dfm.R
import gc.david.dfm.deviceinfo.DeviceInfo
import gc.david.dfm.deviceinfo.PackageManager

/**
 * Created by david on 07.12.16.
 */
class FeedbackPresenter(
        private val feedbackView: Feedback.View,
        private val packageManager: PackageManager,
        private val deviceInfo: DeviceInfo
) : Feedback.Presenter {

    override fun start() {
        val emailAddress = "davidaguiargonzalez@gmail.com"
        val emailSubject = feedbackView.context().getString(R.string.feedback_email_subject)
        val emailBody = feedbackView.context()
                .getString(R.string.feedback_device_info_comments, deviceInfo.getDeviceInfo())

        val uri =
                Uri.parse("mailto:$emailAddress?subject=${Uri.encode(emailSubject)}&body=${Uri.encode(emailBody)}")

        val mailtoIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = uri
        }

        val existsAnyActivityForIntent = packageManager.isThereAnyActivityForIntent(mailtoIntent)

        // Emulators may not like this check...
        if (existsAnyActivityForIntent) {
            feedbackView.showEmailClient(mailtoIntent)
        } else {
            // Nothing resolves mailto, so fallback to send...
            val sendtoIntent = Intent(Intent.ACTION_SENDTO).apply {
                type = "text/plain"
                val bundle = bundleOf(
                        Intent.EXTRA_EMAIL to arrayOf(emailAddress),
                        Intent.EXTRA_SUBJECT to emailSubject,
                        Intent.EXTRA_TEXT to emailBody
                )
                putExtras(bundle)
            }
            try {
                feedbackView.showEmailClient(sendtoIntent)
            } catch (e: ActivityNotFoundException) {
                feedbackView.showError()
            }
        }
    }
}
