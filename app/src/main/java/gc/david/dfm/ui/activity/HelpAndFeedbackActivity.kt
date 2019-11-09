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

package gc.david.dfm.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.snackbar.Snackbar
import gc.david.dfm.DFMApplication
import gc.david.dfm.R
import gc.david.dfm.Utils
import gc.david.dfm.adapter.FAQAdapter
import gc.david.dfm.dagger.DaggerFaqComponent
import gc.david.dfm.dagger.FaqModule
import gc.david.dfm.dagger.RootModule
import gc.david.dfm.deviceinfo.DeviceInfo
import gc.david.dfm.deviceinfo.PackageManager
import gc.david.dfm.faq.Faqs
import gc.david.dfm.faq.FaqsPresenter
import gc.david.dfm.faq.GetFaqsUseCase
import gc.david.dfm.faq.model.Faq
import gc.david.dfm.feedback.Feedback
import gc.david.dfm.feedback.FeedbackPresenter
import javax.inject.Inject

class HelpAndFeedbackActivity : AppCompatActivity(), Faqs.View {

    @BindView(R.id.tbMain)
    lateinit var toolbar: Toolbar
    @BindView(R.id.help_and_feedback_activity_progressbar)
    lateinit var progressBar: ProgressBar
    @BindView(R.id.help_and_feedback_activity_recyclerview)
    lateinit var recyclerView: RecyclerView

    @Inject
    lateinit var packageManager: PackageManager
    @Inject
    lateinit var deviceInfo: DeviceInfo
    @Inject
    lateinit var getFaqsUseCase: GetFaqsUseCase

    private lateinit var faqsPresenter: Faqs.Presenter
    private lateinit var faqAdapter: FAQAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_and_feedback)
        DaggerFaqComponent.builder()
                .rootModule(RootModule(application as DFMApplication))
                .faqModule(FaqModule())
                .build()
                .inject(this)
        ButterKnife.bind(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        faqsPresenter = FaqsPresenter(this, getFaqsUseCase)
        faqsPresenter.start()
    }

    @OnClick(R.id.help_and_feedback_activity_send_feedback_textview)
    fun onSendFeedbackClick() {
        FeedbackPresenter(object : Feedback.View {
            override fun showError() {
                Utils.toastIt(R.string.toast_send_feedback_error, applicationContext)
            }

            override fun showEmailClient(intent: Intent) {
                startActivity(intent)
            }

            override fun context(): Context {
                return applicationContext
            }
        }, packageManager, deviceInfo).start()
    }

    override fun setPresenter(presenter: Faqs.Presenter) {
        this.faqsPresenter = presenter
    }

    override fun showLoading() {
        progressBar.isVisible = true
        recyclerView.isVisible = false
    }

    override fun hideLoading() {
        progressBar.isVisible = false
        recyclerView.isVisible = true
    }

    override fun add(faq: Set<Faq>) {
        faqAdapter.addAll(faq)
    }

    override fun showError() {
        Snackbar.make(recyclerView, R.string.faq_error_message, Snackbar.LENGTH_LONG).show()
    }

    override fun setupList() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.itemAnimator = DefaultItemAnimator().apply { addDuration = 1000 }
        faqAdapter = FAQAdapter()
        recyclerView.adapter = faqAdapter
    }

    companion object {

        fun open(activity: Activity) {
            val openHelpAndFeedbackActivityIntent = Intent(activity, HelpAndFeedbackActivity::class.java)
            activity.startActivity(openHelpAndFeedbackActivityIntent)
        }
    }
}
