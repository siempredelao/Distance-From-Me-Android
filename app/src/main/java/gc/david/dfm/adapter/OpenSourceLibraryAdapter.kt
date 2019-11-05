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

package gc.david.dfm.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import gc.david.dfm.R
import gc.david.dfm.opensource.presentation.model.OpenSourceLibraryModel
import gc.david.dfm.ui.fragment.OpenSourceMasterFragment.OnItemClickListener
import kotlinx.android.extensions.LayoutContainer

/**
 * Created by david on 24.01.17.
 */
class OpenSourceLibraryAdapter(
        private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<OpenSourceLibraryAdapter.OpenSourceLibraryViewHolder>() {

    private val openSourceLibraryModelList = mutableListOf<OpenSourceLibraryModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OpenSourceLibraryViewHolder {
        val view = parent.inflate(R.layout.view_opensourcelibrary_item)
        return OpenSourceLibraryViewHolder(view)
    }

    override fun onBindViewHolder(holder: OpenSourceLibraryViewHolder, position: Int) {
        holder.onBind(openSourceLibraryModelList[position])

        holder.itemView.setOnClickListener { onItemClickListener.onItemClick(openSourceLibraryModelList[holder.adapterPosition], holder) }

        holder.tvName.transitionName = position.toString() + "_id"
        holder.tvShortLicense.transitionName = position.toString() + "_content"
    }

    fun add(openSourceLibraryModels: List<OpenSourceLibraryModel>) {
        val previousSize = openSourceLibraryModels.size
        openSourceLibraryModelList.addAll(openSourceLibraryModels)
        notifyItemRangeInserted(previousSize, openSourceLibraryModels.size)
    }

    override fun getItemCount(): Int {
        return openSourceLibraryModelList.size
    }

    class OpenSourceLibraryViewHolder(
            override val containerView: View
    ) : LayoutContainer, RecyclerView.ViewHolder(containerView) {

        @BindView(R.id.opensourcelibrary_item_view_name_textview)
        lateinit var tvName: TextView
        @BindView(R.id.opensourcelibrary_item_view_version_textview)
        lateinit var tvVersion: TextView
        @BindView(R.id.opensourcelibrary_item_view_description_textview)
        lateinit var tvDescription: TextView
        @BindView(R.id.opensourcelibrary_item_view_short_license_textview)
        lateinit var tvShortLicense: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        internal fun onBind(openSourceLibraryModel: OpenSourceLibraryModel) {
            tvName.text = openSourceLibraryModel.name
            tvVersion.text = String.format("v%s", openSourceLibraryModel.version)
            tvDescription.text = openSourceLibraryModel.description
            tvShortLicense.text = String.format("%s license", openSourceLibraryModel.license)
        }
    }
}
