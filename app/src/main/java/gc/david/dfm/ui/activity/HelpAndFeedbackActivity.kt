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
import gc.david.dfm.adapter.FAQAdapter
import gc.david.dfm.databinding.ActivityHelpAndFeedbackBinding
import gc.david.dfm.faq.FaqViewModel
import org.koin.android.ext.android.inject

class HelpAndFeedbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHelpAndFeedbackBinding
    private lateinit var faqAdapter: FAQAdapter

    private val viewModel: FaqViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpAndFeedbackBinding.inflate(layoutInflater).apply {
            setContentView(root)
            setSupportActionBar(tbMain.tbMain)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            recyclerView.layoutManager = LinearLayoutManager(this@HelpAndFeedbackActivity)
            recyclerView.itemAnimator = DefaultItemAnimator().apply { addDuration = 1000 }
            faqAdapter = FAQAdapter()
            recyclerView.adapter = faqAdapter
        }

        with(viewModel) {
            progressVisibility.observe(this@HelpAndFeedbackActivity, { visible ->
                binding.progressBar.isVisible = visible
                binding.recyclerView.isVisible = !visible
            })
            faqList.observe(this@HelpAndFeedbackActivity, { faqs ->
                faqAdapter.addAll(faqs)
            })
            errorMessage.observe(this@HelpAndFeedbackActivity, { message ->
                Snackbar.make(binding.recyclerView, message, Snackbar.LENGTH_LONG).show()
            })
            onStart()
        }
    }

    companion object {

        fun open(activity: Activity) {
            val openHelpAndFeedbackActivityIntent = Intent(activity, HelpAndFeedbackActivity::class.java)
            activity.startActivity(openHelpAndFeedbackActivityIntent)
        }
    }
}
