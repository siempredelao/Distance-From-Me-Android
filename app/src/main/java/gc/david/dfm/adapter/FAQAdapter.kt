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

package gc.david.dfm.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import gc.david.dfm.databinding.ViewFeedbackCardItemBinding
import gc.david.dfm.faq.model.Faq

/**
 * Created by david on 14.12.16.
 */
class FAQAdapter : RecyclerView.Adapter<FAQAdapter.FAQViewHolder>() {

    private val faqList = mutableListOf<Faq>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQViewHolder {
        val binding = ViewFeedbackCardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FAQViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FAQViewHolder, position: Int) {
        holder.onBind(faqList[position])
    }

    override fun getItemCount(): Int {
        return faqList.size
    }

    fun addAll(faqSet: Set<Faq>) {
        faqList.addAll(faqSet)
        notifyItemRangeInserted(0, faqList.size)
    }

    class FAQViewHolder(
            private val binding: ViewFeedbackCardItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                binding.textViewContent.isGone = binding.textViewContent.isShown
            }
        }

        internal fun onBind(faq: Faq) = with(binding) {
            textViewTitle.text = faq.question
            textViewContent.text = faq.answer
        }
    }
}
