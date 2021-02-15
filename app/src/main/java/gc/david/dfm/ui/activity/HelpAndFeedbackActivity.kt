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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import gc.david.dfm.R
import gc.david.dfm.adapter.FAQAdapter
import gc.david.dfm.databinding.ActivityHelpAndFeedbackBinding
import gc.david.dfm.faq.Faqs
import gc.david.dfm.faq.FaqsPresenter
import gc.david.dfm.faq.GetFaqsInteractor
import gc.david.dfm.faq.model.Faq
import org.koin.android.ext.android.inject

class HelpAndFeedbackActivity : AppCompatActivity(), Faqs.View {

    private lateinit var binding: ActivityHelpAndFeedbackBinding

    val getFaqsUseCase: GetFaqsInteractor by inject()

    private lateinit var faqsPresenter: Faqs.Presenter
    private lateinit var faqAdapter: FAQAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpAndFeedbackBinding.inflate(layoutInflater).apply {
            setContentView(root)
            setSupportActionBar(tbMain.tbMain)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        faqsPresenter = FaqsPresenter(this, getFaqsUseCase)
        faqsPresenter.start()
    }

    override fun setPresenter(presenter: Faqs.Presenter) {
        this.faqsPresenter = presenter
    }

    override fun showLoading() {
        binding.progressBar.isVisible = true
        binding.recyclerView.isVisible = false
    }

    override fun hideLoading() {
        binding.progressBar.isVisible = false
        binding.recyclerView.isVisible = true
    }

    override fun add(faq: Set<Faq>) {
        faqAdapter.addAll(faq)
    }

    override fun showError() {
        Snackbar.make(binding.recyclerView, R.string.faq_error_message, Snackbar.LENGTH_LONG).show()
    }

    override fun setupList() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.itemAnimator = DefaultItemAnimator().apply { addDuration = 1000 }
        faqAdapter = FAQAdapter()
        binding.recyclerView.adapter = faqAdapter
    }

    companion object {

        fun open(activity: Activity) {
            val openHelpAndFeedbackActivityIntent = Intent(activity, HelpAndFeedbackActivity::class.java)
            activity.startActivity(openHelpAndFeedbackActivityIntent)
        }
    }
}
