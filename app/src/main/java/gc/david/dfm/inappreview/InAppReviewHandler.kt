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

package gc.david.dfm.inappreview

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task
import gc.david.dfm.R
import timber.log.Timber

object InAppReviewHandler {

    private const val TAG = "InAppReviewHandler"

    fun rateApp(activity: Activity) {
        showRateApp(activity)
    }

    /**
     * Shows rate app bottom sheet using In-App review API
     * The bottom sheet might or might not shown depending on the Quotas and limitations
     * https://developer.android.com/guide/playcore/in-app-review#quotas
     * We show fallback dialog if there is any error
     */
    private fun showRateApp(activity: Activity) {
        val reviewManager = ReviewManagerFactory.create(activity)
        val request: Task<ReviewInfo> = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Timber.tag(TAG).d("showRateApp success")
                val reviewInfo: ReviewInfo = task.result
                val flow: Task<Void> = reviewManager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener { Timber.tag("asdf").i("Review process finished") }
            } else {
                Timber.tag(TAG).d("showRateApp failure")
                // There was some problem, continue regardless of the result.
                showRateAppFallbackDialog(activity)
            }
        }
    }

    private fun showRateAppFallbackDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.dialog_rate_app_title)
            .setMessage(R.string.dialog_rate_app_message)
            .setPositiveButton(activity.getString(R.string.dialog_rate_app_positive_button)
            ) { dialog, _ ->
                dialog.dismiss()
                openPlayStoreAppPage(activity)
            }
//                .setNegativeButton(getString(R.string.dialog_rate_app_negative_button)
//                ) { dialog, _ ->
//                    dialog.dismiss()
//                    openFeedbackActivity()
//                }
            .create()
            .show()
    }

    private fun openPlayStoreAppPage(activity: Activity) {
        Timber.tag(TAG).d("openPlayStoreAppPage")

        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=gc.david.dfm")))
        } catch (e: ActivityNotFoundException) {
            Timber.tag(TAG).e(Exception("Unable to open Play Store, rooted device?"))
        }
    }
}