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

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import gc.david.dfm.R
import gc.david.dfm.faq.model.Faq
import kotlinx.android.extensions.LayoutContainer

/**
 * Created by david on 14.12.16.
 */
class FAQAdapter : RecyclerView.Adapter<FAQAdapter.FAQViewHolder>() {

    private val faqList = mutableListOf<Faq>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQViewHolder {
        return FAQViewHolder(parent.inflate(R.layout.view_feedback_card_item))
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
            override val containerView: View
    ) : LayoutContainer, RecyclerView.ViewHolder(containerView) {

        @BindView(R.id.feedback_card_item_view_title_textview)
        lateinit var tvTitle: TextView
        @BindView(R.id.feedback_card_item_view_content_textview)
        lateinit var tvContent: TextView

        init {
            ButterKnife.bind(this, itemView)

            itemView.setOnClickListener { tvContent.isGone = tvContent.isShown }
        }

        internal fun onBind(faq: Faq) {
            tvTitle.text = faq.question
            tvContent.text = faq.answer
        }
    }
}
